/**
 * Represents a single detected Braille character from an image.
 *
 * This class holds positional and dimensional information of the character
 * along with its predicted class (symbol).
 *
 * @property x The horizontal center position of the character in the image (in pixels).
 * @property y The vertical center position of the character in the image (in pixels).
 * @property clazz The predicted class of the Braille character (e.g., letter, number, or special symbol).
 * @property height The height of the character's bounding box (in pixels).
 * @property width The width of the character's bounding box (in pixels).
 */
package pl.pb.optigai.utils.data

import com.google.gson.annotations.SerializedName

data class BrailleChar(
    val x: Float,
    val y: Float,
    @SerializedName("class")
    val clazz: String,
    val height: Float,
    val width: Float
)

