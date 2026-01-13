
package pl.pb.optigai.utils.data

/**
 * Holds the results of an analysis performed on an image, including both
 * textual summary and detailed detection information.
 *
 * @property result A summary string of the analysis result, e.g., detected text
 * or recognized items.
 * @property detectionResults A list of [DetectionResult] objects containing
 * detailed information for each detected element, including bounding boxes and labels.
 */
data class DetectionData(
    val result: String,
    val detectionResults: List<DetectionResult>,
)
