package pl.pb.optigai.utils.data

import android.graphics.Bitmap
import android.net.Uri

/**
 * Safely converts a content [Uri] into a [Bitmap].
 * Returns null if decoding fails.
 */
fun uriToBitmap(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            android.graphics.BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

