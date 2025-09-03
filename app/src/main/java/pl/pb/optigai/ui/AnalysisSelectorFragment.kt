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
import pl.pb.optigai.utils.AnalyseService
import pl.pb.optigai.utils.AnalyseUtils
import pl.pb.optigai.utils.data.AnalysisViewModel
import pl.pb.optigai.utils.data.BitmapCache

class AnalysisSelectorFragment : Fragment() {
    private val viewModel: AnalysisViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analysis_selector, container, false)
        val imageView: ImageView = view.findViewById(R.id.analyzedPhoto)
        viewModel.photoUri.observe(viewLifecycleOwner) { uri ->
            AnalyseUtils.updateImageView(imageView, uri, null)
        }
        viewModel.isBitmapPassed.observe(viewLifecycleOwner) { isBitmapPassed ->
            if (!isBitmapPassed) return@observe
            AnalyseUtils.updateImageView(imageView, null, BitmapCache.bitmap)
        }
        return view
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val buttonTextAnalysis = view.findViewById<TextView>(R.id.analysisTextButton)
        val buttonBrailleAnalysis = view.findViewById<TextView>(R.id.analysisBrailleButton)
        val buttonItemAnalysis = view.findViewById<TextView>(R.id.analysisItemButton)

        // Tworzymy instancję klasy, aby móc wywoływać jej metody.
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
            // Uruchamiamy korutynę, aby móc wywołać funkcję suspend
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
            val result = analyseService.analyseItem()
            viewModel.setResult(result)
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, AnalysisResultFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
