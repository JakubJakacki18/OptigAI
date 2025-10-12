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

