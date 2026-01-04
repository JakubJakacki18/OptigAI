/**
 * Utility object for common image analysis operations.
 */
package pl.pb.optigai.utils

import android.graphics.Bitmap
import android.net.Uri

object AnalyseUtils {
    /**
     * Updates an ImageView with a provided [bitmap] or [uri].
     *
     * The priority is given to [bitmap]. If [bitmap] is null, [uri] will be used.
     * Throws [IllegalArgumentException] if both [bitmap] and [uri] are null.
     *
     * @param imageView The ImageView to update.
     * @param uri Optional Uri of the image to display.
     * @param bitmap Optional Bitmap of the image to display.
     */
    fun updateImageView(
        imageView: android.widget.ImageView,
        uri: Uri?,
        bitmap: Bitmap?,
    ) {
        when {
            bitmap != null -> imageView.setImageBitmap(bitmap)
            uri != null -> imageView.setImageURI(uri)
            else -> throw IllegalArgumentException("Both uri and bitmap are null")
        }
    }
}
