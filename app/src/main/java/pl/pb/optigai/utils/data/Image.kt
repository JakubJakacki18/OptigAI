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
    val originalUri: Uri
) : Parcelable
