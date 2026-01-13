package pl.pb.optigai.ui

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
import pl.pb.optigai.Settings
import pl.pb.optigai.utils.AnalyseUtils
import pl.pb.optigai.utils.data.AnalysisViewModel
import pl.pb.optigai.utils.data.BitmapCache
import pl.pb.optigai.utils.data.SettingsViewModel
import pl.pb.optigai.utils.data.const.ColorMap
import kotlin.getValue

/**
 * AnalysisResultFragment
 *
 * Fragment responsible for displaying the results of an image analysis.
 * This includes showing the analyzed image, overlaying detected objects with bounding boxes,
 * and presenting a textual summary of the detection results.
 *
 * Features:
 * - Displays the analyzed image from [BitmapCache].
 * - Observes detection results from [AnalysisViewModel] and renders them on [DetectionOverlay].
 * - Uses user settings from [SettingsViewModel] to customize font size and bounding box colors.
 * - Supports a BottomSheet to display textual analysis results.
 * - Automatically updates overlay and summary when detection results or settings change.
 *
 * Collaborates with:
 * - [AnalysisViewModel] – provides analysis detection results and summary text.
 * - [SettingsViewModel] – provides user settings like font size and border colors.
 * - [BitmapCache] – stores the analyzed bitmap image.
 * - [AnalyseUtils] – helper for updating the displayed image.
 * - [DetectionOverlay] – custom view for rendering detection bounding boxes.
 * - [ColorMap] – maps color enums to actual color resources.
 */
class AnalysisResultFragment : Fragment() {
    /** Shared ViewModel for managing analysis data across the activity. */
    private val viewModel: AnalysisViewModel by activityViewModels()

    /** ViewModel for storing and observing user settings (font size, border colors). */
    private val settingsViewModel: SettingsViewModel by viewModels()

    /**
     * Called to create and return the view hierarchy associated with the fragment.
     * Sets up:
     * - ImageView for displaying the analyzed bitmap.
     * - DetectionOverlay for rendering detected objects.
     * - BottomSheet for textual summary of analysis.
     * - Observers for analysis results and user settings.
     *
     * @param inflater LayoutInflater to inflate fragment layout.
     * @param container Optional parent view to attach the fragment to.
     * @param savedInstanceState Saved state bundle.
     * @return The root view of the fragment's layout.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analysis_result, container, false)
        val imageView: ImageView = view.findViewById(R.id.analyzedPhoto)
        val overlay: DetectionOverlay = view.findViewById(R.id.overlay)
        val summaryResultText: TextView = view.findViewById(R.id.summaryResultText)
        val scrollView = view.findViewById<NestedScrollView>(R.id.resultScrollView)

        val bottomSheetBehavior = BottomSheetBehavior.from(scrollView)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.peekHeight = 700

        AnalyseUtils.updateImageView(imageView, null, BitmapCache.bitmap)

        viewModel.analysisDetectionResults.observe(viewLifecycleOwner) { result ->
            lifecycleScope.launch {
                settingsViewModel.colors.collect { colorEnums ->
                    val availableColors = getAvailableColors(colorEnums)
                    overlay.setAvailableColors(availableColors)
                    overlay.setDetections(result, BitmapCache.bitmap!!.width, BitmapCache.bitmap!!.height, imageView)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            settingsViewModel.fontSizeSp.collect { sp ->
                summaryResultText.textSize = sp.toFloat()
            }
        }
        viewModel.analysisSummaryTextResult.observe(viewLifecycleOwner) { summary ->
            summaryResultText.text = summary
        }
        return view
    }

    /**
     * Maps the list of user-selected color enums to actual Android color integers.
     *
     * @param colorEnums List of [Settings.ColorOfBorder] selected by the user.
     * @return List of integer color values corresponding to the selected colors.
     */
    private fun getAvailableColors(colorEnums: List<Settings.ColorOfBorder>): List<Int?> {
        val availableColorsResId = colorEnums.map { ColorMap.getColorRes(it) }
        val availableColors = availableColorsResId.map { requireContext().getColor(it) }
        return availableColors
    }
}
