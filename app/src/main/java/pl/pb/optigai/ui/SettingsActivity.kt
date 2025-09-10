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
import pl.pb.optigai.Settings
import pl.pb.optigai.databinding.ActivitySettingsBinding
import pl.pb.optigai.utils.AppLogger
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

    private fun bindColorToggleButtons() {
        val toggleRed = viewBinding.toggleRed
        val toggleBlue = viewBinding.toggleBlue
        val toggleYellow = viewBinding.toggleYellow
        val toggleGray = viewBinding.toggleGray
        val toggleGreen = viewBinding.toggleGreen
        val togglePurple = viewBinding.togglePurple
        val toggleBlack = viewBinding.toggleBlack
        val toggleWhite = viewBinding.toggleWhite

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.colors.collect { colors ->
                    toggleRed.isChecked = Settings.ColorOfBorder.RED in colors
                    toggleBlue.isChecked = Settings.ColorOfBorder.BLUE in colors
                    toggleGreen.isChecked = Settings.ColorOfBorder.GREEN in colors
                    toggleYellow.isChecked = Settings.ColorOfBorder.YELLOW in colors
                    toggleGray.isChecked = Settings.ColorOfBorder.GRAY in colors
                    togglePurple.isChecked = Settings.ColorOfBorder.PURPLE in colors
                    toggleBlack.isChecked = Settings.ColorOfBorder.BLACK in colors
                    toggleWhite.isChecked = Settings.ColorOfBorder.WHITE in colors
                }
            }
        }
        toggleRed.setOnCheckedChangeListener { _, _ ->
            lifecycleScope.launch {
                viewModel.toggleColorOfBorder(Settings.ColorOfBorder.RED)
            }
        }
        toggleBlue.setOnCheckedChangeListener { _, _ ->
            viewModel.toggleColorOfBorder(Settings.ColorOfBorder.BLUE)
        }
        toggleGreen.setOnCheckedChangeListener { _, _ ->
            viewModel.toggleColorOfBorder(Settings.ColorOfBorder.GREEN)
        }
        toggleYellow.setOnCheckedChangeListener { _, _ ->
            viewModel.toggleColorOfBorder(Settings.ColorOfBorder.YELLOW)
        }
        toggleGray.setOnCheckedChangeListener { _, _ ->
            viewModel.toggleColorOfBorder(Settings.ColorOfBorder.GRAY)
        }
        togglePurple.setOnCheckedChangeListener { _, _ ->
            viewModel.toggleColorOfBorder(Settings.ColorOfBorder.PURPLE)
        }
        toggleBlack.setOnCheckedChangeListener { _, _ ->
            viewModel.toggleColorOfBorder(Settings.ColorOfBorder.BLACK)
        }
        toggleWhite.setOnCheckedChangeListener { _, _ ->
            viewModel.toggleColorOfBorder(Settings.ColorOfBorder.WHITE)
        }
    }
}
