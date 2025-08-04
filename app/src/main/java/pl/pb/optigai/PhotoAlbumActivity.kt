package pl.pb.optigai

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PhotoAlbumActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.photo_album)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val backButton: ImageView = findViewById(R.id.backButton)

        // Przykładowe dane obrazów
        val imageList = listOf(
            Image(R.drawable.obrazek1),
            Image(R.drawable.obrazek2),
            Image(R.drawable.obrazek3),
            Image(R.drawable.obrazek4),
            Image(R.drawable.obrazek5),
            Image(R.drawable.obrazek6)
        )

        // Konfiguracja RecyclerView
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        val adapter = ImageAdapter(imageList)
        recyclerView.adapter = adapter

        // Ustawienie akcji dla przycisku "back"
        backButton.setOnClickListener {
            // Zamyka bieżącą aktywność i wraca do poprzedniej (MainActivity)
            finish()
        }
    }
}