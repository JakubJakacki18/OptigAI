package pl.pb.optigai.utils.data

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AnalysisViewModel : ViewModel() {

    private val _photoUri = MutableLiveData<Uri>()
    val photoUri: LiveData<Uri> get() = _photoUri

    private val _analysisResults = MutableLiveData<List<DetectionResult>>()
    val analysisResults: LiveData<List<DetectionResult>> get() = _analysisResults

    private val _brailleResult = MutableLiveData<String>()
    val brailleResult: LiveData<String> get() = _brailleResult

    val isBitmapPassed = MutableLiveData(false)

    fun initPhotoUri(uri: Uri) {
        if (_photoUri.value == null) {
            _photoUri.value = uri
        }
    }

    fun setItemResult(result: List<DetectionResult>) {
        _analysisResults.value = result
    }

    fun setBrailleResult(result: String) {
        _brailleResult.value = result
    }
}

