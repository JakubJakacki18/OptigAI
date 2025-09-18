package pl.pb.optigai.utils.api

import okhttp3.MultipartBody
import pl.pb.optigai.utils.data.BrailleResponse
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface IBrailleApi {
    @Multipart
    @POST("braille-detection-f0rb5/10")
    suspend fun uploadBraille(
        @Part file: MultipartBody.Part,
        @Query("api_key") apiKey: String,
    ): Response<BrailleResponse>
}
