package pl.pb.optigai.utils.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.pb.optigai.utils.SettingsService

class SettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val settingsService = SettingsService.getInstance(application)

    val gridColumns = settingsService.gridColumns
    val isGridView = settingsService.isGridView
    val language = settingsService.language
    val defaultSaveLocation = settingsService.defaultSaveLocation
    val isSavingPhoto = settingsService.isSavingPhoto

    fun setGridColumns(columns: Int) {
        viewModelScope.launch {
            settingsService.updateGridColumns(columns)
        }
    }

    fun setIsGridView(isGridView: Boolean) {
        viewModelScope.launch {
            settingsService.updateIsGridView(isGridView)
        }
    }
}
