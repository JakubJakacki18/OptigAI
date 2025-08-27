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

    val isGridView: Flow<Boolean> =
        settingsFlow.map { it.isGridView }

    val gridColumns: Flow<Int> =
        settingsFlow.map { it.gridColumns }

    val language: Flow<Settings.Language> =
        settingsFlow.map { it.language }

    val defaultSaveLocation: Flow<String> =
        settingsFlow.map { it.defaultSaveLocation }

    val isPhotoSaving: Flow<Boolean> =
        settingsFlow.map { it.isPhotoSaving }

    suspend fun updateIsGridView(isGridView: Boolean) {
        dataStore.updateData { current ->
            current
                .toBuilder()
                .setIsGridView(isGridView)
                .build()
        }
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
