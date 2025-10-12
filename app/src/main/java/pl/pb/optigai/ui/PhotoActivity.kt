package pl.pb.optigai.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.yalantis.ucrop.UCrop
import pl.pb.optigai.R
import pl.pb.optigai.databinding.PhotoPreviewBinding
import pl.pb.optigai.utils.PhotoUtils
import pl.pb.optigai.utils.data.Image
import java.io.File

class PhotoActivity : AppCompatActivity() {
    private lateinit var images: List<Image>
    private var currentIndex: Int = 0
    private lateinit var viewBinding: PhotoPreviewBinding

    /**
     * Launcher for the cropping activity.
     */
    private val cropImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val resultUri = result.data?.let { UCrop.getOutput(it) }

            if (resultUri != null) {
                val currentImage = images[currentIndex]
                val newImage = currentImage.copy(uri = resultUri)

                images = images.toMutableList().also {
                    it[currentIndex] = newImage
                }.toList()

                viewBinding.previewImageView.setImageURI(resultUri)
                updateUndoButtonState()
                Log.d("PhotoActivity", "Image cropped and list updated. New URI: $resultUri")
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = result.data?.let { UCrop.getError(it) }
            Log.e("PhotoActivity", "UCrop Error: ${cropError?.message}")
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = PhotoPreviewBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val initialImages = PhotoUtils.imageReader(this@PhotoActivity)
        images = initialImages.map {
            Image(uri = it.uri, originalUri = it.uri)
        }
        currentIndex = intent.getIntExtra("position", 0)

        updateImage()

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

        viewBinding.editButton.setOnClickListener {
            startCropActivity(images[currentIndex].uri)
        }

        viewBinding.undoButton.setOnClickListener {
            revertImageChanges()
        }

        val headerTitle: TextView = findViewById(R.id.headerTitle)
        headerTitle.text = getString(R.string.preview_header_shared)

        val backButton: View = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun updateImage() {
        val currentImage = images[currentIndex]
        viewBinding.previewImageView.setImageURI(currentImage.uri)
        updateNavigationButtons()
        // NEW: Update undo button visibility/state
        updateUndoButtonState()
    }

    private fun updateNavigationButtons() {
        viewBinding.leftArrow.isEnabled = currentIndex > 0
        viewBinding.rightArrow.isEnabled = currentIndex < images.size - 1
    }

    private fun revertImageChanges() {
        val currentImage = images[currentIndex]

        // Use the stored originalUri to revert the changes
        if (currentImage.uri != currentImage.originalUri) {

            val revertedImage = currentImage.copy(uri = currentImage.originalUri)

            images = images.toMutableList().also {
                it[currentIndex] = revertedImage
            }.toList()

            viewBinding.previewImageView.setImageURI(revertedImage.uri)
            updateUndoButtonState()
            Log.d("PhotoActivity", "Image changes reverted to original URI.")
        }
    }

    /**
     * Toggles the state of the undo button based on whether the image has been cropped.
     * Assumes 'undoButton' is an ID in your PhotoPreviewBinding.
     */
    private fun updateUndoButtonState() {
        val currentImage = images[currentIndex]
        val isChanged = currentImage.uri != currentImage.originalUri
        viewBinding.undoButton.isEnabled = isChanged
        viewBinding.undoButton.visibility = if (isChanged) View.VISIBLE else View.INVISIBLE
    }
    // ----------------------------------------------------


    /**
     * Starts the UCrop activity for the given image URI with custom styling.
     */
    private fun startCropActivity(sourceUri: Uri) {
        val outputFileName = "cropped_image_${System.currentTimeMillis()}.jpg"
        val destinationUri = Uri.fromFile(File(cacheDir, outputFileName))

        val options = UCrop.Options()

        val primaryColor = ContextCompat.getColor(this, R.color.light_blue)
        val toolbarIconColor = ContextCompat.getColor(this, R.color.creme)
        val activeAccentColor = ContextCompat.getColor(this, R.color.light_blue)

        options.setToolbarColor(primaryColor)
        options.setStatusBarColor(primaryColor)
        options.setToolbarWidgetColor(toolbarIconColor)
        options.setActiveControlsWidgetColor(activeAccentColor)
        options.setCropFrameColor(toolbarIconColor)
        options.setDimmedLayerColor(ContextCompat.getColor(this, R.color.dark_blue))

        val uCrop = UCrop.of(sourceUri, destinationUri)
            .withOptions(options)

        cropImageLauncher.launch(uCrop.getIntent(this))
    }
}