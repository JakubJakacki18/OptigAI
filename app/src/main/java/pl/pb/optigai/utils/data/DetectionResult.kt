package pl.pb.optigai.utils.data

import android.graphics.RectF

data class DetectionResult(
    val text: String,
    val boundingBox: RectF?,
    val accuracy: Float?,
)
