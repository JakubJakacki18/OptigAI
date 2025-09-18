package pl.pb.optigai.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.ToggleButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import pl.pb.optigai.R
import pl.pb.optigai.Settings
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
        bindColorToggleButtons()
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

    private fun bindOneColorToggleButton(
        button: ToggleButton,
        color: Settings.ColorOfBorder,
    ) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.colors.collect { colors ->
                    button.setOnCheckedChangeListener(null)
                    button.isChecked = color in colors
                    button.setOnCheckedChangeListener { _, _ ->
                        lifecycleScope.launch {
                            viewModel.toggleColorOfBorder(color)
                        }
                    }
                }
            }
        }
    }

    private fun bindColorToggleButtons() {
        val toggleButtons =
            mapOf(
                viewBinding.toggleRed to Settings.ColorOfBorder.RED,
                viewBinding.toggleBlue to Settings.ColorOfBorder.BLUE,
                viewBinding.toggleYellow to Settings.ColorOfBorder.YELLOW,
                viewBinding.toggleCyan to Settings.ColorOfBorder.CYAN,
                viewBinding.toggleGreen to Settings.ColorOfBorder.GREEN,
                viewBinding.togglePurple to Settings.ColorOfBorder.PURPLE,
                viewBinding.toggleBlack to Settings.ColorOfBorder.BLACK,
                viewBinding.toggleWhite to Settings.ColorOfBorder.WHITE,
                viewBinding.toggleOrange to Settings.ColorOfBorder.ORANGE,
            )
        toggleButtons.forEach { (button, color) ->
            bindOneColorToggleButton(button, color)
        }
    }
}
