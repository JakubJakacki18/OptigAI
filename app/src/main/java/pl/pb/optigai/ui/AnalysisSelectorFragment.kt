package pl.pb.optigai.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pl.pb.optigai.R
import pl.pb.optigai.databinding.FragmentAnalysisSelectorBinding
import pl.pb.optigai.utils.AnalyseService
import pl.pb.optigai.utils.AnalyseUtils
import pl.pb.optigai.utils.data.AnalysisViewModel
import pl.pb.optigai.utils.data.BitmapCache
import pl.pb.optigai.utils.data.DetectionResult

class AnalysisSelectorFragment : Fragment() {
    private val viewModel: AnalysisViewModel by activityViewModels()
    private lateinit var viewBinding: FragmentAnalysisSelectorBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        viewBinding = FragmentAnalysisSelectorBinding.inflate(inflater, container, false)
        val imageView: ImageView = viewBinding.analyzedPhoto
        imageView.post {
            Log.d("AnalysisSelector", "ImageView size: ${imageView.width} x ${imageView.height}")
            AnalyseUtils.updateImageView(imageView, null, BitmapCache.bitmap)
        }

        return viewBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val buttonTextAnalysis = viewBinding.analysisTextButton
        val buttonBrailleAnalysis = viewBinding.analysisBrailleButton
        val buttonItemAnalysis = viewBinding.analysisItemButton

        val analyseService = AnalyseService(requireContext())

        buttonTextAnalysis.setOnClickListener {
            throwExceptionIfBitmapIsNull()
            showLoadingFragment()
            viewModel.setDetectionResult(emptyList())
            lifecycleScope.launch {
                val resultOfAnalysis = analyseService.analyseText(BitmapCache.bitmap!!)
                viewModel.setDetectionResult(resultOfAnalysis)
                viewModel.setSummaryTextResult(getResultSummaryText(resultOfAnalysis))
                parentFragmentManager.popBackStack()
                showResultFragment()
            }
        }

        buttonBrailleAnalysis.setOnClickListener {
            throwExceptionIfBitmapIsNull()
            showLoadingFragment()
            viewModel.setDetectionResult(emptyList())
            lifecycleScope.launch {
                val resultOfAnalysis = analyseService.analyseBraille(BitmapCache.bitmap!!)
                viewModel.setSummaryTextResult((resultOfAnalysis))
                parentFragmentManager.popBackStack()
                showResultFragment()
            }
        }

        buttonItemAnalysis.setOnClickListener {
            throwExceptionIfBitmapIsNull()
            // showLoadingFragment()
            val resultOfAnalysis = analyseService.analyseItem(BitmapCache.bitmap!!)

            viewModel.setDetectionResult(resultOfAnalysis)
            viewModel.setSummaryTextResult(getResultSummaryText(resultOfAnalysis))
            showResultFragment()
        }
    }

    private fun throwExceptionIfBitmapIsNull() {
        if (BitmapCache.bitmap == null) {
            throw IllegalStateException("BitmapCache.bitmap is null")
        }
    }

    private fun showLoadingFragment() {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, LoadingFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun showResultFragment() {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, AnalysisResultFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun getResultSummaryText(detectionResult: List<DetectionResult>): String =
        if (detectionResult.isNotEmpty()) {
            detectionResult.joinToString(separator = "\n") {
                it.text
            }
        } else {
            R.string.empty_result_fragment_analysis_result.toString()
        }
}
