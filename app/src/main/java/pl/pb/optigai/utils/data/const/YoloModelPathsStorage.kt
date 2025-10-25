package pl.pb.optigai.utils.data.const

import pl.pb.optigai.utils.data.YoloModelPaths

object YoloModelPathsStorage {
    val paths =
        listOf(
            YoloModelPaths("yolo11m_float32.tflite", "modelLabels/coco_labels.yaml"),
            YoloModelPaths("yolo11m_trained_v1.01_with_metadata.tflite", "modelLabels/keys_labels.yaml"),
        )
}
