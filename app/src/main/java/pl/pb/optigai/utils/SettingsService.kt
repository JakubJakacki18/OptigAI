package pl.pb.optigai.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.pb.optigai.Settings
import android.util.Log

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
        Log.d("SettingsService", "Toggling color: $color")
        dataStore.updateData { settings ->
            val currentColors = settings.colorList.toMutableList()
            if (currentColors.contains(color)) {
                currentColors.remove(color)
            } else {
                currentColors.add(color)
            }
            Log.d("SettingsService", "New color list: $currentColors")
            settings.toBuilder().clearColor().addAllColor(currentColors).build()
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
