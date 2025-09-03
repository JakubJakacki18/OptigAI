package pl.pb.optigai.utils

import android.content.Context
import pl.pb.optigai.ui.BrailleRecognizer
import pl.pb.optigai.utils.data.BitmapCache

class AnalyseService(private val context: Context) {

    fun analyseText(): String {
        return "Text recognition not yet implemented"
    }

    suspend fun analyseBraille(): String {
        val bitmap = BitmapCache.bitmap ?: return "Brak obrazu"
        val recognizer = BrailleRecognizer(context)

        val result = recognizer.recognizeText(bitmap)
        recognizer.close()
        return result
    }

    fun analyseItem(): String {
        return "Item recognition not yet implemented"
    }
}
