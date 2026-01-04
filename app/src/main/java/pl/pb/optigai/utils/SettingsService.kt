/**
 * Singleton service class for managing application settings using
 * [DataStore] backed by Protocol Buffers ([SettingsSerializer]).
 *
 * Provides flows for observing settings and suspend functions for updating them.
 */
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

    /** The full [Settings] object as a flow. */
    val settingsFlow: Flow<Settings> = dataStore.data

    /** Number of columns in the gallery grid view. */
    val gridColumns: Flow<Int> = settingsFlow.map { it.gridColumns }

    /** App language preference. */
    val language: Flow<Settings.Language> = settingsFlow.map { it.language }

    /** Default location path for saving photos. */
    val defaultSaveLocation: Flow<String> = settingsFlow.map { it.defaultSaveLocation }

    /** Whether photo saving is enabled. */
    val isPhotoSaving: Flow<Boolean> = settingsFlow.map { it.isPhotoSaving }

    /** List of selected colors for detection borders. */
    val colors: Flow<List<Settings.ColorOfBorder>> = settingsFlow.map { it.colorList }

    /** Zoom slider visibility mode for the camera. */
    val zoomSeekBarMode: Flow<Settings.ZoomSeekBarMode> = settingsFlow.map { it.zoomSeekBarMode }

    /** Font size in sp for overlay text (defaults to 28 if not set). */
    val fontSizeSp: Flow<Int> = settingsFlow.map { s ->
        if (s.fontSizeSp > 0) s.fontSizeSp else 28
    }

    /**
     * Updates the number of grid columns in the gallery.
     *
     * @param columns Number of columns to set.
     */
    suspend fun updateGridColumns(columns: Int) {
        dataStore.updateData { current ->
            current.toBuilder().setGridColumns(columns).build()
        }
    }

    /**
     * Updates the app language.
     *
     * @param language The [Settings.Language] value to set.
     */
    suspend fun updateLanguage(language: Settings.Language) {
        dataStore.updateData { current ->
            current.toBuilder().setLanguage(language).build()
        }
    }

    /**
     * Updates the default save location path.
     *
     * @param path Path string for saving photos.
     */
    suspend fun updateDefaultSaveLocation(path: String) {
        dataStore.updateData { current ->
            current.toBuilder().setDefaultSaveLocation(path).build()
        }
    }

    /**
     * Enables or disables automatic photo saving.
     *
     * @param enabled True to enable photo saving, false to disable.
     */
    suspend fun updateIsPhotoSaving(enabled: Boolean) {
        dataStore.updateData { current ->
            current.toBuilder().setIsPhotoSaving(enabled).build()
        }
    }

    /**
     * Toggles the selection state of a color in the detection borders list.
     * If the color exists, it is removed; if it does not exist, it is added.
     *
     * @param color The [Settings.ColorOfBorder] to toggle.
     */
    suspend fun toggleColorOfBorder(color: Settings.ColorOfBorder) {
        AppLogger.d("Toggling color: $color")
        dataStore.updateData { settings ->
            val updatedColors = settings.colorList.toMutableList().apply {
                if (!remove(color)) add(color)
            }
            AppLogger.d("New color list: $updatedColors")
            settings.toBuilder().clearColor().addAllColor(updatedColors).build()
        }
    }

    /**
     * Updates the zoom slider visibility mode.
     *
     * @param mode The [Settings.ZoomSeekBarMode] to set.
     */
    suspend fun updateZoomSeekBarMode(mode: Settings.ZoomSeekBarMode) {
        dataStore.updateData { current ->
            current.toBuilder().setZoomSeekBarMode(mode).build()
        }
    }

    /**
     * Updates the font size for overlay text in sp.
     *
     * @param sp Font size in scaled pixels (sp).
     */
    suspend fun updateFontSize(sp: Int) {
        dataStore.updateData { current ->
            current.toBuilder().setFontSizeSp(sp).build()
        }
    }

    companion object {
        @Volatile
        private var instance: SettingsService? = null

        /**
         * Returns the singleton instance of [SettingsService].
         * Initializes it if not already created.
         *
         * @param context Application context.
         */
        fun getInstance(context: Context): SettingsService =
            instance ?: synchronized(this) {
                val instance = SettingsService(context.applicationContext.settingsDataStore)
                Companion.instance = instance
                instance
            }
    }
}
