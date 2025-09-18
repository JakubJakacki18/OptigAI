package pl.pb.optigai.utils.data

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AnalysisViewModel : ViewModel() {
    private val _photoUri = MutableLiveData<Uri>()
    val photoUri: LiveData<Uri> get() = _photoUri

    private val _analysisDetectionResults = MutableLiveData<List<DetectionResult>>()
    val analysisDetectionResults: LiveData<List<DetectionResult>> get() = _analysisDetectionResults

    private val _analysisSummaryTextResult = MutableLiveData<String>()
    val analysisSummaryTextResult: LiveData<String> get() = _analysisSummaryTextResult

    val isBitmapPassed = MutableLiveData(false)

    fun initPhotoUri(uri: Uri) {
        if (_photoUri.value == null) {
            _photoUri.value = uri
        }
    }

    fun setDetectionResult(result: List<DetectionResult>) {
        _analysisDetectionResults.value = result
    }

    fun setSummaryTextResult(result: String) {
        _analysisSummaryTextResult.value = result
    }
}
