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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import pl.pb.optigai.R
import pl.pb.optigai.utils.AnalyseUtils
import pl.pb.optigai.utils.data.AnalysisViewModel
import pl.pb.optigai.utils.data.BitmapCache
import pl.pb.optigai.utils.data.ColorMap
import pl.pb.optigai.utils.data.SettingsViewModel
import kotlin.getValue

class AnalysisResultFragment : Fragment() {
    private val viewModel: AnalysisViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analysis_result, container, false)
        val imageView: ImageView = view.findViewById(R.id.analyzedPhoto)
        val overlay: DetectionOverlay = view.findViewById(R.id.overlay)

        AnalyseUtils.updateImageView(imageView, null, BitmapCache.bitmap)

        val summaryResultText: TextView = view.findViewById(R.id.summaryResultText)
        viewModel.analysisDetectionResults.observe(viewLifecycleOwner) { result ->
            lifecycleScope.launch {
                settingsViewModel.colors.collect { colorEnums ->
                    val availableColorsResId = colorEnums.map { ColorMap.getColorRes(it) }
                    val availableColors = availableColorsResId.map { requireContext().getColor(it) }
                    overlay.setAvailableColors(availableColors)
                    overlay.setDetections(result, BitmapCache.bitmap!!.width, BitmapCache.bitmap!!.height, imageView)
                }
            }
        }
        viewModel.analysisSummaryTextResult.observe(viewLifecycleOwner) { summary ->
            summaryResultText.text = summary
        }
        val scrollView = view.findViewById<NestedScrollView>(R.id.resultScrollView)
        val bottomSheetBehavior = BottomSheetBehavior.from(scrollView)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.peekHeight = 700
        return view
    }
}
