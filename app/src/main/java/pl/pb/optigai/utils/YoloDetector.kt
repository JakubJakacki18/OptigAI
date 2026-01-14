package pl.pb.optigai.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import androidx.core.graphics.get
import androidx.core.graphics.scale
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.yaml.snakeyaml.Yaml
import pl.pb.optigai.utils.data.DetectionResult
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/**
 * YOLO object detector for images using TensorFlow Lite models.
 *
 * This class handles loading a YOLO TFLite model with its YAML configuration,
 * preprocessing images, running inference, and postprocessing the results
 * including non-maximum suppression.
 *
 * @param context Application context used for loading assets.
 * @param modelPath Path to the TFLite model file in assets.
 * @param modelConfigPath Path to the YAML configuration file in assets.
 */
class YoloDetector(
    context: Context,
    modelPath: String,
    modelConfigPath: String,
) {
    private val interpreter: Interpreter
    private val iouThreshold = 0.5f
    private val channels: Int
    private val labels: Map<Int, String>
    private val threshold: Float
    private val maxAmountOfDetections: Int

    init {
        val model = FileUtil.loadMappedFile(context, modelPath)
        val options = Interpreter.Options().apply { setNumThreads(4) }
        interpreter = Interpreter(model, options)

        // loading config from yaml
        val inputStream = context.assets.open(modelConfigPath)
        val yaml = Yaml()
        val data: Map<String, Any> = yaml.load(inputStream)
        inputStream.close()

        labels = loadLabels(getNamesInProperLanguage(context, data))
        channels = data["count"] as Int + 4 // 4 is bounding box params,
        threshold = (data["model_threshold"] as Number).toFloat()
        maxAmountOfDetections = data["max_detections_per_image"] as Int
    }

    /**
     * Converts label names from YAML to a [Map] of class indices to label strings.
     */
    private fun loadLabels(namesAny: Map<*, *>): Map<Int, String> {
        val namesMap = mutableMapOf<Int, String>()
        for ((key, value) in namesAny) {
            val intKey = (key as? Int) ?: (key.toString().toIntOrNull() ?: continue)
            val strValue = value.toString()
            namesMap[intKey] = strValue
        }

        return namesMap.toMap()
    }

    /**
     * Performs object detection on a given [image].
     *
     * @param image Bitmap to analyze.
     * @return List of [DetectionResult] containing detected objects.
     */
    fun detect(image: Bitmap): List<DetectionResult> {
        val input = preprocessImage(image)
        val output = Array(1) { Array(channels) { FloatArray(8400) } }
        interpreter.run(input, output)
        return postprocess(image.width, image.height, output)
    }

    /**
     * Preprocesses the input [bitmap] by resizing to 640x640 and normalizing RGB values to [0,1].
     */
    private fun preprocessImage(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val inputSize = 640
        val resized = bitmap.scale(inputSize, inputSize)
        val input = Array(1) { Array(inputSize) { Array(inputSize) { FloatArray(3) } } }

        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                val pixel = resized[x, y]
                input[0][y][x][0] = (Color.red(pixel) / 255.0f)
                input[0][y][x][1] = (Color.green(pixel) / 255.0f)
                input[0][y][x][2] = (Color.blue(pixel) / 255.0f)
            }
        }
        return input
    }

    /**
     * Postprocesses the raw output from YOLO to a list of [DetectionResult],
     * applies confidence thresholding, bounding box scaling, and non-maximum suppression.
     */
    private fun postprocess(
        imageWidth: Int,
        imageHeight: Int,
        output: Array<Array<FloatArray>>,
    ): List<DetectionResult> {
        val detectionResults = mutableListOf<DetectionResult>()
        for (i in 0 until 8400) {
            var maxClassScore = 0f
            var classId = -1
            for (c in 0 until channels - 4) {
                val classScore = output[0][4 + c][i]
                if (classScore > threshold && classScore > maxClassScore) {
                    maxClassScore = classScore
                    classId = c
                }
            }

            AppLogger.d("Detection: classId=$classId object class score=$maxClassScore ")
            if (maxClassScore <= threshold) {
                continue
            }
            val x = output[0][0][i] * imageWidth
            val y = output[0][1][i] * imageHeight
            val w = output[0][2][i] * imageWidth
            val h = output[0][3][i] * imageHeight

            val box = RectF(x - w / 2, y - h / 2, x + w / 2, y + h / 2)
            val className = labels[classId] ?: "unknown"
            detectionResults.add(DetectionResult(className, box, maxClassScore))
        }

        val result = nonMaximumSuppression(detectionResults).sortedByDescending { it.accuracy }
        return result.take(maxAmountOfDetections)
    }

    /**
     * Applies Non-Maximum Suppression (NMS) to filter overlapping detections.
     */
    private fun nonMaximumSuppression(detections: List<DetectionResult>): List<DetectionResult> {
        val result = mutableListOf<DetectionResult>()
        val detectionsByClass = detections.sortedByDescending { it.accuracy }.groupBy { it.text }

        for ((_, classDetections) in detectionsByClass) {
            val sorted = classDetections.toMutableList()

            while (sorted.isNotEmpty()) {
                val best = sorted.removeAt(0)
                result.add(best)
                sorted.removeAll {
                    intersectionOverUnion(best.boundingBox!!, it.boundingBox!!) > iouThreshold
                }
            }
        }

        return result
    }

    /**
     * Calculates the Intersection over Union (IoU) for two bounding boxes.
     *
     * @return IoU value between 0 and 1.
     */
    private fun intersectionOverUnion(
        a: RectF,
        b: RectF,
    ): Float {
        val interLeft = max(a.left, b.left)
        val interTop = max(a.top, b.top)
        val interRight = min(a.right, b.right)
        val interBottom = min(a.bottom, b.bottom)
        val interArea = max(0f, interRight - interLeft) * max(0f, interBottom - interTop)

        val areaA = (a.right - a.left) * (a.bottom - a.top)
        val areaB = (b.right - b.left) * (b.bottom - b.top)
        val unionArea = areaA + areaB - interArea

        return if (unionArea == 0f) 0f else interArea / unionArea
    }

    /**
     * Chooses the correct language-specific label names from YAML config.
     */
    private fun getNamesInProperLanguage(
        context: Context,
        data: Map<String, Any>,
    ): Map<*, *> {
        val locale: Locale = context.resources.configuration.locales[0]
        val languageCode = locale.language
        return when (languageCode) {
            "pl" -> data["names_pl"] as Map<*, *>
            else -> data["names"] as Map<*, *>
        }
    }
}
