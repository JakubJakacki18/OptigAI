package pl.pb.optigai.ui

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class BrailleRecognizer(private val context: android.content.Context) {

    private val modelName = "braille_recognition_model.tflite"
    private val labels = listOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z")
    private var interpreter: Interpreter? = null

    init {
        initialize()
    }

    private fun initialize() {
        try {
            val modelByteBuffer = loadModelFile()
            interpreter = Interpreter(modelByteBuffer)
            Log.d("BrailleRecognizer", "Model TensorFlow Lite załadowany poprawnie.")
        } catch (e: Exception) {
            Log.e("BrailleRecognizer", "Błąd podczas ładowania modelu", e)
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

    private fun recognizeSingleCharacter(imageBitmap: Bitmap): String {
        if (interpreter == null) {
            Log.e("BrailleRecognizer", "Interpreter nie jest zainicjalizowany.")
            return "Błąd: model nie jest gotowy."
        }

        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(imageBitmap)

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(28, 28, ResizeOp.ResizeMethod.BILINEAR))
            .add(TransformToGrayscaleOp())
            .build()

        val processedImage = imageProcessor.process(tensorImage)

        val outputShape = interpreter!!.getOutputTensor(0).shape()
        val outputDataType = interpreter!!.getOutputTensor(0).dataType()
        val outputTensor = TensorBuffer.createFixedSize(outputShape, outputDataType)

        interpreter!!.run(processedImage.buffer, outputTensor.buffer)

        val probabilities = outputTensor.floatArray
        val highestProbabilityIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1

        return if (highestProbabilityIndex != -1 && highestProbabilityIndex < labels.size) {
            labels[highestProbabilityIndex]
        } else {
            Log.e("BrailleRecognizer", "Model predicted an invalid index: $highestProbabilityIndex")
            "?"
        }
    }

    /**
     * Używa ML Kit do rozpoznawania i segmentacji tekstu Braille'a,
     * a następnie przekazuje każdy wykryty znak do modelu TensorFlow Lite.
     */
    suspend fun recognizeText(imageBitmap: Bitmap): String {
        val recognizedText = StringBuilder()
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val inputImage = InputImage.fromBitmap(imageBitmap, 0)

        try {
            val result = recognizer.process(inputImage).await()
            val textBlocks = result.textBlocks

            // Iteruj przez każdy wykryty blok tekstu (znak Braille'a)
            for (textBlock in textBlocks) {
                for (line in textBlock.lines) {
                    val boundingBox = line.boundingBox
                    if (boundingBox != null) {
                        try {
                            // Wytnij znak Braille'a na podstawie jego bounding boxa
                            val characterBitmap = Bitmap.createBitmap(
                                imageBitmap,
                                boundingBox.left,
                                boundingBox.top,
                                boundingBox.width(),
                                boundingBox.height()
                            )
                            // Rozpoznaj wycięty znak i dodaj do wyniku
                            val recognizedChar = recognizeSingleCharacter(characterBitmap)
                            recognizedText.append(recognizedChar)
                            characterBitmap.recycle()
                        } catch (e: Exception) {
                            Log.e("BrailleRecognizer", "Błąd przy wycinaniu fragmentu: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BrailleRecognizer", "Błąd podczas rozpoznawania tekstu ML Kit: ${e.message}")
            return "Błąd: ${e.message}"
        }

        return recognizedText.toString()
    }

    fun close() {
        interpreter?.close()
    }
}
