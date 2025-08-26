package pl.pb.optigai.ui

import AnalysisSelectorFragment
import android.os.Bundle
import android.view.View
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

        val uriString = intent.getStringExtra("IMAGE_URI")
        val uri = uriString?.toUri()

        uri?.let { analysisViewModel.initPhotoUri(it) }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, AnalysisSelectorFragment())
                .commit()
        }

        val backButton: View = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }
}
