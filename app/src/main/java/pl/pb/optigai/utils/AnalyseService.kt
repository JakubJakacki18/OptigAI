package pl.pb.optigai.utils
import android.graphics.Bitmap
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import org.json.JSONObject
import java.io.IOException

object AnalyseService{
    private const val API_KEY = "nT4D3OfHfZE0E8tI4sL0"
    private const val MODEL_URL = "https://detect.roboflow.com/braille-detection-f0rb5/10?api_key=$API_KEY"
    fun analyseText() : String
    {
        return "analyseText"
    }

    fun analyseBraille(bitmap: Bitmap, callback: (String) -> Unit) {
        try {
            val tempFile = File.createTempFile("braille", ".jpg")
            FileOutputStream(tempFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            val client = OkHttpClient()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file", tempFile.name,
                    tempFile.asRequestBody("image/jpeg".toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url(MODEL_URL)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("BrailleAnalysis", "API call failed: ${e.message}")
                    callback("Error: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!it.isSuccessful) {
                            callback("Error: ${it.code}")
                        } else {
                            val jsonStr = it.body?.string() ?: "{}"
                            Log.d("BrailleAnalysis", "Roboflow response: $jsonStr")

                            val json = JSONObject(jsonStr)
                            val predictions = json.optJSONArray("predictions")
                            val chars = mutableListOf<BrailleActivity.BrailleChar>()

                            if (predictions != null) {
                                for (i in 0 until predictions.length()) {
                                    val pred = predictions.getJSONObject(i)
                                    chars.add(
                                        BrailleActivity.BrailleChar(
                                            x = pred.getDouble("x").toFloat(),
                                            y = pred.getDouble("y").toFloat(),
                                            clazz = pred.getString("class"),
                                            height = pred.getDouble("height").toFloat()
                                        )
                                    )
                                }
                            }

                            val sentence = BrailleActivity.decode(chars)
                            callback(sentence)
                        }
                    }
                }
            })

        } catch (e: Exception) {
            callback("Error creating temp file or bitmap: ${e.message}")
        }
    }


    fun analyseItem() : String
    {
        return "analyseItem"
    }

}
