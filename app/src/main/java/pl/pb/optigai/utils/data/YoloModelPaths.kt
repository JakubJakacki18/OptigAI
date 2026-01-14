package pl.pb.optigai.utils.data

/**
 * Represents the file paths for a YOLO object detection model.
 *
 * @property modelPath The path to the TensorFlow Lite model file (e.g., `.tflite`).
 * @property configPath The path to the label or configuration file associated with the model (e.g., YAML file).
 */
data class YoloModelPaths(
    val modelPath: String,
    val configPath: String,
)
