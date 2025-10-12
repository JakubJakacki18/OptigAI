package pl.pb.optigai.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import pl.pb.optigai.ui.BrailleActivity
import pl.pb.optigai.utils.api.IBrailleApi
import pl.pb.optigai.utils.data.DetectionResult
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
class AnalyseService(
    private val context: Context,
) {
    private val apiKey = "nT4D3OfHfZE0E8tI4sL0"
    private val brailleModelUrl = "https://detect.roboflow.com/"

    suspend fun analyseText(bitmap: Bitmap): List<DetectionResult> {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)
        val result: MutableList<DetectionResult> = mutableListOf()
        return try {
            val visionText = recognizer.process(image).await()
            for (block in visionText.textBlocks) {
                result.add(DetectionResult(block.text, RectF(block.boundingBox), null))
                AppLogger.i("Block text: ${block.text}")

//                for (line in block.lines) {
//                    AppLogger.i("Line text: ${line.text}")
//                    val lineText = line.text
//                    val lineFrame = line.boundingBox
//                    result.add(DetectionResult(lineText, RectF(lineFrame), null))
//                }
            }
            result
        } catch (e: Exception) {
            AppLogger.e("Recognizer has error, result is unknown. ${e.message}")
            result
        }
    }

//        for (block in visionText.textBlocks) {
//            val blockText = block.text
//            val blockCornerPoints = block.cornerPoints
//            val blockFrame = block.boundingBox
//            for (line in block.lines) {
//                val lineText = line.text
//                val lineCornerPoints = line.cornerPoints
//                val lineFrame = line.boundingBox
//                result.add(DetectionResult(RectF(lineFrame), lineText, 0f))
//            }
//        recognizer
//            .process(image)
//            .addOnSuccessListener { visionText ->
//                val detectionResults: MutableList<DetectionResult> = mutableListOf()
//                for (block in visionText.textBlocks) {
//                    val blockText = block.text
//                    val blockCornerPoints = block.cornerPoints
//                    val blockFrame = block.boundingBox
//                    for (line in block.lines) {
//                        val lineText = line.text
//                        val lineCornerPoints = line.cornerPoints
//                        val lineFrame = line.boundingBox
//                        result.add(DetectionResult(RectF(lineFrame), lineText, 0f))
//                    }
//                }
//            }.addOnFailureListener { e ->
//                AppLogger.e("Recognizer has error, result is unknown")
//                listOf(DetectionResult(null, "Something went wrong", 0f))
//            }
//        return result
//    }

    suspend fun analyseBraille(bitmap: Bitmap): Pair<String, List<DetectionResult>> { // Return summary text and detection results
        try {
            val tempFile = File.createTempFile("braille", ".jpg")
            FileOutputStream(tempFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            val requestFile = tempFile.asRequestBody("image/jpeg".toMediaType())
            val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

            val client = OkHttpClient()

            val retrofit =
                Retrofit.Builder()
                    .baseUrl(brailleModelUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(IBrailleApi::class.java)

            val response = retrofit.uploadBraille(body, apiKey)
            if (response.isSuccessful) {
                val result = response.body()
                AppLogger.d("Braille raw response: $result")
                val predictions = result?.predictions ?: emptyList()
                val sentence = BrailleActivity.decode(predictions)
                AppLogger.d("Braille sentence: $sentence")

                val detectionResults = predictions.map { brailleChar ->
                    val left = brailleChar.x - brailleChar.width / 2
                    val top = brailleChar.y - brailleChar.height / 2
                    val right = brailleChar.x + brailleChar.width / 2
                    val bottom = brailleChar.y + brailleChar.height / 2
                    val rectF = RectF(left, top, right, bottom)
                    DetectionResult(brailleChar.clazz, rectF, null)
                }

                return Pair(sentence, detectionResults) // Return both
            } else {
                AppLogger.e("BrailleError: HTTP ${response.code()} ${response.errorBody()}")
                return Pair("Error: ${response.code()}", emptyList())
            }
        } catch (e: Exception) {
            AppLogger.e("BrailleException: ${e.message}")
            return Pair("Error creating temp file or bitmap: ${e.message}", emptyList())
        }
    }
    fun analyseItem(bitmap: Bitmap): List<DetectionResult> {
        // Step 1: Create TFLite's TensorImage object
        val image = TensorImage.fromBitmap(bitmap)
        // Step 2: Initialize the detector object
        val options =
            ObjectDetector.ObjectDetectorOptions
                .builder()
                .setMaxResults(5)
                .setScoreThreshold(0.3f)
                .build()

        val detector =
            ObjectDetector.createFromFileAndOptions(
                context,
                "item_recognition_model_edl4.tflite",
                options,
            )
        // Step 3: Feed given image to the detector
        val results = detector.detect(image)

        // Step 4: Parse the detection result and show it
        val resultToDisplay =
            results.map {
                // Get the top-1 category and craft the display text
                val category = it.categories.first()
                // val text = "${category.label}, ${category.score.times(100).toInt()}%"

                // Create a data object to display the detection result
                DetectionResult(category.label, it.boundingBox, category.score)
            }

//        // Draw the detection result on the bitmap and show it.
//        val imgWithResult = drawDetectionResult(bitmap, resultToDisplay)
//        runOnUiThread {
//            inputImageView.setImageBitmap(imgWithResult)
//        }
        return resultToDisplay
    }

//    private fun drawDetectionResult(
//        bitmap: Bitmap,
//        detectionResults: List<DetectionResult>,
//    ): Bitmap {
//        val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
//        val canvas = Canvas(outputBitmap)
//        val pen = Paint()
//
//        @Suppress("ktlint:standard:property-naming")
//        val MAX_FONT_SIZE = 96F
//        pen.textAlign = Paint.Align.LEFT
//
//        detectionResults.forEach {
//            // draw bounding box
//            pen.color = Color.RED
//            pen.strokeWidth = 8F
//            pen.style = Paint.Style.STROKE
//            val box = it.boundingBox
//            canvas.drawRect(box, pen)
//
//            val tagSize = Rect(0, 0, 0, 0)
//
//            // calculate the right font size
//            pen.style = Paint.Style.FILL_AND_STROKE
//            pen.color = Color.YELLOW
//            pen.strokeWidth = 2F
//
//            pen.textSize = MAX_FONT_SIZE
//            pen.getTextBounds(it.text, 0, it.text.length, tagSize)
//            val fontSize: Float = pen.textSize * box.width() / tagSize.width()
//
//            // adjust the font size so texts are inside the bounding box
//            if (fontSize < pen.textSize) pen.textSize = fontSize
//
//            var margin = (box.width() - tagSize.width()) / 2.0F
//            if (margin < 0F) margin = 0F
//            canvas.drawText(
//                it.text,
//                box.left + margin,
//                box.top + tagSize.height().times(1F),
//                pen,
//            )
//        }
//        return outputBitmap
//    }
}
