package pl.pb.optigai.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import pl.pb.optigai.R
import pl.pb.optigai.utils.AnalyseUtils
import pl.pb.optigai.utils.data.AnalysisViewModel
import pl.pb.optigai.utils.data.BitmapCache
import kotlin.getValue

class AnalysisResultFragment : Fragment() {
    private val viewModel: AnalysisViewModel by activityViewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analysis_result, container, false)
        val imageView: ImageView = view.findViewById(R.id.analyzedPhoto)
        viewModel.photoUri.observe(viewLifecycleOwner) { uri ->
            AnalyseUtils.updateImageView(imageView, uri, null)
        }
        viewModel.isBitmapPassed.observe(viewLifecycleOwner) { isBitmapPassed ->
            if (!isBitmapPassed) return@observe
            AnalyseUtils.updateImageView(imageView, null, BitmapCache.bitmap)
        }
        val resultText: TextView = view.findViewById(R.id.resultText)
        viewModel.analysisResult.observe(viewLifecycleOwner) { result ->
            resultText.text = result
        }

        val scrollView = view.findViewById<NestedScrollView>(R.id.resultScrollView)
        val bottomSheetBehavior = BottomSheetBehavior.from(scrollView)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.peekHeight = 700
        return view
    }
}
