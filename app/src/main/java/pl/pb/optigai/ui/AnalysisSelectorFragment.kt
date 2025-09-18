package pl.pb.optigai.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import pl.pb.optigai.R
import pl.pb.optigai.databinding.FragmentAnalysisSelectorBinding
import pl.pb.optigai.utils.AnalyseService
import pl.pb.optigai.utils.AnalyseUtils
import pl.pb.optigai.utils.data.AnalysisViewModel
import pl.pb.optigai.utils.data.BitmapCache

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
//            val result = analyseService.analyseText()
//            viewModel.setResult(result)
            viewModel.setDetectionResult(emptyList())
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, AnalysisResultFragment())
                .addToBackStack(null)
                .commit()
        }

        buttonBrailleAnalysis.setOnClickListener {
            val bitmap = BitmapCache.bitmap
            if (bitmap != null) {
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, LoadingFragment())
                    .addToBackStack(null)
                    .commit()
                viewModel.setDetectionResult(emptyList())
                view.post {
                    analyseService.analyseBraille(bitmap) { sentence ->
                        requireActivity().runOnUiThread {
                            parentFragmentManager.popBackStack()
                            viewModel.setSummaryTextResult(sentence)
                            parentFragmentManager
                                .beginTransaction()
                                .replace(R.id.fragmentContainer, AnalysisResultFragment())
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                }
            }
        }

        buttonItemAnalysis.setOnClickListener {
            throwExceptionIfBitmapIsNull()
            // showLoadingFragment()
            val resultOfAnalysis = analyseService.analyseItem(BitmapCache.bitmap!!)

            viewModel.setDetectionResult(resultOfAnalysis)
            val resultSummary =
                if (resultOfAnalysis.isNotEmpty()) {
                    resultOfAnalysis.joinToString(separator = "\n") {
                        it.text
                    }
                } else {
                    R.string.empty_result_fragment_analysis_result.toString()
                }
            viewModel.setSummaryTextResult(resultSummary)
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, AnalysisResultFragment())
                .addToBackStack(null)
                .commit()
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
}
