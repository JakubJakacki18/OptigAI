package pl.pb.optigai.utils

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.MediaStore
import androidx.core.graphics.scale
import pl.pb.optigai.ui.PhotoAlbumActivity.Companion.RELATIVE_PICTURES_PATH
import pl.pb.optigai.utils.data.Image
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility object for working with photos on the device.
 *
 * Provides functions to read images from storage, extract metadata, and convert between
 * [Bitmap] and [Uri].
 */
object PhotoUtils {
    /**
     * Reads all images from the app-specific pictures directory (`RELATIVE_PICTURES_PATH`)
     * and returns them as a list of [Image] objects.
     *
     * @param context Context used to access content resolver.
     * @return List of [Image] objects sorted by date added in descending order.
     */
    fun imageReader(context: Context): List<Image> {
        val images = mutableListOf<Image>()
        val projection =
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
            )
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} = ? AND ${MediaStore.Images.Media.SIZE} > 0"
        val selectionArgs = arrayOf(RELATIVE_PICTURES_PATH)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val query =
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder,
            )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)

                val contentUri =
                    ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id,
                    )
                images.add(Image(uri = contentUri, fileName = name, dateAddedTimeStamp = dateAdded))
            }
        }
        AppLogger.i("Found ${images.size} images")
        return images
    }

    /**
     * Extracts a formatted date and time from a Unix timestamp.
     *
     * @param timeStamp The timestamp in seconds.
     * @return Pair where first is formatted date (`dd.MM.yyyy`) and second is formatted time (`HH:mm`).
     */
    fun extractDateAndTime(timeStamp: Long): Pair<String, String> {
        val date = Date(timeStamp * 1000)

        val dateFormat = SimpleDateFormat("dd.mm.yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(date)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = timeFormat.format(date)
        return formattedDate to formattedTime
    }

    /**
     * Converts a [Uri] pointing to an image into a [Bitmap].
     *
     * @param context Context used to access content resolver.
     * @param uri The [Uri] of the image.
     * @return A [Bitmap] in ARGB_8888 config.
     */
    fun convertUriToBitmap(
        context: Context,
        uri: Uri,
    ): Bitmap {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        val loadedBitmap = ImageDecoder.decodeBitmap(source)
        return loadedBitmap.copy(Bitmap.Config.ARGB_8888, false)
    }

    /**
     * Resizes [source] Bitmap while maintaining its original aspect ratio.
     *
     * @param source The input [Bitmap] to be resized
     * @param maxLength The maximum allowed size of width/height(longer side) in pixels
     * @return resized or original [Bitmap] if no resizing is required
     */
    fun resizeBitmap(
        source: Bitmap,
        maxLength: Int = 2048,
    ): Bitmap {
        if (source.height <= maxLength && source.width <= maxLength) {
            return source
        }

        val targetWidth: Int
        val targetHeight: Int
        try {
            val aspectRatio = source.width.toDouble() / source.height.toDouble()
            if (source.width > source.height) {
                targetWidth = maxLength
                targetHeight = (maxLength / aspectRatio).toInt()
            } else {
                targetHeight = maxLength
                targetWidth = (maxLength * aspectRatio).toInt()
            }
        } catch (_: Exception) {
            return source
        }
        return source.scale(targetWidth, targetHeight)
    }

    /**
     * Converts a [Bitmap] into a [Uri] by saving it temporarily in the cache directory.
     *
     * @param context Context used to access cache directory.
     * @param bitmap The [Bitmap] to convert.
     * @return A [Uri] pointing to the saved temporary JPEG file.
     */
    fun convertBitmapToUri(
        context: Context,
        bitmap: Bitmap,
    ): Uri {
        val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        return Uri.fromFile(file)
    }
}
