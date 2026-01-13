package pl.pb.optigai.utils.data

import android.net.Uri

/**
 * BitmapCache
 *
 * A simple singleton object that holds a cached [Bitmap] and its corresponding [Uri].
 * This is used to share the latest captured or loaded image across activities and fragments
 * without repeatedly reading from storage or recreating the bitmap.
 *
 * Properties:
 * - [bitmap]: The currently cached [android.graphics.Bitmap], or null if none.
 * - [lastUri]: The [Uri] corresponding to the cached bitmap, or null if none.
 */
object BitmapCache {
    /**
     * The currently cached bitmap.
     */
    var bitmap: android.graphics.Bitmap? = null

    /**
     * The URI of the last cached bitmap.
     */
    var lastUri: Uri? = null
}
