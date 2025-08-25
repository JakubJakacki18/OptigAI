package pl.pb.optigai.ui

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.pb.optigai.R
import pl.pb.optigai.databinding.PhotoAlbumBinding
import pl.pb.optigai.utils.PermissionHandler
import pl.pb.optigai.utils.data.Image

class PhotoAlbumActivity : AppCompatActivity() {
    private lateinit var viewBinding: PhotoAlbumBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = PhotoAlbumBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if (!PermissionHandler.hasPermissions(baseContext, REQUIRED_PERMISSIONS)) {
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            loadImagesToRecyclerView()
        }
        val backButton: ImageView = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }
    }

    fun loadImagesToRecyclerView() {
        viewBinding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        lifecycleScope.launch {
            val imageList =
                withContext(Dispatchers.IO) {
                    imageReader(this@PhotoAlbumActivity)
                }
            val adapter =
                ImageAdapter(imageList) { position ->
                    val intent = Intent(this@PhotoAlbumActivity, PhotoActivity::class.java)
                    intent.putExtra("images", ArrayList(imageList))
                    intent.putExtra("position", position)
                    startActivity(intent)
                }
            viewBinding.recyclerView.adapter = adapter
        }
    }

    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
            ).toTypedArray()
        const val RELATIVE_PICTURES_PATH = "Pictures/OptigAI/"
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value) {
                    permissionGranted = false
                }
            }
            if (!permissionGranted) {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            } else {
                loadImagesToRecyclerView()
            }
        }

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
        val selectionArgs = arrayOf(RELATIVE_PICTURES_PATH)

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
