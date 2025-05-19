package pl.pb.optigai

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager // Import GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Define the Album data class (replace with your actual data structure)
data class Album(val imageUrl: String)

class AlbumView : AppCompatActivity() {
    private lateinit var rvContacts: RecyclerView
    private lateinit var back_button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.photo_album)

        // Handle insets (if you're using edge-to-edge display)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.album_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize RecyclerView and back button using findViewById
        rvContacts = findViewById(R.id.photos) // Make sure this ID exists in photo_album.xml
        back_button = findViewById(R.id.back_button) // Make sure this ID exists in photo_album.xml

        // RecyclerView setup with GridLayoutManager
        val spanCount = 2 // Number of columns in the grid
        rvContacts.layoutManager = GridLayoutManager(this, spanCount)
        rvContacts.adapter = AlbumViewAdapter(this, createAlbumList()) // Assuming createAlbumList() exists

        // Handle back button click
        back_button.setOnClickListener {
            finish()
        }
    }

    // Example function to create a list of Album objects (replace with your actual data loading)
    private fun createAlbumList(): List<Album> {
        return listOf(
            Album("https://via.placeholder.com/150/92c952"),
            Album("https://via.placeholder.com/150/771796"),
            Album("https://via.placeholder.com/150/24f355"),
            Album("https://via.placeholder.com/150/d32776"),
            Album("https://via.placeholder.com/150/f66b97"),
            Album("https://via.placeholder.com/150/56a8c2"),
            // Add more image URLs here
        )
    }
}