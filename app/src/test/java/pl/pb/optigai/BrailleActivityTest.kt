import org.junit.Assert.assertEquals
import org.junit.Test
import pl.pb.optigai.ui.BrailleActivity
import pl.pb.optigai.utils.data.BrailleChar

class BrailleActivityTest {

    @Test
    fun `decode should correctly convert braille characters to text`() {
        val predictions = listOf(
            BrailleChar(clazz = "A", x = 10f, y = 10f, width = 5f, height = 5f),
            BrailleChar(clazz = "B", x = 20f, y = 10f, width = 5f, height = 5f),
            BrailleChar(clazz = "C", x = 30f, y = 10f, width = 5f, height = 5f)
        )

        val result = BrailleActivity.decode(predictions)

        assertEquals("ABC", result)
    }
}









