package pl.pb.optigai.utils

import android.graphics.Bitmap
import android.net.Uri

object AnalyseUtils {
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
