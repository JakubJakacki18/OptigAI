package pl.pb.optigai.ui

import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class BrailleRecognizer(private val context: android.content.Context) {

    // Nazwa pliku modelu
    private val modelName = "braille_recognition_model.tflite"

    // Nazwy klas, które model przewiduje (np. "a", "b", "c"...)
    private val labels = listOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z")

    private var interpreter: Interpreter? = null

    // Inicjalizacja interpretera
    fun initialize() {
        try {
            val modelByteBuffer = loadModelFile()
            interpreter = Interpreter(modelByteBuffer)
            Log.d("BrailleRecognizer", "Model TensorFlow Lite załadowany poprawnie.")
        } catch (e: Exception) {
            Log.e("BrailleRecognizer", "Błąd podczas ładowania modelu", e)
        }
    }

    // Funkcja do wczytywania pliku modelu z folderu assets
    private fun loadModelFile(): ByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val fileInputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Funkcja do przetwarzania obrazu i rozpoznawania znaku Braille'a
    fun recognize(imageBitmap: Bitmap): String {
        if (interpreter == null) {
            Log.e("BrailleRecognizer", "Interpreter nie jest zainicjalizowany.")
            return "Błąd: model nie jest gotowy."
        }

        // 1. Przetwarzanie obrazu
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(imageBitmap)

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(28, 28, ResizeOp.ResizeMethod.BILINEAR))
            .add(TransformToGrayscaleOp())
            .add(NormalizeOp(0.0f, 255.0f))
            .build()

        val processedImage = imageProcessor.process(tensorImage)

        // 2. Przygotowanie bufora wyjściowego
        val outputShape = interpreter!!.getOutputTensor(0).shape()
        val outputDataType = interpreter!!.getOutputTensor(0).dataType()
        val outputTensor = TensorBuffer.createFixedSize(outputShape, outputDataType)

        // 3. Uruchomienie predykcji
        interpreter!!.run(processedImage.buffer, outputTensor.buffer)

        // 4. Analiza wyników
        val probabilities = outputTensor.floatArray
        val highestProbabilityIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1

        return if (highestProbabilityIndex != -1) {
            labels[highestProbabilityIndex]
        } else {
            "Nie rozpoznano"
        }
    }

    // Funkcja do zamykania interpretera, aby zwolnić zasoby
    fun close() {
        interpreter?.close()
    }
}
