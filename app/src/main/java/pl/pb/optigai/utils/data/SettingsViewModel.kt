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

    // Only use gridColumns to manage the number of columns
    val gridColumns = settingsService.gridColumns
    val language = settingsService.language
    val defaultSaveLocation = settingsService.defaultSaveLocation
    val isPhotoSaving = settingsService.isPhotoSaving
    val colors = settingsService.colors
    val zoomSeekBarMode = settingsService.zoomSeekBarMode

    fun setGridColumns(columns: Int) {
        viewModelScope.launch {
            settingsService.updateGridColumns(columns)
        }
    }


    fun setIsPhotoSaving(isPhotoSaving: Boolean) {
        viewModelScope.launch {
            settingsService.updateIsPhotoSaving(isPhotoSaving)
        }
    }

    fun toggleColorOfBorder(color: Settings.ColorOfBorder) {
        viewModelScope.launch {
            settingsService.toggleColorOfBorder(color)
        }
    }


    fun setZoomSeekBarMode(mode: Settings.ZoomSeekBarMode) {
        viewModelScope.launch {
            settingsService.updateZoomSeekBarMode(mode)
        }
    }

}
