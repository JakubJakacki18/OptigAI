package pl.pb.optigai.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.pb.optigai.utils.data.BitmapCache

class AnalyseService(
    private val context: Context,
) {
    fun analyseText(): String = "Text recognition not yet implemented"

    suspend fun analyseBraille(): String =
        withContext(Dispatchers.Default) {
            val bitmap = BitmapCache.bitmap ?: return@withContext "Brak obrazu"
            val recognizer = BrailleRecognizer(context)

            val result = recognizer.recognizeText(bitmap)
            recognizer.close()
            result
        }

    fun analyseItem(): String = "Item recognition not yet implemented"
}
