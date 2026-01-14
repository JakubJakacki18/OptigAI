package pl.pb.optigai.ui

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import pl.pb.optigai.R
import pl.pb.optigai.databinding.FragmentAnalysisSelectorBinding
import pl.pb.optigai.utils.AnalyseService
import pl.pb.optigai.utils.AnalyseUtils
import pl.pb.optigai.utils.AppLogger
import pl.pb.optigai.utils.PhotoUtils
import pl.pb.optigai.utils.data.AnalysisViewModel
import pl.pb.optigai.utils.data.BitmapCache
import pl.pb.optigai.utils.data.DetectionData
import java.io.File

/**
 * AnalysisSelectorFragment
 *
 * Fragment responsible for allowing the user to select and initiate different types of image analysis.
 * It also supports cropping the image before analysis and displaying the updated bitmap.
 *
 * Features:
 * - Displays the currently analyzed image from [BitmapCache].
 * - Provides buttons to perform text analysis, Braille analysis, and object detection via [AnalyseService].
 * - Allows editing (cropping) of the image using UCrop before running analysis.
 * - Updates the shared [AnalysisViewModel] with detection results and summary text.
 * - Shows a loading fragment while analysis is in progress and then transitions to [AnalysisResultFragment].
 *
 * Collaborates with:
 * - [AnalyseService] – performs the actual analysis (text, Braille, object detection).
 * - [BitmapCache] – stores the current bitmap and the last image URI.
 * - [PhotoUtils] – utility functions for converting between bitmap and URI.
 * - [AnalysisViewModel] – shares detection results and summary text across fragments.
 * - [UCrop] – provides cropping functionality.
 * - [AnalyseUtils] – updates the displayed bitmap in the ImageView.
 * - [LoadingFragment] – temporary fragment shown while analysis is running.
 * - [AnalysisResultFragment] – fragment displaying final analysis results.
 */
class AnalysisSelectorFragment : Fragment() {
    /** Shared ViewModel for managing analysis data across the activity. */
    private val viewModel: AnalysisViewModel by activityViewModels()

    /** View binding for the fragment layout. */
    private lateinit var viewBinding: FragmentAnalysisSelectorBinding

    /**
     * Activity result launcher for cropping images with UCrop.
     * Updates [BitmapCache] and the ImageView when the cropping is complete.
     */
    private val cropImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val resultUri = result.data?.let { UCrop.getOutput(it) }
                if (resultUri != null) {
                    AppLogger.d("Image cropped: $resultUri")
                    val newBitmap = PhotoUtils.convertUriToBitmap(requireContext(), resultUri)
                    if (newBitmap != null) {
                        BitmapCache.bitmap = newBitmap
                        BitmapCache.lastUri = resultUri
                        viewBinding.analyzedPhoto.setImageBitmap(null)
                        viewBinding.analyzedPhoto.setImageBitmap(newBitmap)
                    } else {
                        AppLogger.e("Failed to decode bitmap from cropped URI")
                    }
                }
            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                val cropError = result.data?.let { UCrop.getError(it) }
                AppLogger.e("UCrop Error: ${cropError?.message}")
            }
        }

    /**
     * Called to create and return the view hierarchy associated with the fragment.
     * Sets up the analyzed image ImageView and ensures the bitmap is displayed properly.
     *
     * @param inflater LayoutInflater to inflate the fragment layout.
     * @param container Optional parent view to attach the fragment to.
     * @param savedInstanceState Saved state bundle.
     * @return The root view of the fragment's layout.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = FragmentAnalysisSelectorBinding.inflate(inflater, container, false)

        val imageView: ImageView = viewBinding.analyzedPhoto
        imageView.post {
            AppLogger.d("ImageView size: ${imageView.width} x ${imageView.height}")
            AnalyseUtils.updateImageView(imageView, null, BitmapCache.bitmap)
        }

        return viewBinding.root
    }

    /**
     * Called immediately after onCreateView.
     * Sets up click listeners for analysis buttons and the edit (crop) button.
     * Invokes analysis functions from [AnalyseService] and updates the [AnalysisViewModel] with results.
     *
     * @param view The fragment's root view.
     * @param savedInstanceState Saved state bundle.
     */
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val analyseService = AnalyseService(requireContext())

        viewBinding.analysisTextButton.setOnClickListener {
            callAnalyseFunction(analyseService::analyseText)
        }

        viewBinding.analysisBrailleButton.setOnClickListener {
            callAnalyseFunction(analyseService::analyseBraille)
        }

        viewBinding.analysisItemButton.setOnClickListener {
            callAnalyseFunction(analyseService::analyseItem)
        }

        viewBinding.SelectorEditButton.setOnClickListener {
            val bitmap = BitmapCache.bitmap
            if (bitmap == null) {
                AppLogger.e("Cannot crop: bitmap is null")
                return@setOnClickListener
            }
            val freshUri = PhotoUtils.convertBitmapToUri(requireContext(), bitmap)
            BitmapCache.lastUri = freshUri
            startCropActivity(freshUri)
        }
    }

    /**
     * Starts the UCrop activity to crop the given [sourceUri].
     * Configures colors and UI options for the cropping toolbar.
     *
     * @param sourceUri URI of the image to crop.
     */
    private fun startCropActivity(sourceUri: Uri) {
        val outputFileName = "cropped_image_${System.currentTimeMillis()}.jpg"
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, outputFileName))

        val options = UCrop.Options()
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.light_blue)
        val toolbarIconColor = ContextCompat.getColor(requireContext(), R.color.creme)
        val dimmedLayerColor = ContextCompat.getColor(requireContext(), R.color.dark_blue)

        options.setToolbarColor(primaryColor)
        options.setToolbarWidgetColor(toolbarIconColor)
        options.setActiveControlsWidgetColor(primaryColor)
        options.setCropFrameColor(toolbarIconColor)
        options.setDimmedLayerColor(dimmedLayerColor)

        val uCrop = UCrop.of(sourceUri, destinationUri).withOptions(options)
        cropImageLauncher.launch(uCrop.getIntent(requireContext()))
    }

    /**
     * Throws an [IllegalStateException] if [BitmapCache.bitmap] is null.
     * Used to ensure that a valid bitmap exists before performing analysis.
     */
    private fun throwExceptionIfBitmapIsNull() {
        if (BitmapCache.bitmap == null) {
            throw IllegalStateException("BitmapCache.bitmap is null")
        }
    }

    /**
     * Helper function to call a suspend analysis function (text, Braille, or item detection) on the current bitmap.
     * Shows the loading fragment during analysis and then navigates to [AnalysisResultFragment].
     *
     * @param analyseFunction Suspend function taking a [Bitmap] and returning [DetectionData].
     */
    private fun callAnalyseFunction(analyseFunction: suspend (bitmap: Bitmap) -> DetectionData) {
        throwExceptionIfBitmapIsNull()
        showLoadingFragment()
        lifecycleScope.launch {
            val resultOfAnalysis = analyseFunction(BitmapCache.bitmap!!)
            viewModel.setDetectionResult(resultOfAnalysis.detectionResults)
            viewModel.setSummaryTextResult(resultOfAnalysis.result)
            parentFragmentManager.popBackStack()
            showResultFragment()
        }
    }

    /**
     * Replaces the current fragment with [LoadingFragment] and adds the transaction to the back stack.
     */
    private fun showLoadingFragment() {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, LoadingFragment())
            .addToBackStack(null)
            .commit()
    }

    /**
     * Replaces the current fragment with [AnalysisResultFragment] and adds the transaction to the back stack.
     */
    private fun showResultFragment() {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, AnalysisResultFragment())
            .addToBackStack(null)
            .commit()
    }
}
