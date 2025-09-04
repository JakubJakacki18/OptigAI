package pl.pb.optigai.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import pl.pb.optigai.utils.data.BitmapCache
import pl.pb.optigai.utils.data.DetectionResult

class AnalyseService(
    private val context: Context,
) {
    fun analyseText(): String = "Text recognition not yet implemented"

    suspend fun analyseBraille(): String =
        withContext(Dispatchers.Default) {
            val bitmap = BitmapCache.bitmap ?: return@withContext "Brak obrazu"
            val recognizer = BrailleRecognizer(context)

            val result = recognizer.recognizeText(bitmap)
            recognizer.close()
            result
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
                DetectionResult(it.boundingBox, category.label, category.score)
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
