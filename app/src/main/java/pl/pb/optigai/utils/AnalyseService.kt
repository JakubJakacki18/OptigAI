package pl.pb.optigai.utils
import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import pl.pb.optigai.utils.data.DetectionResult
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AnalyseService(
    private val context: Context,
) {
    private val API_KEY = "nT4D3OfHfZE0E8tI4sL0"
    private val MODEL_URL = "https://detect.roboflow.com/braille-detection-f0rb5/10?api_key=$API_KEY"

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

    fun analyseBraille(
        bitmap: Bitmap,
        callback: (String) -> Unit,
    ) {
        try {
            val tempFile = File.createTempFile("braille", ".jpg")
            FileOutputStream(tempFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            val client = OkHttpClient()
            val requestBody =
                MultipartBody
                    .Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        tempFile.name,
                        tempFile.asRequestBody("image/jpeg".toMediaType()),
                    ).build()

            val request =
                Request
                    .Builder()
                    .url(MODEL_URL)
                    .post(requestBody)
                    .build()

            client.newCall(request).enqueue(
                object : Callback {
                    override fun onFailure(
                        call: Call,
                        e: IOException,
                    ) {
                        Log.e("BrailleAnalysis", "API call failed: ${e.message}")
                        callback("Error: ${e.message}")
                    }

                    override fun onResponse(
                        call: Call,
                        response: Response,
                    ) {
                        response.use {
                            if (!it.isSuccessful) {
                                callback("Error: ${it.code}")
                            } else {
                                val jsonStr = it.body?.string() ?: "{}"
                                Log.d("BrailleAnalysis", "Roboflow response: $jsonStr")

                                val json = JSONObject(jsonStr)
                                val predictions = json.optJSONArray("predictions")
                                val chars = mutableListOf<BrailleActivity.BrailleChar>()

                                if (predictions != null) {
                                    for (i in 0 until predictions.length()) {
                                        val pred = predictions.getJSONObject(i)
                                        chars.add(
                                            BrailleActivity.BrailleChar(
                                                x = pred.getDouble("x").toFloat(),
                                                y = pred.getDouble("y").toFloat(),
                                                clazz = pred.getString("class"),
                                                height = pred.getDouble("height").toFloat(),
                                            ),
                                        )
                                    }
                                }

                                val sentence = BrailleActivity.decode(chars)
                                callback(sentence)
                            }
                        }
                    }
                },
            )
        } catch (e: Exception) {
            callback("Error creating temp file or bitmap: ${e.message}")
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
                val text = "${category.label}, ${category.score.times(100).toInt()}%"

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
