package pl.pb.optigai.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import pl.pb.optigai.R
import pl.pb.optigai.ui.BrailleActivity
import pl.pb.optigai.utils.api.IBrailleApi
import pl.pb.optigai.utils.data.DetectionData
import pl.pb.optigai.utils.data.DetectionResult
import pl.pb.optigai.utils.data.const.YoloModelPathsStorage
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

class AnalyseService(
    private val context: Context,
) {
    private val apiKey = "nT4D3OfHfZE0E8tI4sL0"
    private val brailleModelUrl = "https://detect.roboflow.com/"

    suspend fun analyseText(bitmap: Bitmap): DetectionData {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)
        val detectionResults = mutableListOf<DetectionResult>()

        return try {
            val visionText = recognizer.process(image).await()
            for (block in visionText.textBlocks) {
                detectionResults.add(DetectionResult(block.text, RectF(block.boundingBox), null))
                AppLogger.i("Block text: ${block.text}")
            }
            DetectionData(getResultSummaryText(detectionResults), detectionResults)
        } catch (e: Exception) {
            AppLogger.e("Recognizer has error, result is unknown. ${e.message}")
            DetectionData(
                R.string.empty_result_fragment_analysis_result.toString(),
                listOf(),
            )
        }
    }

    suspend fun analyseBraille(bitmap: Bitmap): DetectionData {
        try {
            val tempFile = File.createTempFile("braille", ".jpg")
            FileOutputStream(tempFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            val requestFile = tempFile.asRequestBody("image/jpeg".toMediaType())
            val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

            val client = OkHttpClient()

            val retrofit =
                Retrofit
                    .Builder()
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

                val detectionResults =
                    predictions.map { brailleChar ->
                        val left = brailleChar.x - brailleChar.width / 2
                        val top = brailleChar.y - brailleChar.height / 2
                        val right = brailleChar.x + brailleChar.width / 2
                        val bottom = brailleChar.y + brailleChar.height / 2
                        val rectF = RectF(left, top, right, bottom)
                        DetectionResult(brailleChar.clazz, rectF, null)
                    }

                return DetectionData(sentence, detectionResults) // Return both
            } else {
                AppLogger.e("BrailleError: HTTP ${response.code()} ${response.errorBody()}")
                return DetectionData("Error: ${response.code()}", emptyList())
            }
        } catch (e: Exception) {
            AppLogger.e("BrailleException: ${e.message}")
            return DetectionData("Error creating temp file or bitmap: ${e.message}", emptyList())
        }
    }

    suspend fun analyseItem(bitmap: Bitmap): DetectionData =
        coroutineScope {
//            // Step 1: Create TFLite's TensorImage object
//            val image = TensorImage.fromBitmap(bitmap)
//            // Step 2: Initialize the detector object
//            val options =
//                ObjectDetector.ObjectDetectorOptions
//                    .builder()
//                    .setMaxResults(5)
//                    .setScoreThreshold(0.3f)
//                    .build()
//
//            val itemDetector =
//                ObjectDetector.createFromFileAndOptions(
//                    context,
//                    "item_recognition_model_edl4.tflite",
//                    options,
//                )

//            val keyDetector =
//                ObjectDetector.createFromFileAndOptions(
//                    context,
//                    "yolo11m_trained_v1.01_with_metadata.tflite",
//                    options,
//                )
            val detectionResultsByModels =
                YoloModelPathsStorage.paths
                    .map { model ->
                        async(Dispatchers.Default) {
                            val detector = YoloDetector(context, model.modelPath, model.configPath)
                            detector.detect(bitmap)
                        }
                    }.awaitAll()
            // Step 3: Feed given image to the detector
//            val itemResults = YoloDetector(context, "yolo11m_float32.tflite", "coco_labels.yaml").detect(bitmap)
//            val keyResults = YoloDetector(context, "yolo11m_trained_v1.01_with_metadata.tflite", "keys_labels.yaml").detect(bitmap)

            // Step 4: Parse the detection result and show it
//            var detectionResults =
//                itemResults.map {
//                    // Get the top-1 category and craft the display text
//                    val category = it.categories.first()
//                    DetectionResult(category.label, it.boundingBox, category.score)
//                }
            // val detectionResults = itemResults + keyResults
            val detectionResults = detectionResultsByModels.flatten()
            val textResult = getResultSummaryText(detectionResults)
            DetectionData(textResult, detectionResults)
        }

    private fun getResultSummaryText(detectionResult: List<DetectionResult>): String =
        if (detectionResult.isNotEmpty()) {
            detectionResult.joinToString(separator = "\n") {
                it.text
            }
        } else {
            context.getString(R.string.empty_result_fragment_analysis_result)
        }
}
