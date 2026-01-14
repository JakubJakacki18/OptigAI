import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.*
import pl.pb.optigai.utils.PermissionHandler

class PermissionHandlerTest {

    @Test
    fun `hasPermissions returns true when all permissions are granted`() {
        val context = mock(Context::class.java)

        mockStatic(ContextCompat::class.java).use {
            `when`(
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.CAMERA
                )
            ).thenReturn(PackageManager.PERMISSION_GRANTED)

            val result =
                PermissionHandler.hasPermissions(
                    context,
                    arrayOf(android.Manifest.permission.CAMERA)
                )

            assertTrue(result)
        }
    }

    @Test
    fun `hasPermissions returns false when permission is missing`() {
        val context = mock(Context::class.java)

        mockStatic(ContextCompat::class.java).use {
            `when`(
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.CAMERA
                )
            ).thenReturn(PackageManager.PERMISSION_DENIED)

            val result =
                PermissionHandler.hasPermissions(
                    context,
                    arrayOf(android.Manifest.permission.CAMERA)
                )

            assertFalse(result)
        }
    }
    @Test
    fun `hasPermissions returns true when permission list is empty`() {
        val context = mock(Context::class.java)

        val result = PermissionHandler.hasPermissions(
            context,
            emptyArray()
        )

        assertTrue(result)
    }

}
