package pl.pb.optigai.utils.data

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AnalysisViewModel : ViewModel() {
    private val _photoUri = MutableLiveData<Uri>()
    val photoUri: LiveData<Uri> get() = _photoUri

    private val _analysisResult = MutableLiveData<String>()
    val analysisResult: LiveData<String> get() = _analysisResult

    val isBitmapPassed = MutableLiveData(false)

    fun initPhotoUri(uri: Uri) {
        if (_photoUri.value == null) {
            _photoUri.value = uri
        }
    }

    fun setResult(result: String) {
        _analysisResult.value = result
    }
}
