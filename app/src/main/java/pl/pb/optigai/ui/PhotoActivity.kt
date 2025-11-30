package pl.pb.optigai.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import pl.pb.optigai.R
import pl.pb.optigai.databinding.PhotoPreviewBinding
import pl.pb.optigai.utils.PhotoUtils
import pl.pb.optigai.utils.data.Image

class PhotoActivity : AppCompatActivity() {
    lateinit var images: List<Image>
    var currentIndex: Int = 0
    private lateinit var viewBinding: PhotoPreviewBinding
    var testCurrentIndex = 0
    lateinit var testImages: List<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = PhotoPreviewBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        testImages = images.map { it.uri.toString() }

        testCurrentIndex = intent?.getIntExtra("position", 0) ?: 0

        images = PhotoUtils.imageReader(this@PhotoActivity)
        currentIndex = intent.getIntExtra("position", 0)

        updateImage()
        bindHeaderLayout()
        bindPreviewButtons()
    }

    /**
     * Launcher for the cropping activity.
     */


    private fun updateImage() {
        val currentImage = images[currentIndex]
        viewBinding.previewImageView.setImageURI(currentImage.uri)
        updateNavigationButtons()
    }

    private fun updateNavigationButtons() {
        val leftArrow = viewBinding.leftArrow
        val rightArrow = viewBinding.rightArrow
        Log.d("PhotoActivity", "Current index: $testCurrentIndex, leftArrow enabled: ${leftArrow.isEnabled}")

        leftArrow.isEnabled = currentIndex > 0
        rightArrow.isEnabled = currentIndex < images.size - 1

        leftArrow.alpha = if (leftArrow.isEnabled) 1f else 0.4f
        rightArrow.alpha = if (rightArrow.isEnabled) 1f else 0.4f
    }

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

}