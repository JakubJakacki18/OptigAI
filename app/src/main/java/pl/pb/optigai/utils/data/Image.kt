/**
 * Represents an image stored on the device.
 *
 * This class is [Parcelable] so it can be passed between activities or fragments.
 *
 * @property uri The [Uri] pointing to the image file.
 * @property fileName The name of the image file.
 * @property dateAddedTimeStamp The timestamp (in milliseconds) when the image was added
 * to the device storage.
 */
package pl.pb.optigai.utils.data

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// @Parcelize
// data class Image(
//    val resId: Int,
// ) : Parcelable

@Parcelize
data class Image(
    val uri: Uri,
    val fileName: String,
    val dateAddedTimeStamp: Long,
) : Parcelable
