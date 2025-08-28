package pl.pb.optigai.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import pl.pb.optigai.R
import pl.pb.optigai.databinding.ActivitySettingsBinding
import pl.pb.optigai.utils.data.SettingsViewModel

class SettingsActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        bindIsGridViewRadioButtons()
        bindIsPhotoSavingRadioButtons()
        val headerTitle: TextView = findViewById(R.id.headerTitle)
        headerTitle.text = getString(R.string.settings_header)

        val backButton: View = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun bindIsGridViewRadioButtons() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isGridView.collect { isGridView ->
                    viewBinding.viewRadioGroup.check(
                        if (isGridView) R.id.gridRadioButton else R.id.listRadioButton,
                    )
                    viewBinding.viewRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                        lifecycleScope.launch {
                            val isGridViewValue = checkedId == R.id.gridRadioButton
                            viewModel.setIsGridView(isGridViewValue)
                        }
                    }
                }
            }
        }
    }

    private fun bindIsPhotoSavingRadioButtons() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isPhotoSaving.collect { isPhotoSaving ->
                    viewBinding.isPhotoSavingRadioGroup.check(
                        if (isPhotoSaving) R.id.yesIsPhotoSavingRadioButton else R.id.noIsPhotoSavingRadioButton,
                    )
                    viewBinding.isPhotoSavingRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                        lifecycleScope.launch {
                            val isPhotoSavingValue = checkedId == R.id.yesIsPhotoSavingRadioButton
                            viewModel.setIsPhotoSaving(isPhotoSavingValue)
                        }
                    }
                }
            }
        }
    }
}
