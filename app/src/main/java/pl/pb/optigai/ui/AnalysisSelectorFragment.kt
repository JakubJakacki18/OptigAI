import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView // <-- Changed from Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import pl.pb.optigai.R
import pl.pb.optigai.ui.AnalysisResultFragment
import pl.pb.optigai.utils.AnalyseService
import pl.pb.optigai.utils.data.AnalysisViewModel

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
            imageView.setImageURI(uri)
        }
        return view
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val buttonTextAnalysis = view.findViewById<TextView>(R.id.analysisTextButton) // <-- Changed to TextView
        val buttonBrailleAnalysis = view.findViewById<TextView>(R.id.analysisBrailleButton) // <-- Changed to TextView
        val buttonItemAnalysis = view.findViewById<TextView>(R.id.analysisItemButton) // <-- Changed to TextView

        buttonTextAnalysis.setOnClickListener {
            viewModel.setResult(AnalyseService.analyseText())
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, AnalysisResultFragment())
                .addToBackStack(null)
                .commit()
        }
        buttonBrailleAnalysis.setOnClickListener {
            viewModel.setResult(AnalyseService.analyseBraille())
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, AnalysisResultFragment())
                .addToBackStack(null)
                .commit()
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