package pl.pb.optigai.utils.data

import android.graphics.RectF

data class DetectionResult(
    val boundingBox: RectF?,
    val text: String,
    val accuracy: Float,
)
