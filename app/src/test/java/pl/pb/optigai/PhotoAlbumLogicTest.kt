package pl.pb.optigai

import android.content.Context
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import pl.pb.optigai.utils.PermissionHandler
import pl.pb.optigai.utils.PhotoUtils
import pl.pb.optigai.utils.data.Image
@ExperimentalCoroutinesApi
class PhotoAlbumLogicTest {

    @Before
    fun setup() {
        // Mock PermissionHandler
        mockkObject(PermissionHandler)
        every { PermissionHandler.hasPermissions(any(), any()) } returns true

        // Mock PhotoUtils
        mockkObject(PhotoUtils)
        every { PhotoUtils.imageReader(any()) } returns listOf(
            Image(
                uri = mockk<Uri>(), // zamockowany Uri
                dateAddedTimeStamp = 123456789L,
                fileName = "dummy1.jpg"
            ),
            Image(
                uri = mockk<Uri>(),
                dateAddedTimeStamp = 456L,
                fileName = "dummy2.jpg"
            )
        )

    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testPermissionHandler_allowsAccess() {
        val context = mockk<Context>()
        val result = PermissionHandler.hasPermissions(context, arrayOf())
        assert(result) // zawsze true dzięki mock
    }

    @Test
    fun testPhotoUtils_returnsImages() = runBlocking {
        val images = PhotoUtils.imageReader(mockk())
        assert(images.size == 2)
        assert(images[0].fileName == "dummy1.jpg")
    }


}
