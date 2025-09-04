package pl.pb.optigai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
        AnalyseUtils.updateImageView(imageView, null, BitmapCache.bitmap)
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
            val result = analyseService.analyseText()
            viewModel.setResult(result)
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, AnalysisResultFragment())
                .addToBackStack(null)
                .commit()
        }

        buttonBrailleAnalysis.setOnClickListener {
            lifecycleScope.launch {
                val result = analyseService.analyseBraille()
                viewModel.setResult(result)

                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, AnalysisResultFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }

        buttonItemAnalysis.setOnClickListener {
            if (BitmapCache.bitmap == null) {
                throw IllegalStateException("BitmapCache.bitmap is null")
            }
            val result = analyseService.analyseItem(BitmapCache.bitmap!!)
            viewModel.setResult(result)
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, AnalysisResultFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
