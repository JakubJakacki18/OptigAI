package pl.pb.optigai.ui

import android.graphics.Bitmap
import android.net.Uri
import pl.pb.optigai.utils.data.uriToBitmap
import android.os.Bundle
import android.util.Log
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
import pl.pb.optigai.utils.data.AnalysisViewModel
import pl.pb.optigai.utils.data.BitmapCache
import pl.pb.optigai.utils.data.DetectionData
import java.io.File
import java.io.FileOutputStream

class AnalysisSelectorFragment : Fragment() {
    private val viewModel: AnalysisViewModel by activityViewModels()
    private lateinit var viewBinding: FragmentAnalysisSelectorBinding

    /**
     * UCrop result launcher
     */
    private val cropImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val resultUri = result.data?.let { UCrop.getOutput(it) }
                if (resultUri != null) {
                    Log.d("AnalysisSelector", "Image cropped: $resultUri")

                    // Update ImageView
                    viewBinding.analyzedPhoto.setImageURI(resultUri)

                    // Convert the URI to a bitmap and cache it properly
                    val newBitmap = uriToBitmap(requireContext(), resultUri)
                    if (newBitmap != null) {
                        BitmapCache.bitmap = newBitmap
                        BitmapCache.lastUri = resultUri
                    } else {
                        Log.e("AnalysisSelector", "Failed to decode bitmap from cropped URI")
                    }
                }


            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                val cropError = result.data?.let { UCrop.getError(it) }
                AppLogger.e("UCrop Error: ${cropError?.message}")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = FragmentAnalysisSelectorBinding.inflate(inflater, container, false)

        val imageView: ImageView = viewBinding.analyzedPhoto
        imageView.post {
            Log.d("AnalysisSelector", "ImageView size: ${imageView.width} x ${imageView.height}")
            AnalyseUtils.updateImageView(imageView, null, BitmapCache.bitmap)
        }

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        // ✅ Launch UCrop when edit button is clicked
        viewBinding.SelectorEditButton.setOnClickListener {
            if (BitmapCache.lastUri == null && BitmapCache.bitmap != null) {
                val file = File(requireContext().cacheDir, "temp_image.jpg")
                FileOutputStream(file).use {
                    BitmapCache.bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }
                BitmapCache.lastUri = Uri.fromFile(file)
            }

            val currentUri = BitmapCache.lastUri
            if (currentUri != null) {
                startCropActivity(currentUri)
            } else {
                AppLogger.e("Cannot crop: no image URI found in BitmapCache.")
            }
        }
    }

    /**
     * Starts UCrop for the given source URI.
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

    private fun callAnalyseFunction(analyseFunction: suspend (bitmap: Bitmap) -> DetectionData) {
        if (BitmapCache.bitmap == null) {
            throw IllegalStateException("BitmapCache.bitmap is null")
        }

        showLoadingFragment()
        lifecycleScope.launch {
            val resultOfAnalysis = analyseFunction(BitmapCache.bitmap!!)
            viewModel.setDetectionResult(resultOfAnalysis.detectionResults)
            viewModel.setSummaryTextResult(resultOfAnalysis.result)
            parentFragmentManager.popBackStack()
            showResultFragment()
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
}
