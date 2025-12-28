import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import android.net.Uri
import pl.pb.optigai.utils.data.AnalysisViewModel
import pl.pb.optigai.utils.data.DetectionResult
class AnalysisViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun initPhotoUri_setsUriOnlyOnce() {
        val viewModel = AnalysisViewModel()
        val uri = mock(Uri::class.java)

        viewModel.initPhotoUri(uri)
        viewModel.initPhotoUri(mock(Uri::class.java))

        assertEquals(uri, viewModel.photoUri.value)
    }

    @Test
    fun setDetectionResult_updatesLiveData() {
        val viewModel = AnalysisViewModel()
        val list = emptyList<DetectionResult>()

        viewModel.setDetectionResult(list)

        assertEquals(list, viewModel.analysisDetectionResults.value)
    }

    @Test
    fun setSummaryTextResult_updatesLiveData() {
        val viewModel = AnalysisViewModel()
        val text = "Test summary"

        viewModel.setSummaryTextResult(text)

        assertEquals(text, viewModel.analysisSummaryTextResult.value)
    }
    @Test
    fun initPhotoUri_doesNotOverrideExistingValue() {
        val viewModel = AnalysisViewModel()
        val firstUri = mock(Uri::class.java)

        viewModel.initPhotoUri(firstUri)
        viewModel.initPhotoUri(mock(Uri::class.java))

        assertEquals(firstUri, viewModel.photoUri.value)
    }

}
