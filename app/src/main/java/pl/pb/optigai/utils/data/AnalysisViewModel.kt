package pl.pb.optigai.utils.data

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AnalysisViewModel : ViewModel() {
    private val _photoUri = MutableLiveData<Uri>()
    val photoUri: LiveData<Uri> get() = _photoUri

    val analysisResult = MutableLiveData<String>()

    fun initPhotoUri(uri: Uri) {
        if (_photoUri.value == null) {
            _photoUri.value = uri
        }
    }
}
