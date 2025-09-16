package pl.pb.optigai.ui
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = PhotoAlbumBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if (!PermissionHandler.hasPermissions(baseContext, REQUIRED_PERMISSIONS)) {
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            loadImages()
        }

        viewBinding.headerTitle.text = getString(R.string.analysis_header_shared)
        viewBinding.backButton.setOnClickListener {
            finish()
        }

        viewBinding.layoutButton.setOnClickListener {
            lifecycleScope.launch {
                val isGridView = getIsGridView()
                viewModel.setIsGridView(!isGridView)
                updateRecyclerView()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadImages()
    }

    private fun loadImages() {
        lifecycleScope.launch {
            imageList =
                withContext(Dispatchers.IO) {
                    PhotoUtils.imageReader(this@PhotoAlbumActivity)
                }
            updateRecyclerView()
        }
    }

    private suspend fun getIsGridView(): Boolean = viewModel.isGridView.first()

    private fun updateRecyclerView() {
        if (!::imageList.isInitialized) return

        lifecycleScope.launch {
            val isGridView = getIsGridView()
            if (isGridView) {
                viewBinding.recyclerView.layoutManager = GridLayoutManager(this@PhotoAlbumActivity, 2)
                viewBinding.layoutButton.text = "2"
            } else {
                viewBinding.recyclerView.layoutManager = LinearLayoutManager(this@PhotoAlbumActivity)
                viewBinding.layoutButton.text = "1"
            }
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
