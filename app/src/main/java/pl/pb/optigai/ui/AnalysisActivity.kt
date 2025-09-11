package pl.pb.optigai.ui
import pl.pb.optigai.ui.AnalysisSelectorFragment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
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
import pl.pb.optigai.utils.data.AnalysisViewModel
import pl.pb.optigai.utils.data.BitmapCache
import java.lang.IllegalArgumentException

class AnalysisActivity : AppCompatActivity() {
    private val analysisViewModel: AnalysisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)
        var uri: Uri?
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

        uri?.let {
            analysisViewModel.initPhotoUri(it)
            val source = ImageDecoder.createSource(contentResolver, it)
            val loadedBitmap = ImageDecoder.decodeBitmap(source)
            val convertedBitmap = loadedBitmap.copy(Bitmap.Config.ARGB_8888, true)
            BitmapCache.bitmap = convertedBitmap
        }
        if (BitmapCache.bitmap == null) {
            AppLogger.e("bitmap is null", IllegalArgumentException())
        }
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

    override fun onDestroy() {
        super.onDestroy()
        BitmapCache.bitmap = null
        AppLogger.i("onDestroy: Bitmap cleared from cache")
    }
}
