package pl.pb.optigai.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Utility object for handling Android runtime permissions.
 *
 * Provides a simple method to check if all required permissions have been granted.
 */
object PermissionHandler {
    /**
     * Checks if all required permissions are granted.
     *
     * @param context The context used to check permissions.
     * @param requiredPermissions An array of permission strings to check (e.g., `Manifest.permission.CAMERA`).
     * @return `true` if all permissions are granted, `false` otherwise.
     */
    fun hasPermissions(
        context: Context,
        requiredPermissions: Array<String>,
    ) = requiredPermissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}
