/**
 * ColorMap
 *
 * A utility object that maps [Settings.ColorOfBorder] enum values to corresponding
 * Android color resource IDs. Used throughout the app to display consistent
 * colors for borders, UI elements, and overlays.
 *
 * Example usage:
 * ```
 * val redColorRes = ColorMap.getColorRes(Settings.ColorOfBorder.RED)
 * ```
 */
package pl.pb.optigai.utils.data.const

import pl.pb.optigai.R
import pl.pb.optigai.Settings

object ColorMap {
    /**
     * Internal mapping from [Settings.ColorOfBorder] to color resource IDs.
     * If a color is not found, the default fallback color is [R.color.blue_toggle].
     */
    private val colorMap =
        mapOf(
            Settings.ColorOfBorder.RED to R.color.red_toggle,
            Settings.ColorOfBorder.BLUE to R.color.blue_toggle,
            Settings.ColorOfBorder.YELLOW to R.color.yellow_toggle,
            Settings.ColorOfBorder.CYAN to R.color.cyan_toggle,
            Settings.ColorOfBorder.GREEN to R.color.green_toggle,
            Settings.ColorOfBorder.PURPLE to R.color.purple_toggle,
            Settings.ColorOfBorder.BLACK to R.color.black_toggle,
            Settings.ColorOfBorder.WHITE to R.color.white_toggle,
            Settings.ColorOfBorder.ORANGE to R.color.orange_toggle,
        )
    /**
     * Retrieves the color resource ID associated with the given [color].
     *
     * @param color The [Settings.ColorOfBorder] enum value.
     * @return The corresponding Android color resource ID. Defaults to [R.color.blue_toggle] if not found.
     */
    fun getColorRes(color: Settings.ColorOfBorder) = colorMap[color] ?: R.color.blue_toggle
}
