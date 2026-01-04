/**
 * [AndroidViewModel] responsible for managing and exposing the user settings.
 *
 * This ViewModel acts as a bridge between the UI and [SettingsService],
 * providing reactive flows for observing settings and functions to update them.
 *
 * @property gridColumns Flow representing the number of columns in the gallery grid.
 * @property language Flow representing the selected app language.
 * @property defaultSaveLocation Flow representing the default save location for photos.
 * @property isPhotoSaving Flow indicating whether photos should be saved to external storage.
 * @property colors Flow containing the list of selected border colors.
 * @property zoomSeekBarMode Flow representing the visibility mode of the camera zoom slider.
 * @property fontSizeSp Flow representing the preferred font size (in sp) for UI elements.
 *
 * All setter functions launch coroutines in [viewModelScope] to update values asynchronously
 * through [SettingsService].
 */
package pl.pb.optigai.utils.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.pb.optigai.Settings
import pl.pb.optigai.utils.SettingsService

class SettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val settingsService = SettingsService.getInstance(application)

    val gridColumns = settingsService.gridColumns
    val language = settingsService.language
    val defaultSaveLocation = settingsService.defaultSaveLocation
    val isPhotoSaving = settingsService.isPhotoSaving
    val colors = settingsService.colors
    val zoomSeekBarMode = settingsService.zoomSeekBarMode
    val fontSizeSp = settingsService.fontSizeSp
    /** Updates the number of columns in the gallery grid. */
    fun setGridColumns(columns: Int) {
        viewModelScope.launch {
            settingsService.updateGridColumns(columns)
        }
    }
    /** Updates whether photos are saved automatically. */
    fun setIsPhotoSaving(isPhotoSaving: Boolean) {
        viewModelScope.launch {
            settingsService.updateIsPhotoSaving(isPhotoSaving)
        }
    }
    /** Toggles a color in the selected border colors list. */
    fun toggleColorOfBorder(color: Settings.ColorOfBorder) {
        viewModelScope.launch {
            settingsService.toggleColorOfBorder(color)
        }
    }
    /** Updates the zoom slider visibility mode. */
    fun setZoomSeekBarMode(mode: Settings.ZoomSeekBarMode) {
        viewModelScope.launch {
            settingsService.updateZoomSeekBarMode(mode)
        }
    }
    /** Updates the preferred font size (in sp). */
    fun setFontSize(sp: Int) {
        viewModelScope.launch {
            settingsService.updateFontSize(sp)
        }
    }

}
