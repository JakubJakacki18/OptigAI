package pl.pb.optigai.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.pb.optigai.R
import pl.pb.optigai.databinding.PhotoAlbumBinding
import pl.pb.optigai.utils.PermissionHandler
import pl.pb.optigai.utils.PhotoUtils
import pl.pb.optigai.utils.data.Image
import pl.pb.optigai.utils.data.SettingsViewModel
import kotlin.getValue

class PhotoAlbumActivity : AppCompatActivity() {
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var viewBinding: PhotoAlbumBinding
    private lateinit var imageList: List<Image>

    /**
     * Initializes the activity, sets up view binding, handles permissions, and loads images.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = PhotoAlbumBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if (!PermissionHandler.hasPermissions(baseContext, REQUIRED_PERMISSIONS)) {
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            loadImages()
        }

        val headerTitle: TextView = viewBinding.headerLayout.headerTitle
        headerTitle.text = getString(R.string.gallery_header_shared)

        val backButton: View = viewBinding.headerLayout.headerTitle
        backButton.setOnClickListener {
            finish()
        }
    }

    /**
     * Called when the activity resumes. Reloads images to reflect any changes.
     */
    override fun onResume() {
        super.onResume()
        loadImages()
    }

    /**
     * Loads images from the device's storage on a background thread.
     */
    private fun loadImages() {
        lifecycleScope.launch {
            imageList =
                withContext(Dispatchers.IO) {
                    PhotoUtils.imageReader(this@PhotoAlbumActivity)
                }
            updateRecyclerView()
        }
    }

    /**
     * Retrieves the preferred number of grid columns from the ViewModel.
     * @return The number of columns as an integer.
     */
    private suspend fun getGridColumns(): Int = viewModel.gridColumns.first()

    /**
     * Sets the layout manager for the RecyclerView based on the number of grid columns.
     */
    private fun updateRecyclerView() {
        if (!::imageList.isInitialized) return

        lifecycleScope.launch {
            val gridColumns = getGridColumns()
            viewBinding.recyclerView.layoutManager = GridLayoutManager(this@PhotoAlbumActivity, gridColumns)
        }

        val adapter =
            ImageAdapter(imageList) { position ->
                val intent = Intent(this, PhotoActivity::class.java)
                intent.putExtra("position", position)
                startActivity(intent)
            }
        viewBinding.recyclerView.adapter = adapter
    }

    companion object {
        private val REQUIRED_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mutableListOf(
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                ).toTypedArray()
            } else {
                mutableListOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                ).toTypedArray()
            }
        const val RELATIVE_PICTURES_PATH = "Pictures/OptigAI/"
    }

    /**
     * Registers a callback for handling permission request results.
     */
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
                loadImages()
            }
        }
}
