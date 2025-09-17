package pl.pb.optigai.utils.data

import pl.pb.optigai.R
import pl.pb.optigai.Settings

object ColorMap {
    private val colorMap =
        mapOf(
            Settings.ColorOfBorder.RED to R.color.red_toggle,
            Settings.ColorOfBorder.BLUE to R.color.blue_toggle,
            Settings.ColorOfBorder.YELLOW to R.color.yellow_toggle,
            Settings.ColorOfBorder.GRAY to R.color.gray_toggle,
            Settings.ColorOfBorder.GREEN to R.color.green_toggle,
            Settings.ColorOfBorder.PURPLE to R.color.purple_toggle,
            Settings.ColorOfBorder.BLACK to R.color.black_toggle,
            Settings.ColorOfBorder.WHITE to R.color.white_toggle,
        )

    fun getColorRes(color: Settings.ColorOfBorder) = colorMap[color] ?: R.color.blue_toggle
}
