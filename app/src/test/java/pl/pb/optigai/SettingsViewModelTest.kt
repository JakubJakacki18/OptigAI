import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import pl.pb.optigai.utils.SettingsService
import pl.pb.optigai.utils.data.SettingsViewModel
import kotlinx.coroutines.flow.first

class SettingsViewModelTest {

    @Test
    fun testGridColumns() = runBlocking {
        // Zamockuj singleton
        mockkObject(SettingsService)
        val mockedService = mockk<SettingsService>(relaxed = true)

        // Ustawienie zwracanej instancji singletona
        every { SettingsService.getInstance(any()) } returns mockedService

        // Zamockowanie property gridColumns jako Flow<Int>
        every { mockedService.gridColumns } returns flowOf(2) // Int, nie Long

        // Stwórz ViewModel
        val viewModel = SettingsViewModel(mockk())

        // Odczytaj wartość z Flow
        val columns: Int = viewModel.gridColumns.first()
        assert(columns == 2)
    }
}
