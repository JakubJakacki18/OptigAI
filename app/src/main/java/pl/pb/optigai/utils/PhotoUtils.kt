package pl.pb.optigai.utils

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import pl.pb.optigai.ui.PhotoAlbumActivity.Companion.RELATIVE_PICTURES_PATH
import pl.pb.optigai.utils.data.Image
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PhotoUtils {
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
                images.add(Image(uri = contentUri, originalUri = contentUri, fileName = name, dateAddedTimeStamp = dateAdded))
            }
        }
        AppLogger.i("Found ${images.size} images")
        return images
    }

    fun extractDateAndTime(timeStamp: Long): Pair<String, String> {
        val date = Date(timeStamp * 1000)

        val dateFormat = SimpleDateFormat("dd.mm.yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(date)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = timeFormat.format(date)
        return formattedDate to formattedTime
    }
}
