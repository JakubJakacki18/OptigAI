package pl.pb.optigai.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import pl.pb.optigai.R
import pl.pb.optigai.utils.data.Image

class PhotoActivity : AppCompatActivity() {
    private lateinit var images: List<Image>
    private var currentIndex: Int = 0

    private lateinit var previewImageView: ImageView
    private lateinit var leftArrow: ImageView
    private lateinit var rightArrow: ImageView
    private lateinit var middleButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.photo_preview)

        previewImageView = findViewById(R.id.previewImageView)
        leftArrow = findViewById(R.id.leftArrow)
        rightArrow = findViewById(R.id.rightArrow)
        middleButton = findViewById(R.id.middleButton)

        // Get the list of images and the clicked image's position from the intent
        images =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra("images", Image::class.java) ?: emptyList()
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra("images") ?: emptyList()
            }
        currentIndex = intent.getIntExtra("position", 0)

        // Display the initial image
        updateImage()

        // Set click listeners for the navigation arrows
        leftArrow.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                updateImage()
            }
        }

        rightArrow.setOnClickListener {
            if (currentIndex < images.size - 1) {
                currentIndex++
                updateImage()
            }
        }
        middleButton.setOnClickListener {
            val currentImage = images[currentIndex]
            val intent = Intent(this@PhotoActivity, AnalysisActivity::class.java)
            intent.putExtra("IMAGE_URI", currentImage.uri.toString())
            startActivity(intent)
        }
    }

    private fun updateImage() {
        val currentImage = images[currentIndex]
        previewImageView.setImageURI(currentImage.uri)
        updateNavigationButtons()
    }

    private fun updateNavigationButtons() {
        leftArrow.isEnabled = currentIndex > 0
        rightArrow.isEnabled = currentIndex < images.size - 1
    }
}
