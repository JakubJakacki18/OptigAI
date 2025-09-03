package pl.pb.optigai.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import pl.pb.optigai.R
import pl.pb.optigai.utils.data.AnalysisViewModel

// AnalysisActivity.kt
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import pl.pb.optigai.utils.data.BitmapCache
import kotlin.compareTo

class AnalysisActivity : AppCompatActivity() {
    private val analysisViewModel: AnalysisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        val uriString = intent.getStringExtra("IMAGE_URI")
        val uri = uriString?.toUri()

        uri?.let {
            analysisViewModel.initPhotoUri(it)

            val loadedBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, it)
            }

            // Corrected: Convert the bitmap to ARGB_8888 format
            val convertedBitmap = loadedBitmap.copy(Bitmap.Config.ARGB_8888, true)

            BitmapCache.bitmap = convertedBitmap
            analysisViewModel.isBitmapPassed.postValue(true)
        }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, AnalysisSelectorFragment())
                .commit()
        }

        val headerTitle: TextView = findViewById(R.id.headerTitle)
        headerTitle.text = getString(R.string.analysis_header_shared)

        onBackPressedDispatcher.addCallback(this){
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
}