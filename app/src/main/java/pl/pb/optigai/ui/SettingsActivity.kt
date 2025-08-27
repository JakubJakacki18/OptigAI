package pl.pb.optigai.ui

import android.os.Bundle
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isGridView.collect { isGridView ->
                    viewBinding.viewRadioGroup.check(
                        if (isGridView) R.id.gridRadioButton else R.id.listRadioButton,
                    )
                    viewBinding.viewRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                        lifecycleScope.launch {
                            val isGrid = checkedId == R.id.gridRadioButton
                            viewModel.setIsGridView(isGrid)
                        }
                    }
                }
            }
        }

        viewBinding.backButton.setOnClickListener {
            finish()
        }
    }
}
