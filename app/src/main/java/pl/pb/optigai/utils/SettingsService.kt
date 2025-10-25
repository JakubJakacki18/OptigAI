package pl.pb.optigai.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.pb.optigai.Settings

private val Context.settingsDataStore: DataStore<Settings> by dataStore(
    fileName = "settings.optigai",
    serializer = SettingsSerializer,
)

class SettingsService private constructor(
    private val dataStore: DataStore<Settings>,
) {
    val settingsFlow: Flow<Settings> = dataStore.data

    val gridColumns: Flow<Int> =
        settingsFlow.map { it.gridColumns }

    val language: Flow<Settings.Language> =
        settingsFlow.map { it.language }

    val defaultSaveLocation: Flow<String> =
        settingsFlow.map { it.defaultSaveLocation }

    val isPhotoSaving: Flow<Boolean> =
        settingsFlow.map { it.isPhotoSaving }

    val colors: Flow<List<Settings.ColorOfBorder>> =
        settingsFlow.map { it.colorList }
    val zoomSeekBarMode: Flow<Settings.ZoomSeekBarMode> =
        settingsFlow.map { it.zoomSeekBarMode }

    val fontSizeSp: Flow<Int> = settingsFlow.map { s ->
        // default to 28 if the field was never set
        if (s.fontSizeSp > 0) s.fontSizeSp else 28
    }

    suspend fun updateGridColumns(columns: Int) {
        dataStore.updateData { current ->
            current
                .toBuilder()
                .setGridColumns(columns)
                .build()
        }
    }

    suspend fun updateLanguage(language: Settings.Language) {
        dataStore.updateData { current ->
            current
                .toBuilder()
                .setLanguage(language)
                .build()
        }
    }

    suspend fun updateDefaultSaveLocation(path: String) {
        dataStore.updateData { current ->
            current
                .toBuilder()
                .setDefaultSaveLocation(path)
                .build()
        }
    }

    suspend fun updateIsPhotoSaving(enabled: Boolean) {
        dataStore.updateData { current ->
            current
                .toBuilder()
                .setIsPhotoSaving(enabled)
                .build()
        }
    }

    suspend fun toggleColorOfBorder(color: Settings.ColorOfBorder) {
        AppLogger.d("Toggling color: $color")
        dataStore.updateData { settings ->
            val updatedColors =
                settings.colorList
                    .toMutableList()
                    .apply {
                        if (!remove(color)) add(color)
                    }
            AppLogger.d("New color list: $updatedColors")
            settings
                .toBuilder()
                .clearColor()
                .addAllColor(updatedColors)
                .build()
        }
    }

    suspend fun updateZoomSeekBarMode(mode: Settings.ZoomSeekBarMode) {
        dataStore.updateData { current ->
            current.toBuilder()
                .setZoomSeekBarMode(mode)
                .build()
        }
    }

    suspend fun updateFontSize(sp: Int) {
        dataStore.updateData { current ->
            current.toBuilder()
                .setFontSizeSp(sp)
                .build()
        }
    }

    companion object {
        @Volatile
        private var instance: SettingsService? = null

        fun getInstance(context: Context): SettingsService =
            instance ?: synchronized(this) {
                val instance = SettingsService(context.applicationContext.settingsDataStore)
                Companion.instance = instance
                instance
            }
    }
}
