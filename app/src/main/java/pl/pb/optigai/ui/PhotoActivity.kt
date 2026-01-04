/**
 * PhotoActivity
 *
 * Activity used to display a single image from the device's gallery with navigation controls.
 * Supports scrolling through images, previewing them, and launching the analysis workflow.
 *
 * @property images List of [Image] objects read from the device storage.
 * @property currentIndex Index of the currently displayed image in [images].
 * @property viewBinding View binding for [PhotoPreviewBinding].
 */
package pl.pb.optigai.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pl.pb.optigai.R
import pl.pb.optigai.databinding.PhotoPreviewBinding
import pl.pb.optigai.utils.PhotoUtils
import pl.pb.optigai.utils.data.Image

class PhotoActivity : AppCompatActivity() {
    private lateinit var images: List<Image>
    private var currentIndex: Int = 0
    private lateinit var viewBinding: PhotoPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = PhotoPreviewBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        images = PhotoUtils.imageReader(this@PhotoActivity)
        currentIndex = intent.getIntExtra("position", 0)

        updateImage()
        bindHeaderLayout()
        bindPreviewButtons()
    }
    /**
     * Updates the ImageView to show the currently selected image.
     * Also updates the navigation arrows' enabled state.
     */
    private fun updateImage() {
        val currentImage = images[currentIndex]
        viewBinding.previewImageView.setImageURI(currentImage.uri)
        updateNavigationButtons()
    }
    /**
     * Updates the state and alpha of the left and right navigation buttons
     * depending on the currently displayed image.
     */
    private fun updateNavigationButtons() {
        val leftArrow = viewBinding.leftArrow
        val rightArrow = viewBinding.rightArrow

        leftArrow.isEnabled = currentIndex > 0
        rightArrow.isEnabled = currentIndex < images.size - 1

        leftArrow.alpha = if (leftArrow.isEnabled) 1f else 0.4f
        rightArrow.alpha = if (rightArrow.isEnabled) 1f else 0.4f
    }
    /**
     * Sets up the header layout, including the title and the back button behavior.
     */
    private fun bindHeaderLayout() {
        viewBinding.headerLayout.headerTitle.text = getString(R.string.preview_header_shared)
        viewBinding.headerLayout.backButton.setOnClickListener {
            finish()
        }
    }
    /**
     * Sets up the left/right navigation buttons and the middle button for analysis.
     * Left/right buttons change the current image.
     * Middle button launches [AnalysisActivity] with the currently displayed image.
     */
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

}
