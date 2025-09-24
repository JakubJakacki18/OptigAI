package pl.pb.optigai.ui

import android.annotation.SuppressLint
import android.view.View
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.ScaleGestureDetector
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pl.pb.optigai.R
import pl.pb.optigai.Settings
import pl.pb.optigai.databinding.ActivityCameraBinding
import pl.pb.optigai.utils.PermissionHandler
import pl.pb.optigai.utils.data.BitmapCache
import pl.pb.optigai.utils.data.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.getValue

class CameraActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityCameraBinding
    private val viewModel: SettingsViewModel by viewModels()
    private var imageCapture: ImageCapture? = null
    private var flashMode = ImageCapture.FLASH_MODE_AUTO
    private lateinit var zoomSeekBar: SeekBar
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var camera: androidx.camera.core.Camera

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if (!PermissionHandler.hasPermissions(baseContext, REQUIRED_PERMISSIONS)) {
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        } else {
            lifecycleScope.launch { startCamera() }
        }

        viewBinding.takePhotoButton.setOnClickListener { takePhoto() }

        viewBinding.openGalleryButton.setOnClickListener {
            val intent = Intent(this, PhotoAlbumActivity::class.java)
            startActivity(intent)
        }

        viewBinding.openSettingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        lifecycleScope.launch {
            viewModel.zoomSeekBarMode.collect { mode ->
                when (mode) {
                    Settings.ZoomSeekBarMode.ALWAYS_ON -> {
                        viewBinding.zoomSeekBar!!.visibility = View.VISIBLE
                    }
                    Settings.ZoomSeekBarMode.ALWAYS_OFF -> {
                        viewBinding.zoomSeekBar!!.visibility = View.GONE
                    }
                    Settings.ZoomSeekBarMode.AUTO -> {
                        viewBinding.zoomSeekBar!!.visibility = View.GONE
                    }
                    Settings.ZoomSeekBarMode.UNRECOGNIZED -> {
                        viewBinding.zoomSeekBar!!.visibility = View.GONE
                    }
                }
            }
        }



        val flashButton = viewBinding.flashToggleButton!!

        flashButton.setOnClickListener {
            flashMode = when (flashMode) {
                ImageCapture.FLASH_MODE_AUTO -> {
                    flashButton.setImageResource(R.drawable.ic_flash_on)
                    ImageCapture.FLASH_MODE_ON
                }
                ImageCapture.FLASH_MODE_ON -> {
                    flashButton.setImageResource(R.drawable.ic_flash_off)
                    ImageCapture.FLASH_MODE_OFF
                }
                else -> {
                    flashButton.setImageResource(R.drawable.ic_flash_auto)
                    ImageCapture.FLASH_MODE_AUTO
                }
            }
            imageCapture?.flashMode = flashMode
        }
        scaleGestureDetector = ScaleGestureDetector(this,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val zoomState = camera.cameraInfo.zoomState.value ?: return false
                    val currentZoom = zoomState.zoomRatio
                    val newZoom = (currentZoom * detector.scaleFactor)
                        .coerceIn(zoomState.minZoomRatio, zoomState.maxZoomRatio)

                    camera.cameraControl.setZoomRatio(newZoom)

                    val linearZoom = (newZoom - zoomState.minZoomRatio) /
                            (zoomState.maxZoomRatio - zoomState.minZoomRatio)
                    zoomSeekBar.progress = (linearZoom * 100).toInt()

                    lifecycleScope.launch {
                        if (viewModel.zoomSeekBarMode.first() == Settings.ZoomSeekBarMode.AUTO) {
                            zoomSeekBar.visibility = View.VISIBLE
                            zoomSeekBar.removeCallbacks(hideZoomBarRunnable)
                            zoomSeekBar.postDelayed(hideZoomBarRunnable, 1000)
                        }
                    }
                    return true
                }
            })

        viewBinding.cameraPreviewView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }
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
                lifecycleScope.launch { startCamera() }
            }
        }

    companion object {
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS = mutableListOf(
            android.Manifest.permission.CAMERA,
        ).toTypedArray()
    }

    private suspend fun startCamera() {
        val cameraProvider = ProcessCameraProvider.getInstance(this).await()
        val preview = Preview.Builder().build()
        preview.surfaceProvider = viewBinding.cameraPreviewView.surfaceProvider

        imageCapture = ImageCapture.Builder()
            .setFlashMode(flashMode)
            .build()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture,
            )
            camera.cameraControl.setZoomRatio(1.0f)
            setupZoomControls()

        } catch (exc: Exception) {
            Toast.makeText(this, "Camera initialization failed: ${exc.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun takePhoto() {
        lifecycleScope.launch {
            if (getIsSavingPhoto()) {
                takePhotoAndSaveToExternalStorage()
            } else {
                takePhotoAndSaveToTemporaryBitmap()
            }
        }
    }

    private fun getOutputFileOptions(): ImageCapture.OutputFileOptions {
        val locale = Locale.getDefault()
        val name = SimpleDateFormat(FILENAME_FORMAT, locale).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/OptigAI")
        }
        return ImageCapture.OutputFileOptions
            .Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()
    }

    private fun takePhotoAndSaveToExternalStorage() {
        imageCapture?.takePicture(
            getOutputFileOptions(),
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        "Photo capture failed: ${exception.message}",
                        Toast.LENGTH_LONG,
                    ).show()
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri
                    if (savedUri != null) {
                        startAnalysis(savedUri)
                    } else {
                        Toast.makeText(this@CameraActivity, "Error saving photo", Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )
    }

    private fun takePhotoAndSaveToTemporaryBitmap() {
        imageCapture?.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    val bitmap = imageProxy.toBitmap().rotate(rotationDegrees)
                    imageProxy.close()
                    BitmapCache.bitmap = bitmap
                    startAnalysis(bitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        "Photo capture failed: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
        )
    }

    private fun startAnalysis(data: Any) {
        val intent = Intent(this, AnalysisActivity::class.java)
        when (data) {
            is Uri -> intent.putExtra("IMAGE_URI", data.toString())
            is Bitmap -> Unit
            else -> throw IllegalArgumentException("Unsupported data type")
        }
        startActivity(intent)
    }
    private val hideZoomBarRunnable = Runnable {
        viewBinding.zoomSeekBar!!.visibility = View.GONE
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupZoomControls() {
        val zoomSeekBar = viewBinding.zoomSeekBar!!  // non-null

        zoomSeekBar.max = 100
        zoomSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                val zoomState = camera.cameraInfo.zoomState.value ?: return
                val zoomRatio = zoomState.minZoomRatio +
                        (zoomState.maxZoomRatio - zoomState.minZoomRatio) * (progress / 100f)
                camera.cameraControl.setZoomRatio(zoomRatio)

                lifecycleScope.launch {
                    if (viewModel.zoomSeekBarMode.first() == Settings.ZoomSeekBarMode.AUTO) {
                        zoomSeekBar.visibility = View.VISIBLE
                        zoomSeekBar.removeCallbacks(hideZoomBarRunnable)
                        zoomSeekBar.postDelayed(hideZoomBarRunnable, 1000)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun Bitmap.rotate(degrees: Int): Bitmap {
        if (degrees == 0) return this
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    private suspend fun getIsSavingPhoto(): Boolean = viewModel.isPhotoSaving.first()
}
