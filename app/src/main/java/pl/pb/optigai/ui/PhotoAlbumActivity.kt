package pl.pb.optigai.ui

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pl.pb.optigai.R
import pl.pb.optigai.utils.data.Image
import java.io.File

class PhotoAlbumActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.photo_album)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val backButton: ImageView = findViewById(R.id.backButton)

        // Przykładowe dane obrazów
        val imageList =
            imageReader(this)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        val adapter =
            ImageAdapter(imageList) { position ->
                val intent = Intent(this, PhotoActivity::class.java)
                intent.putExtra("images", ArrayList(imageList))
                intent.putExtra("position", position)
                startActivity(intent)
            }
        recyclerView.adapter = adapter

        backButton.setOnClickListener {
            finish()
        }
    }

    val absoluteStoragePath: String = Environment.getExternalStorageDirectory().absolutePath
    val relativePicturesPath = "Pictures/OptigAI/"
    val picturesPath = File(absoluteStoragePath + File.separator + relativePicturesPath)

//    fun imageReader(): List<Image> {
//        val listAllFiles = picturesPath.listFiles()
//        return listAllFiles
//            ?.filter { it.name.endsWith(".jpg", ignoreCase = true) }
//            ?.map { file -> Image(Uri.fromFile(file)) }
//            ?: emptyList()
//    }

    fun imageReader(context: Context): List<Image> {
        val images = mutableListOf<Image>()
        val projection =
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
            )
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} = ? AND ${MediaStore.Images.Media.SIZE} > 0"
        val selectionArgs = arrayOf(relativePicturesPath)

        val query =
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${MediaStore.Images.Media.DATE_ADDED} DESC",
            )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri =
                    ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id,
                    )
                images.add(Image(contentUri))
            }
        }
        Log.d("PhotoAlbumActivity", "Found ${images.size} images")
        return images
    }
}
