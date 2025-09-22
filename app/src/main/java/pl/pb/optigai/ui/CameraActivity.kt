package pl.pb.optigai.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.ScaleGestureDetector
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import pl.pb.optigai.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var flashButton: ImageButton
    private lateinit var takePhotoButton: ImageButton
    private lateinit var zoomSeekBar: SeekBar

    private lateinit var camera: Camera
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    companion object {
        private const val TAG = "CameraActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.cameraPreviewView)
        flashButton = findViewById(R.id.flashToggleButton)
        takePhotoButton = findViewById(R.id.takePhotoButton)
        zoomSeekBar = findViewById(R.id.zoomSeekBar)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        setupPinchToZoom()
        setupZoomSeekBar()
        setupFlashToggle()
        setupTakePhoto()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupPinchToZoom() {
        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoom = camera.cameraInfo.zoomState.value?.zoomRatio ?: 1f
                camera.cameraControl.setZoomRatio(currentZoom * detector.scaleFactor)
                updateZoomSeekBar()
                return true
            }
        })

        previewView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun setupZoomSeekBar() {
        zoomSeekBar.max = 100
        zoomSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val zoomState = camera.cameraInfo.zoomState.value ?: return
                val zoom = zoomState.minZoomRatio + (zoomState.maxZoomRatio - zoomState.minZoomRatio) * (progress / 100f)
                camera.cameraControl.setZoomRatio(zoom)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateZoomSeekBar() {
        val zoomState = camera.cameraInfo.zoomState.value ?: return
        val zoom = camera.cameraInfo.zoomState.value?.zoomRatio ?: 1f
        val progress = ((zoom - zoomState.minZoomRatio) / (zoomState.maxZoomRatio - zoomState.minZoomRatio) * 100).toInt()
        zoomSeekBar.progress = progress
    }

    private fun setupFlashToggle() {
        flashButton.setOnClickListener {
            val currentTorch = camera.cameraInfo.torchState.value
            camera.cameraControl.enableTorch(currentTorch != TorchState.ON)
        }
    }

    private fun setupTakePhoto() {
        takePhotoButton.setOnClickListener {
            val imageCapture = ImageCapture.Builder().build()
            val photoFile = File(
                externalMediaDirs.first(),
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpg"
            )
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Toast.makeText(baseContext, "Photo saved: ${photoFile.absolutePath}", Toast.LENGTH_SHORT).show()
                    }
                    override fun onError(exception: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    }
                }
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
