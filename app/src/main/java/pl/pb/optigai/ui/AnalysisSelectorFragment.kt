import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import pl.pb.optigai.R
import pl.pb.optigai.ui.AnalysisResultFragment
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

        buttonTextAnalysis.setOnClickListener {
            viewModel.setResult(AnalyseService.analyseText())
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, AnalysisResultFragment())
                .addToBackStack(null)
                .commit()
        }
        buttonBrailleAnalysis.setOnClickListener {
            val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
            progressBar.visibility = View.VISIBLE

            val bitmap = BitmapCache.bitmap
            Log.d("BrailleAnalysis", "Bitmap is null? ${bitmap == null}")
            if (bitmap != null) {
                AnalyseService.analyseBraille(bitmap) { sentence ->
                    requireActivity().runOnUiThread {
                        progressBar.visibility = View.GONE
                        viewModel.setResult(sentence)
                        parentFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, AnalysisResultFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                }
            } else {
                // Hide progress bar if bitmap missing
                progressBar.visibility = View.GONE
            }
        }



        buttonItemAnalysis.setOnClickListener {
            viewModel.setResult(AnalyseService.analyseItem())
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, AnalysisResultFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
