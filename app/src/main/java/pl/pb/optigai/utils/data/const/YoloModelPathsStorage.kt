/**
 * YoloModelPathsStorage
 *
 * A utility object that stores the paths to YOLO object detection models
 * used in the app. Each entry is represented by a [YoloModelPaths] object,
 * which contains the TFLite model file and the corresponding labels YAML file.
 *
 * This allows the app to easily iterate over multiple models for detecting
 * different types of objects (e.g., general items, keys).
 *
 * Example usage:
 * ```
 * for (model in YoloModelPathsStorage.paths) {
 *     val detector = YoloDetector(context, model.modelPath, model.configPath)
 *     val results = detector.detect(bitmap)
 * }
 * ```
 */
package pl.pb.optigai.utils.data.const

import pl.pb.optigai.utils.data.YoloModelPaths

object YoloModelPathsStorage {
    /**
     * List of YOLO models used by the app.
     * - The first model (`yolo11m_float32.tflite`) uses COCO labels for general object detection.
     * - The second model (`yolo11m_trained_v1.01_with_metadata.tflite`) uses a custom keys labels dataset.
     */
    val paths =
        listOf(
            YoloModelPaths("yolo11m_float32.tflite", "modelLabels/coco_labels.yaml"),
            YoloModelPaths("yolo11m_trained_v1.01_with_metadata.tflite", "modelLabels/keys_labels.yaml"),
        )
}
