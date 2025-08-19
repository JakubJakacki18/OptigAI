package pl.pb.optigai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import pl.pb.optigai.ui.CameraActivity
import pl.pb.optigai.ui.PhotoAlbumActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cameraButton: Button = findViewById(R.id.cameraButton)
        val galleryButton: Button = findViewById(R.id.galleryButton)

        galleryButton.setOnClickListener {
            val intent = Intent(this, PhotoAlbumActivity::class.java)
            startActivity(intent)
        }

        cameraButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
    }
}
