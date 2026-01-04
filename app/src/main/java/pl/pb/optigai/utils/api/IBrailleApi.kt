/**
 * IBrailleApi
 *
 * Retrofit interface for interacting with the Braille detection API.
 * Provides endpoints for uploading images containing Braille text
 * and receiving predictions from the remote model.
 */
package pl.pb.optigai.utils.api

import okhttp3.MultipartBody
import pl.pb.optigai.utils.data.BrailleResponse
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface IBrailleApi {
    /**
     * Uploads an image containing Braille text to the remote Braille detection API.
     *
     * @param file The image file to upload as a [MultipartBody.Part].
     * @param apiKey API key for authentication.
     * @return [Response] containing a [BrailleResponse] with detected characters.
     *
     * Example usage:
     * ```
     * val response = api.uploadBraille(filePart, "YOUR_API_KEY")
     * if (response.isSuccessful) {
     *     val brailleResponse = response.body()
     * }
     * ```
     */
    @Multipart
    @POST("braille-detection-f0rb5/10")
    suspend fun uploadBraille(
        @Part file: MultipartBody.Part,
        @Query("api_key") apiKey: String,
    ): Response<BrailleResponse>
}
