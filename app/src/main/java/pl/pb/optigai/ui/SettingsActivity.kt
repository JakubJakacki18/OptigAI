package pl.pb.optigai.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import pl.pb.optigai.R
import pl.pb.optigai.Settings
import pl.pb.optigai.databinding.ActivitySettingsBinding
import pl.pb.optigai.utils.data.ColorMap
import pl.pb.optigai.utils.data.SettingsViewModel

class SettingsActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    /**
     * Initializes the activity, sets up the view binding, and binds UI components to the ViewModel.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        bindGalleryViewColumnsSlider()
        bindPhotoSavingToggle()
        bindColorCircles()

        val headerTitle: TextView = findViewById(R.id.headerTitle)
        headerTitle.text = getString(R.string.settings_header)

        val backButton: View = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
        val zoomToggle = viewBinding.zoomSeekBarToggle
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.zoomSeekBarMode.collect { mode ->
                    when (mode) {
                        Settings.ZoomSeekBarMode.ALWAYS_OFF -> zoomToggle.check(R.id.zoomAlwaysOff)
                        Settings.ZoomSeekBarMode.AUTO -> zoomToggle.check(R.id.zoomAuto)
                        Settings.ZoomSeekBarMode.ALWAYS_ON -> zoomToggle.check(R.id.zoomAlwaysOn)
                        Settings.ZoomSeekBarMode.UNRECOGNIZED -> {
                            zoomToggle.clearChecked()
                        }
                    }
                }
            }
        }


        zoomToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val newMode = when (checkedId) {
                    R.id.zoomAlwaysOff -> Settings.ZoomSeekBarMode.ALWAYS_OFF
                    R.id.zoomAuto -> Settings.ZoomSeekBarMode.AUTO
                    R.id.zoomAlwaysOn -> Settings.ZoomSeekBarMode.ALWAYS_ON
                    else -> Settings.ZoomSeekBarMode.AUTO
                }
                viewModel.setZoomSeekBarMode(newMode)
            }
        }

    }

    /**
     * Binds the color circles to the ViewModel, handling color display and user selection.
     */
    private fun bindColorCircles() {
        val colorItems =
            mapOf(
                viewBinding.colorRed.colorCircle to Settings.ColorOfBorder.RED,
                viewBinding.colorOrange.colorCircle to Settings.ColorOfBorder.ORANGE,
                viewBinding.colorYellow.colorCircle to Settings.ColorOfBorder.YELLOW,
                viewBinding.colorGreen.colorCircle to Settings.ColorOfBorder.GREEN,
                viewBinding.colorCyan.colorCircle to Settings.ColorOfBorder.CYAN,
                viewBinding.colorBlue.colorCircle to Settings.ColorOfBorder.BLUE,
                viewBinding.colorPurple.colorCircle to Settings.ColorOfBorder.PURPLE,
                viewBinding.colorBlack.colorCircle to Settings.ColorOfBorder.BLACK,
                viewBinding.colorWhite.colorCircle to Settings.ColorOfBorder.WHITE,
            )

        colorItems.forEach { (circleView, color) ->
            val checkMark = circleView.findViewById<TextView>(R.id.checkMark)
            val circleColorInt = ColorMap.getColorRes(color)

            setBackgroundColorAndBorderForCircleView(circleView, circleColorInt)
            // Set checkmark color for contrast
            checkMark?.setTextColor(if (isColorLight(circleColorInt)) Color.BLACK else Color.WHITE)

            // Click listener on the circle itself
            circleView.setOnClickListener {
                lifecycleScope.launch { viewModel.toggleColorOfBorder(color) }
            }

            // Observe ViewModel for selected colors
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.colors.collect { selectedColors ->
                        checkMark?.visibility = if (selectedColors.contains(color)) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    /**
     * Determines if a given color is light or dark to set a contrasting text color.
     * @return true if the color is light, false otherwise.
     */
    private fun isColorLight(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness < 0.5
    }

    /**
     * Binds the gallery view slider to the ViewModel to manage grid columns.
     */
    private fun bindGalleryViewColumnsSlider() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.gridColumns.collect { gridColumns ->
                    viewBinding.gridColumnsSlider.value = gridColumns.toFloat()
                }
            }
        }

        viewBinding.gridColumnsSlider.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                lifecycleScope.launch {
                    viewModel.setGridColumns(value.toInt())
                }
            }
        }
    }

    /**
     * Binds the photo saving switch to the ViewModel to toggle photo saving.
     */
    private fun bindPhotoSavingToggle() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isPhotoSaving.collect { isPhotoSaving ->
                    viewBinding.isPhotoSavingToggle.isChecked = isPhotoSaving
                }
            }
        }

        viewBinding.isPhotoSavingToggle.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                viewModel.setIsPhotoSaving(isChecked)
            }
        }
    }

/**
     * Sets the background color and border for a given circle view.
     * @param circleView The MaterialCardView representing the color circle.
     * @param circleColorInt The color integer to set as the background.
     */
    private fun setBackgroundColorAndBorderForCircleView(
        circleView: MaterialCardView,
        circleColorInt: Int,
    ) {
        circleView.setCardBackgroundColor((ContextCompat.getColor(this, circleColorInt)))
        circleView.strokeColor = ContextCompat.getColor(this, R.color.dark_blue)
        circleView.strokeWidth = 8
        circleView.cardElevation = 0f
    }
}
