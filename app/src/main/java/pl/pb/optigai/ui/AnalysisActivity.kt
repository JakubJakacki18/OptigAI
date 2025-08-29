package pl.pb.optigai.ui

import AnalysisSelectorFragment
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import pl.pb.optigai.R
import pl.pb.optigai.utils.data.AnalysisViewModel

class AnalysisActivity : AppCompatActivity() {
    private val analysisViewModel: AnalysisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        val isBitmapPassed = intent.getBooleanExtra("IS_BITMAP_PASSED", false)
        val uriString = intent.getStringExtra("IMAGE_URI")

        if (isBitmapPassed) {
            analysisViewModel.isBitmapPassed.value = true
        } else {
            val uri = uriString?.toUri()
            uri?.let { analysisViewModel.initPhotoUri(it) }
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
