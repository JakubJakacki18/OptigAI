package pl.pb.optigai.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import pl.pb.optigai.R
import pl.pb.optigai.databinding.PhotoPreviewBinding
import pl.pb.optigai.utils.AppLogger
import pl.pb.optigai.utils.PhotoUtils
import pl.pb.optigai.utils.data.Image
import androidx.core.net.toUri

class PhotoActivity : AppCompatActivity() {
    private lateinit var images: List<Image>
    private var currentIndex: Int = 0
    private lateinit var viewBinding: PhotoPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = PhotoPreviewBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val initialImages = PhotoUtils.imageReader(this@PhotoActivity)
        images =
            initialImages.map {
                Image(uri = it.uri, originalUri = it.uri)
            }
        currentIndex = intent.getIntExtra("position", 0)

        updateImage()
        bindHeaderLayout()
        bindPreviewButtons()
        bindCropperButtons()
    }

    /**
     * Launcher for the custom image editor activity.
     */
    private fun startCustomEditor(sourceUri: Uri) {
        val intent = Intent(this, ImageEditorActivity::class.java)
        intent.putExtra("IMAGE_URI", sourceUri.toString())
        cropImageLauncher.launch(intent)
    }

    private val cropImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val resultUriString = result.data?.getStringExtra("CROPPED_URI")
                if (resultUriString != null) {
                    val resultUri = resultUriString.toUri()
                    val currentImage = images[currentIndex]

                    try {
                        val updatedImage = currentImage.copy(uri = resultUri)
                        images = images.toMutableList().also { it[currentIndex] = updatedImage }

                        viewBinding.previewImageView.setImageURI(null)
                        viewBinding.previewImageView.setImageURI(resultUri)
                        updateUndoButtonState()

                        AppLogger.d("✅ Cropped image loaded from temp URI: $resultUri")
                    } catch (e: Exception) {
                        AppLogger.e( "Failed to update image URI after crop: ${e.message}")
                    }
                } else {
                    AppLogger.e("No cropped URI returned.")
                }
            }
        }

    private fun updateImage() {
        val currentImage = images[currentIndex]
        viewBinding.previewImageView.setImageURI(currentImage.uri)
        updateNavigationButtons()
        updateUndoButtonState()
    }

    private fun updateNavigationButtons() {
        viewBinding.leftArrow.isEnabled = currentIndex > 0
        viewBinding.rightArrow.isEnabled = currentIndex < images.size - 1
    }

    private fun revertImageChanges() {
        val currentImage = images[currentIndex]
        if (currentImage.uri != currentImage.originalUri) {
            val revertedImage = currentImage.copy(uri = currentImage.originalUri)

            images =
                images
                    .toMutableList()
                    .also {
                        it[currentIndex] = revertedImage
                    }.toList()

            viewBinding.previewImageView.setImageURI(revertedImage.uri)
            updateUndoButtonState()
            AppLogger.d("Image changes reverted to original URI.")
        }
    }

    /**
     * Toggles the state of the undo button based on whether the image has been cropped.
     */
    private fun updateUndoButtonState() {
        val currentImage = images[currentIndex]
        val isChanged = currentImage.uri != currentImage.originalUri
        viewBinding.undoButton.isEnabled = isChanged
        viewBinding.undoButton.visibility = if (isChanged) View.VISIBLE else View.INVISIBLE
    }

    /**
     * NOTE: This method is not currently used, but is left for reference.
     * It was using UCrop, which has been replaced by ImageEditorActivity.
     */
    /*
    private fun startCropActivity(sourceUri: Uri) {
        val outputFileName = "cropped_image_${System.currentTimeMillis()}.jpg"
        val destinationUri = Uri.fromFile(File(cacheDir, outputFileName))

        val options = UCrop.Options()

        val primaryColor = ContextCompat.getColor(this, R.color.light_blue)
        val toolbarIconColor = ContextCompat.getColor(this, R.color.creme)
        val dimmedLayerColor = ContextCompat.getColor(this, R.color.dark_blue)

        options.setToolbarColor(primaryColor)
        options.setToolbarWidgetColor(toolbarIconColor)
        options.setActiveControlsWidgetColor(primaryColor)
        options.setCropFrameColor(toolbarIconColor)
        options.setDimmedLayerColor(dimmedLayerColor)

        val uCrop =
            UCrop
                .of(sourceUri, destinationUri)
                .withOptions(options)

        cropImageLauncher.launch(uCrop.getIntent(this))
    }
    */

    private fun bindHeaderLayout() {
        viewBinding.headerLayout.headerTitle.text = getString(R.string.preview_header_shared)
        viewBinding.headerLayout.backButton.setOnClickListener {
            finish()
        }
    }

    private fun bindPreviewButtons() {
        viewBinding.leftArrow.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                updateImage()
            }
        }

        viewBinding.rightArrow.setOnClickListener {
            if (currentIndex < images.size - 1) {
                currentIndex++
                updateImage()
            }
        }

        viewBinding.middleButton.setOnClickListener {
            val currentImage = images[currentIndex]
            val intent = Intent(this@PhotoActivity, AnalysisActivity::class.java)
            intent.putExtra("IMAGE_URI", currentImage.uri.toString())
            startActivity(intent)
        }
    }

    private fun bindCropperButtons() {
        viewBinding.editButton.setOnClickListener {
            startCustomEditor(images[currentIndex].uri)
        }

        viewBinding.undoButton.setOnClickListener {
            revertImageChanges()
        }
    }
}
