package pl.pb.optigai.utils.data

import android.graphics.RectF

/**
 * Represents a single detected element in an image, such as text, Braille character,
 * or an object detected by a model.
 *
 * @property text The label, text, or class of the detected element.
 * @property boundingBox The location of the detected element in the image as a [RectF],
 * or `null` if no bounding box is available.
 * @property accuracy The confidence score of the detection as a float between 0.0 and 1.0,
 * or `null` if not provided.
 */
data class DetectionResult(
    val text: String,
    val boundingBox: RectF?,
    val accuracy: Float?,
)
