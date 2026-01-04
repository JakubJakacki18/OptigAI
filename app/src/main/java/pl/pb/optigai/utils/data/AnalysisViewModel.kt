/**
 * AnalysisViewModel
 *
 * A [ViewModel] that holds the state of the image analysis process.
 * It manages the photo URI, detection results, summary text, and bitmap status.
 * This allows fragments and activities to observe and react to changes in
 * analysis data in a lifecycle-aware manner.
 */
package pl.pb.optigai.utils.data

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AnalysisViewModel : ViewModel() {
    /**
     * The URI of the photo being analyzed.
     */
    private val _photoUri = MutableLiveData<Uri>()
    val photoUri: LiveData<Uri> get() = _photoUri
    /**
     * List of detection results obtained from analyzing the image.
     */
    private val _analysisDetectionResults = MutableLiveData<List<DetectionResult>>()
    val analysisDetectionResults: LiveData<List<DetectionResult>> get() = _analysisDetectionResults
    /**
     * Summary text describing the analysis result.
     * For example, a concatenated string of detected items or recognized text.
     */
    private val _analysisSummaryTextResult = MutableLiveData<String>()
    val analysisSummaryTextResult: LiveData<String> get() = _analysisSummaryTextResult
    /**
     * Flag indicating whether a bitmap has been passed for analysis.
     */
    val isBitmapPassed = MutableLiveData(false)
    /**
     * Initializes the photo URI if it has not already been set.
     *
     * @param uri The URI of the photo to analyze.
     */
    fun initPhotoUri(uri: Uri) {
        if (_photoUri.value == null) {
            _photoUri.value = uri
        }
    }
    /**
     * Updates the list of detection results.
     *
     * @param result A list of [DetectionResult] objects obtained from analysis.
     */
    fun setDetectionResult(result: List<DetectionResult>) {
        _analysisDetectionResults.value = result
    }
    /**
     * Updates the summary text result of the analysis.
     *
     * @param result A string representing the summary of analysis.
     */
    fun setSummaryTextResult(result: String) {
        _analysisSummaryTextResult.value = result
    }
}
