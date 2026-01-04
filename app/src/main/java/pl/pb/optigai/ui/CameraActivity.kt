/**
 * CameraActivity
 *
 * Activity responsible for handling the device camera, capturing photos, and initiating image analysis.
 * Supports both saving photos to external storage and temporarily storing them in memory for analysis.
 *
 * Features:
 * - Initializes CameraX with back camera and provides live preview.
 * - Captures photos and either saves them to external storage or caches them in [BitmapCache].
 * - Supports pinch-to-zoom and a zoom SeekBar with configurable visibility modes.
 * - Allows toggling flash modes (Auto, On, Off).
 * - Provides buttons to open gallery ([PhotoAlbumActivity]) and settings ([SettingsActivity]).
 * - Automatically rotates captured images according to device orientation.
 * - Initiates [AnalysisActivity] after capturing a photo.
 *
 * Collaborates with:
 * - [SettingsViewModel] – stores user preferences such as zoom bar mode and photo saving behavior.
 * - [BitmapCache] – stores temporarily captured bitmap images.
 * - [PermissionHandler] – checks and requests camera permissions.
 * - CameraX APIs – [ImageCapture], [Preview], [ProcessCameraProvider], [CameraSelector].
 * - [MediaStore] – saves captured photos to device storage.
 * - [UCrop] – not used here, but cropping is handled in other fragments if needed.
 */
package pl.pb.optigai.ui

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.ScaleGestureDetector
import android.view.View
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
    /** View binding for the camera activity layout. */
    private lateinit var viewBinding: ActivityCameraBinding
    /** ViewModel storing user settings (zoom mode, photo saving preferences). */
    private val viewModel: SettingsViewModel by viewModels()
    /** CameraX ImageCapture instance used to take photos. */
    private var imageCapture: ImageCapture? = null
    /** Current flash mode (Auto, On, Off) applied to [imageCapture]. */
    private var flashMode: Int? = null
    /** SeekBar for zoom control. */
    private lateinit var zoomSeekBar: SeekBar
    /** Gesture detector for pinch-to-zoom functionality. */
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    /** CameraX Camera instance controlling zoom and other camera features. */
    private lateinit var camera: androidx.camera.core.Camera
    /**
     * Initializes the activity, sets up the camera preview, zoom controls, flash toggle, and
     * button listeners for taking photos, opening gallery, and opening settings.
     *
     * Handles permissions via [PermissionHandler] and [ActivityResultContracts.RequestMultiplePermissions].
     */
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
        zoomSeekBar = viewBinding.zoomSeekBar!!

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
                    else -> {
                        viewBinding.zoomSeekBar!!.visibility = View.GONE
                    }
                }
            }
        }

        viewBinding.flashToggleButton!!.setOnClickListener {
            setFlashMode()
        }
        setFlashMode()
        scaleGestureDetector =
            ScaleGestureDetector(
                this,
                object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    override fun onScale(detector: ScaleGestureDetector): Boolean {
                        val zoomState = camera.cameraInfo.zoomState.value ?: return false
                        val currentZoom = zoomState.zoomRatio
                        val newZoom =
                            (currentZoom * detector.scaleFactor)
                                .coerceIn(zoomState.minZoomRatio, zoomState.maxZoomRatio)

                        camera.cameraControl.setZoomRatio(newZoom)

                        val linearZoom =
                            (newZoom - zoomState.minZoomRatio) /
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
                },
            )

        viewBinding.cameraPreviewView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }
    }
    /**
     * Toggles the flash mode between Auto, On, and Off.
     * Updates the UI icon and description accordingly.
     */
    private fun setFlashMode() {
        val flashButton = viewBinding.flashToggleButton!!
        flashMode =
            when (flashMode) {
                ImageCapture.FLASH_MODE_AUTO -> {
                    flashButton.setImageResource(R.drawable.ic_flash_on)
                    flashButton.contentDescription = getString(R.string.flash_on_description)
                    ImageCapture.FLASH_MODE_ON
                }
                ImageCapture.FLASH_MODE_ON -> {
                    flashButton.setImageResource(R.drawable.ic_flash_off)
                    flashButton.contentDescription = getString(R.string.flash_off_description)
                    ImageCapture.FLASH_MODE_OFF
                }
                else -> {
                    flashButton.setImageResource(R.drawable.ic_flash_auto)
                    flashButton.contentDescription = getString(R.string.flash_auto_description)
                    ImageCapture.FLASH_MODE_AUTO
                }
            }
        imageCapture?.flashMode = flashMode!!
    }
    /**
     * ActivityResult launcher for requesting multiple permissions (camera).
     * Starts the camera if permissions are granted.
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
                lifecycleScope.launch { startCamera() }
            }
        }

    companion object {
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA,
            ).toTypedArray()
    }
    /**
     * Initializes CameraX, sets up preview and [imageCapture], binds lifecycle to this activity.
     * Also sets up initial zoom ratio and zoom controls.
     */
    private suspend fun startCamera() {
        val cameraProvider = ProcessCameraProvider.getInstance(this).await()
        val preview = Preview.Builder().build()
        preview.surfaceProvider = viewBinding.cameraPreviewView.surfaceProvider

        imageCapture =
            ImageCapture
                .Builder()
                .setFlashMode(flashMode!!)
                .build()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            camera =
                cameraProvider.bindToLifecycle(
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
    /**
     * Captures a photo using [imageCapture].
     * Decides whether to save the photo to external storage or to temporary bitmap based on user settings.
     */
    private fun takePhoto() {
        lifecycleScope.launch {
            if (getIsSavingPhoto()) {
                takePhotoAndSaveToExternalStorage()
            } else {
                takePhotoAndSaveToTemporaryBitmap()
            }
        }
    }
    /**
     * Builds the output file options for saving a photo to MediaStore.
     *
     * @return [ImageCapture.OutputFileOptions] configured with display name, MIME type, and relative path.
     */
    private fun getOutputFileOptions(): ImageCapture.OutputFileOptions {
        val locale = Locale.getDefault()
        val name = SimpleDateFormat(FILENAME_FORMAT, locale).format(System.currentTimeMillis())
        val contentValues =
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/OptigAI")
            }
        return ImageCapture.OutputFileOptions
            .Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()
    }
    /**
     * Takes a photo and saves it to external storage.
     * Starts analysis after saving the photo.
     */
    private fun takePhotoAndSaveToExternalStorage() {
        imageCapture?.takePicture(
            getOutputFileOptions(),
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Toast
                        .makeText(
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

    /**
     * Takes a photo and stores it temporarily in [BitmapCache].
     * Starts analysis using the in-memory bitmap.
     */
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
                    Toast
                        .makeText(
                            this@CameraActivity,
                            "Photo capture failed: ${exception.message}",
                            Toast.LENGTH_LONG,
                        ).show()
                }
            },
        )
    }
    /**
     * Starts [AnalysisActivity] for a captured image.
     *
     * @param data Either a [Uri] for saved image or a [Bitmap] for in-memory image.
     * @throws IllegalArgumentException if data type is unsupported.
     */
    private fun startAnalysis(data: Any) {
        val intent = Intent(this, AnalysisActivity::class.java)
        when (data) {
            is Uri -> intent.putExtra("IMAGE_URI", data.toString())
            is Bitmap -> Unit
            else -> throw IllegalArgumentException("Unsupported data type")
        }
        startActivity(intent)
    }
    /** Runnable that hides the zoom SeekBar after a delay when in AUTO mode. */

    private val hideZoomBarRunnable =
        Runnable {
            viewBinding.zoomSeekBar!!.visibility = View.GONE
        }
    /**
     * Sets up zoom controls for [zoomSeekBar].
     * Synchronizes SeekBar progress with camera zoom and supports auto-hide in AUTO mode.
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupZoomControls() {
        zoomSeekBar.max = 100
        zoomSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    if (!fromUser) return
                    val zoomState = camera.cameraInfo.zoomState.value ?: return
                    val zoomRatio =
                        zoomState.minZoomRatio +
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
            },
        )
    }
    /**
     * Rotates a [Bitmap] by the given degrees.
     *
     * @param degrees Rotation in degrees.
     * @return Rotated [Bitmap].
     */
    private fun Bitmap.rotate(degrees: Int): Bitmap {
        if (degrees == 0) return this
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
    /**
     * Checks user settings to determine if photos should be saved to storage or temporarily cached.
     *
     * @return True if saving to external storage, false if caching temporarily.
     */
    private suspend fun getIsSavingPhoto(): Boolean = viewModel.isPhotoSaving.first()
}
