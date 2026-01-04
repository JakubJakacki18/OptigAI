/**
 * AnalysisActivity
 *
 * Main activity responsible for displaying and handling image analysis in the application.
 * This activity initializes the image for analysis based on the received URI, stores the bitmap
 * in a cache, and manages the AnalysisSelectorFragment.
 *
 * Features:
 * - Receiving an image from an Intent (shared from other apps) or a URI passed via the "IMAGE_URI" parameter.
 * - Initializing and storing the bitmap in the global BitmapCache.
 * - Managing UI fragments related to image analysis.
 * - Customizing the header title and back button behavior.
 * - Clearing the cached bitmap when the activity is destroyed.
 *
 * Collaborates with:
 * - [AnalysisViewModel] – handles the logic of image analysis and stores the photo URI.
 * - [BitmapCache] – global cache for storing the bitmap.
 * - [AnalysisSelectorFragment] – fragment for selecting the type of analysis.
 * - [PhotoUtils] – utility functions for converting URI to bitmap.
 * - [AppLogger] – logging events and errors.
 */
package pl.pb.optigai.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import pl.pb.optigai.R
import pl.pb.optigai.utils.AppLogger
import pl.pb.optigai.utils.PhotoUtils.convertUriToBitmap
import pl.pb.optigai.utils.data.AnalysisViewModel
import pl.pb.optigai.utils.data.BitmapCache
import java.lang.IllegalArgumentException

class AnalysisActivity : AppCompatActivity() {
    /** ViewModel responsible for the image analysis logic. */
    private val analysisViewModel: AnalysisViewModel by viewModels()
    /**
     * Called when the activity is created.
     * - Sets the layout from R.layout.activity_analysis.
     * - Retrieves the image URI from the Intent or parameters and initializes the BitmapCache.
     * - Initializes the AnalysisSelectorFragment.
     * - Configures the header title and back button.
     * - Adds a custom callback for the onBackPressedDispatcher.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)
        val uri = getUriOrNull()

        uri?.let { uri ->
            analysisViewModel.initPhotoUri(uri)
            BitmapCache.bitmap = convertUriToBitmap(this, uri)
        }

        handleBitmapCacheIsNull()

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, AnalysisSelectorFragment())
                .commit()
        }

        val headerTitle: TextView = findViewById(R.id.headerTitle)
        headerTitle.text = getString(R.string.analysis_header_shared)

        onBackPressedDispatcher.addCallback(this) {
            val fragmentManager = supportFragmentManager
            if (fragmentManager.backStackEntryCount > 0) {
                fragmentManager.popBackStack()
            } else {
                finish()
            }
        }

        val backButton: View = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    /**
     * Called when the activity is destroyed.
     * Clears the bitmap from the cache and logs the event.
     */
    override fun onDestroy() {
        super.onDestroy()
        BitmapCache.bitmap = null
        AppLogger.i("onDestroy: Bitmap cleared from cache")
    }
    /**
     * Retrieves the image URI from the Intent or parameters.
     *
     * @return The image URI, or null if it was not provided.
     */
    private fun getUriOrNull(): Uri? {
        val uri: Uri?
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            uri =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }
        } else {
            val uriString = intent.getStringExtra("IMAGE_URI")
            uri = uriString?.toUri()
        }
        return uri
    }
    /**
     * Checks whether the BitmapCache contains a bitmap.
     * Logs an error if the bitmap is null.
     */
    private fun handleBitmapCacheIsNull() {
        if (BitmapCache.bitmap == null) {
            AppLogger.e("bitmap is null", IllegalArgumentException())
        }
    }
}
