package pl.pb.optigai.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import pl.pb.optigai.R
import pl.pb.optigai.databinding.PhotoPreviewBinding
import pl.pb.optigai.utils.PhotoUtils
import pl.pb.optigai.utils.data.Image

class PhotoActivity : AppCompatActivity() {
    private lateinit var images: List<Image>
    private var currentIndex: Int = 0
    private lateinit var viewBinding: PhotoPreviewBinding

    /**
     * Called when the activity is first created. Initializes the view, loads images,
     * and sets up click listeners for navigation and analysis.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = PhotoPreviewBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        images = PhotoUtils.imageReader(this@PhotoActivity)
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

        val headerTitle: TextView = findViewById(R.id.headerTitle)
        headerTitle.text = getString(R.string.preview_header_shared)

        val backButton: View = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    /**
     * Updates the ImageView with the current image and refreshes the navigation button states.
     */
    private fun updateImage() {
        val currentImage = images[currentIndex]
        viewBinding.previewImageView.setImageURI(currentImage.uri)
        updateNavigationButtons()
    }

    /**
     * Enables or disables the navigation arrows based on the current image index.
     * The left arrow is disabled at the first image, and the right arrow at the last.
     */
    private fun updateNavigationButtons() {
        viewBinding.leftArrow.isEnabled = currentIndex > 0
        viewBinding.rightArrow.isEnabled = currentIndex < images.size - 1
    }
}