package pl.pb.optigai.utils

import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import pl.pb.optigai.Settings
import java.io.InputStream
import java.io.OutputStream

/**
 * A [Serializer] implementation for the [Settings] Protobuf object.
 *
 * This serializer handles reading and writing the app's settings from/to
 * a [DataStore] using Protocol Buffers.
 */
object SettingsSerializer : Serializer<Settings> {
    /**
     * Default settings used when the settings file is missing or cannot be read.
     */
    override val defaultValue: Settings =
        Settings
            .newBuilder()
            .setGridColumns(2)
            .setLanguageValue(Settings.Language.SYSTEM_DEFAULT_VALUE)
            .setIsPhotoSaving(true)
            .setZoomSeekBarModeValue(Settings.ZoomSeekBarMode.AUTO_VALUE)
            .addAllColor(
                listOf(
                    Settings.ColorOfBorder.RED,
                    Settings.ColorOfBorder.ORANGE,
                    Settings.ColorOfBorder.YELLOW,
                    Settings.ColorOfBorder.GREEN,
                    Settings.ColorOfBorder.CYAN,
                    Settings.ColorOfBorder.BLUE,
                    Settings.ColorOfBorder.PURPLE,
                ),
            ).build()

    /**
     * Reads the [Settings] object from the provided [InputStream].
     *
     * @param input The input stream to read from.
     * @return The deserialized [Settings] object, or [defaultValue] if parsing fails.
     */
    override suspend fun readFrom(input: InputStream): Settings =
        try {
            Settings.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            AppLogger.e("Error reading settings", e)
            defaultValue
        }

    /**
     * Writes the [Settings] object to the provided [OutputStream].
     *
     * @param t The [Settings] object to write.
     * @param output The output stream to write to.
     */
    override suspend fun writeTo(
        t: Settings,
        output: OutputStream,
    ) = t.writeTo(output)
}
