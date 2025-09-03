package pl.pb.optigai.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.Utils;
import org.opencv.android.OpenCVLoader;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Recognizer class for Braille characters using a pre-trained TensorFlow Lite model
 * and OpenCV for image segmentation.
 */
class BrailleRecognizer(private val context: Context) {

    private val modelName = "braille_recognition_model.tflite"

    // IMPORTANT: The list of labels must be identical to the ones on which the model was trained.
    // Ensure they are sorted in the same order as in the Python script.
    private val labels = listOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z")
    private var interpreter: Interpreter? = null

    init {
        // Initialize OpenCV library before any OpenCV functions are called.
        if (!OpenCVLoader.initDebug()) {
            Log.e("BrailleRecognizer", "OpenCV initialization failed.")
        } else {
            Log.d("BrailleRecognizer", "OpenCV initialization successful.")
        }
        initialize()
    }

    private fun initialize() {
        try {
            val modelByteBuffer = loadModelFile()
            interpreter = Interpreter(modelByteBuffer)
            Log.d("BrailleRecognizer", "TensorFlow Lite model loaded successfully.")
        } catch (e: Exception) {
            Log.e("BrailleRecognizer", "Error loading the model", e)
        }
    }

    private fun loadModelFile(): ByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val fileInputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Processes a single bitmap segment (one character) and recognizes it.
     * This function now mirrors the preprocessing from the Python training script.
     * @param imageBitmap The bitmap of a single character.
     * @return The recognized character as a String.
     */
    private fun recognizeSingleCharacter(imageBitmap: Bitmap): String {
        if (interpreter == null) {
            Log.e("BrailleRecognizer", "Interpreter is not initialized.")
            return "Error: model not ready."
        }

        // This is a simplified recognizeSingleCharacter function, it assumes the input bitmap is already a Braille character.
        // It bypasses the TensorImage.load() check and ensures the correct size for the model.
        val inputByteBuffer = ByteBuffer.allocateDirect(28 * 28 * 4) // 28x28 grayscale, 4 bytes per float
        inputByteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(28 * 28)
        val resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 28, 28, true)
        resizedBitmap.getPixels(pixels, 0, 28, 0, 0, 28, 28)

        for (pixel in pixels) {
            // Take the green channel (or any single channel) as the grayscale value
            val gray = (pixel shr 8) and 0xFF
            // Normalize to float32 (0.0 - 1.0) and put into the buffer
            inputByteBuffer.putFloat(gray / 255.0f)
        }

        // Ensure the buffer is reset to the beginning before running the interpreter
        inputByteBuffer.rewind()

        val outputShape = interpreter!!.getOutputTensor(0).shape()
        val outputDataType = interpreter!!.getOutputTensor(0).dataType()
        val outputTensor = TensorBuffer.createFixedSize(outputShape, outputDataType)

        // Run the interpreter with the manually prepared buffer
        interpreter!!.run(inputByteBuffer, outputTensor.buffer)

        val probabilities = outputTensor.floatArray

        // Log the full probabilities array for debugging
        Log.d("BrailleRecognizer", "Raw probabilities: ${probabilities.joinToString()}")

        val highestProbabilityIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1

        Log.d("BrailleRecognizer", "Highest probability index: $highestProbabilityIndex")

        resizedBitmap.recycle()
        return if (highestProbabilityIndex != -1 && highestProbabilityIndex < labels.size) {
            labels[highestProbabilityIndex]
        } else {
            Log.e("BrailleRecognizer", "Model predicted an invalid index: $highestProbabilityIndex")
            "?"
        }
    }

    /**
     * Recognizes multiple Braille characters (entire words/text) in an image.
     * Uses OpenCV for image segmentation.
     * @param imageBitmap The bitmap of the entire image.
     * @return The recognized text as a String.
     */
    fun recognizeText(imageBitmap: Bitmap): String {
        val recognizedText = StringBuilder()

        val characterBitmaps = segmentBrailleCharacters(imageBitmap)

        for (charBitmap in characterBitmaps) {
            val recognizedChar = recognizeSingleCharacter(charBitmap)
            recognizedText.append(recognizedChar)
            charBitmap.recycle()
        }

        if (recognizedText.isEmpty()) {
            Log.w("BrailleRecognizer", "No Braille characters were detected.")
            return "No Braille found"
        }

        return recognizedText.toString()
    }

    /**
     * Advanced function for segmenting an image into individual Braille characters using OpenCV.
     * It specifically finds and groups Braille dots to form characters.
     */
    private fun segmentBrailleCharacters(fullImage: Bitmap): List<Bitmap> {
        val characterBitmaps = mutableListOf<Bitmap>()
        val mat = Mat()
        Utils.bitmapToMat(fullImage, mat)

        // Convert to grayscale and apply a binary threshold to isolate dots
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
        Imgproc.adaptiveThreshold(mat, mat, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 2.0)

        // Find contours, representing potential dots
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        // Filter contours to find potential Braille dots (small, roughly circular)
        val dotRects = mutableListOf<Rect>()
        for (contour in contours) {
            val rect = Imgproc.boundingRect(contour)
            val area = rect.area()
            val aspectRatio = rect.width.toDouble() / rect.height.toDouble()

            // Heuristics for a Braille dot
            if (area > 50 && area < 500 && aspectRatio > 0.5 && aspectRatio < 2.0) {
                dotRects.add(rect)
            }
        }

        Log.d("BrailleRecognizer", "Found ${dotRects.size} potential Braille dots.")

        // Group dots into Braille cells (2x3 grid)
        val brailleCells = mutableListOf<Rect>()
        val groupedRects = mutableListOf<List<Rect>>()

        if (dotRects.isNotEmpty()) {
            // Sort all dots by x-coordinate to process them left-to-right
            val sortedByX = dotRects.sortedBy { it.x }

            // Start a new group with the first dot
            val currentGroup = mutableListOf<Rect>()
            currentGroup.add(sortedByX[0])

            // Iterate through the rest of the dots to group them
            for (i in 1 until sortedByX.size) {
                val previousDot = sortedByX[i-1]
                val currentDot = sortedByX[i]

                // If the horizontal gap is small, they are part of the same character
                // Increased the threshold from 40 to 80 for better robustness.
                if (currentDot.x - previousDot.x < 80) {
                    currentGroup.add(currentDot)
                } else {
                    // Otherwise, the previous group is a complete character, and we start a new one
                    groupedRects.add(currentGroup.toList() as List<Rect>)
                    currentGroup.clear()
                    currentGroup.add(currentDot)
                }
            }
            // Add the last group
            groupedRects.add(currentGroup.toList() as List<Rect>)
        }

        // Create a single bounding box for each character group
        for (group in groupedRects) {
            if (group.isNotEmpty()) {
                val minX = group.minByOrNull { it.x }?.x ?: 0
                val minY = group.minByOrNull { it.y }?.y ?: 0
                val maxX = group.maxByOrNull { it.x + it.width }?.x?.plus(group.maxByOrNull { it.x + it.width }?.width ?: 0) ?: 0
                val maxY = group.maxByOrNull { it.y + it.height }?.y?.plus(group.maxByOrNull { it.y + it.height }?.height ?: 0) ?: 0

                // Add padding to ensure the entire Braille character is captured
                val padding = 5
                val brailleRect = Rect(
                    minX - padding,
                    minY - padding,
                    (maxX - minX) + 2 * padding,
                    (maxY - minY) + 2 * padding
                )
                // Add a sanity check to ensure the calculated rectangle is valid
                if (brailleRect.x >= 0 && brailleRect.y >= 0 &&
                    brailleRect.x + brailleRect.width <= mat.cols() &&
                    brailleRect.y + brailleRect.height <= mat.rows()) {
                    brailleCells.add(brailleRect)
                } else {
                    Log.e("BrailleRecognizer", "Invalid rectangle coordinates, skipping segment.")
                }
            }
        }

        // Sort the final character rectangles from left to right for correct word order
        val sortedRects = brailleCells.sortedBy { it.x }

        for (rect in sortedRects) {
            try {
                // Ensure the rectangle is valid before creating the submat
                val submat = mat.submat(rect)
                val segmentBitmap = Bitmap.createBitmap(submat.cols(), submat.rows(), Bitmap.Config.ARGB_8888)
                Utils.matToBitmap(submat, segmentBitmap)
                characterBitmaps.add(segmentBitmap)
                submat.release()
            } catch (e: Exception) {
                Log.e("BrailleRecognizer", "Error cropping segment: ${e.message}")
            }
        }

        mat.release()
        hierarchy.release()

        return characterBitmaps.toList() as List<Bitmap>
    }

    fun close() {
        interpreter?.close()
    }
}
