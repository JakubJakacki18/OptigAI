/**
 * Represents the response returned by the Braille detection API.
 *
 * @property predictions A list of [BrailleChar] objects representing each detected
 * Braille character in the uploaded image.
 */
package pl.pb.optigai.utils.data

data class BrailleResponse(
    val predictions: List<BrailleChar>,
)
