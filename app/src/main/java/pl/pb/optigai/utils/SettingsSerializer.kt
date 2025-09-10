package pl.pb.optigai.utils

import android.util.Log
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import pl.pb.optigai.Settings
import java.io.InputStream
import java.io.OutputStream

object SettingsSerializer : Serializer<Settings> {
    override val defaultValue: Settings =
        Settings
            .newBuilder()
            .setIsGridView(true)
            .setGridColumns(2)
            .setLanguageValue(Settings.Language.SYSTEM_DEFAULT_VALUE)
            .setIsPhotoSaving(true)
            .addAllColor(
                listOf(
                    Settings.ColorOfBorder.BLUE,
                    Settings.ColorOfBorder.YELLOW,
                    Settings.ColorOfBorder.PURPLE,
                    Settings.ColorOfBorder.GRAY,
                    Settings.ColorOfBorder.WHITE,
                ),
            ).build()

    override suspend fun readFrom(input: InputStream): Settings =
        try {
            Settings.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            Log.e("SettingsSerializer", "Error reading settings", e)
            defaultValue
        }

    override suspend fun writeTo(
        t: Settings,
        output: OutputStream,
    ) = t.writeTo(output)
}
