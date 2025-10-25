package pl.pb.optigai.ui

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import coil.load
import pl.pb.optigai.databinding.ActivityImageEditorBinding
import java.io.File
import java.io.FileOutputStream

class ImageEditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageEditorBinding
    private lateinit var imageUri: Uri
    private var currentRotation = 0f

    enum class EditorMode { CROP, ROTATE }
    private var currentMode = EditorMode.CROP

    private val rotationStep = 15f
    private val maxRotationValue = 360f
    private val maxSeekBarProgress = (maxRotationValue / rotationStep).toInt() * 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageUri = intent.getStringExtra("IMAGE_URI")!!.toUri()
        binding.photoView.load(imageUri) {
            // Optionally ensure PhotoView starts with a default fit scale
            // Note: PhotoView usually defaults to FIT_CENTER, which is fine,
            // but ensure you aren't doing any conflicting initial setup.
            listener(onSuccess = { _, _ ->
                setupPhotoViewBoundsListener()
            })
        }

        binding.photoView.maximumScale = 6f
        binding.photoView.minimumScale = 1f
        binding.rotationSeekBar.max = maxSeekBarProgress
        binding.rotationSeekBar.progress = maxSeekBarProgress / 2

        setupCropListeners()
        setupRotationListeners()
        setupModeListeners()
        setEditorMode(EditorMode.CROP)

        binding.btnCrop.setOnClickListener { cropVisibleArea() }
        binding.btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun setupCropListeners() {
        val free = CropOverlayView.CropRatio.FREE
        val square = CropOverlayView.CropRatio.SQUARE
        val r43 = CropOverlayView.CropRatio.R_4_3
        val r169 = CropOverlayView.CropRatio.R_16_9

        val ratioClickListener: (CropOverlayView.CropRatio) -> View.OnClickListener = { ratio ->
            View.OnClickListener {
                binding.cropOverlay.setCropRatio(ratio)

                // Immediately force the new ratio to fit inside the photo boundaries
                binding.photoView.displayRect?.let { bounds ->
                    binding.cropOverlay.scaleToFitBounds(bounds)
                }
            }
        }
        binding.btnRatioFree.setOnClickListener(ratioClickListener(free))
        binding.btnRatio11.setOnClickListener(ratioClickListener(square))
        binding.btnRatio43.setOnClickListener(ratioClickListener(r43))
        binding.btnRatio169.setOnClickListener(ratioClickListener(r169))
    }

    private fun setupRotationListeners() {
        // These now call rotateBy(), which updates the seek bar and triggers the core logic.
        binding.btnRotateLeft.setOnClickListener { rotateBy(currentRotation - 90f) }
        binding.btnRotateRight.setOnClickListener { rotateBy(currentRotation + 90f) }
    }

    private fun setupModeListeners() {
        binding.btnToggleMode.setOnClickListener {
            val newMode = if (currentMode == EditorMode.CROP) EditorMode.ROTATE else EditorMode.CROP
            setEditorMode(newMode)
        }
    }

    private fun setEditorMode(mode: EditorMode) {
        currentMode = mode

        binding.cropRatioControls.visibility = if (mode == EditorMode.CROP) View.VISIBLE else View.GONE
        binding.rotationControls.visibility = if (mode == EditorMode.ROTATE) View.VISIBLE else View.GONE
        binding.btnToggleMode.text = if (mode == EditorMode.CROP) "ROTATE" else "CROP"

        if (mode == EditorMode.CROP) {
            binding.photoView.isZoomable = true
        } else {
            binding.photoView.isZoomable = false
        }

        binding.cropOverlay.visibility = View.VISIBLE

    }

    private fun rotateBy(newRotation: Float) {
        var normalizedRotation = newRotation % 360
        if (normalizedRotation > 180) normalizedRotation -= 360
        if (normalizedRotation <= -180) normalizedRotation += 360

        val targetProgressFloat = (normalizedRotation / rotationStep) + (maxSeekBarProgress / 2)
        val targetProgress = targetProgressFloat.toInt().coerceIn(0, maxSeekBarProgress)

        binding.rotationSeekBar.progress = targetProgress
    }

    private fun setupPhotoViewBoundsListener() {
        // Initial setup (called once)
        binding.photoView.post {
            binding.photoView.displayRect?.let { rect ->
                binding.cropOverlay.setImageBounds(RectF(rect))
                binding.cropOverlay.setCropRatio(CropOverlayView.CropRatio.FREE)
                fitPhotoInCropBounds() // Initial fit
            }
        }

        // PhotoView updates cropOverlay's imageBounds whenever its matrix changes (zoom/pan/rotate)
        binding.photoView.setOnMatrixChangeListener { rect ->
            binding.cropOverlay.setImageBounds(rect)
        }

        binding.rotationSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val discreteProgress = progress - (maxSeekBarProgress / 2)
                currentRotation = discreteProgress * rotationStep

                binding.photoView.rotation = currentRotation

                // Skalujemy croppera natychmiast, aby zawsze mieścił się w obracanym zdjęciu
                binding.photoView.post {
                    scaleCropToFitImage()
                }
            }




            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val targetProgress = (currentRotation / rotationStep).toInt() + (maxSeekBarProgress / 2)
                seekBar?.progress = targetProgress.coerceIn(0, maxSeekBarProgress)
            }
        })
    }

    private fun cropVisibleArea() {
        val drawable = binding.photoView.drawable ?: return
        val bitmap = (drawable as BitmapDrawable).bitmap

        val matrix = Matrix().apply { postRotate(currentRotation) }
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        val displayRect = binding.photoView.displayRect
        val cropRect = binding.cropOverlay.getCropRect()

        val offsetX = displayRect.left
        val offsetY = displayRect.top
        val viewScaleX = displayRect.width() / rotatedBitmap.width.toFloat()
        val viewScaleY = displayRect.height() / rotatedBitmap.height.toFloat()

        val unTranslatedLeft = cropRect.left - offsetX
        val unTranslatedTop = cropRect.top - offsetY
        val unTranslatedRight = cropRect.right - offsetX
        val unTranslatedBottom = cropRect.bottom - offsetY

        val left = (unTranslatedLeft / viewScaleX).toInt().coerceIn(0, rotatedBitmap.width)
        val top = (unTranslatedTop / viewScaleY).toInt().coerceIn(0, rotatedBitmap.height)
        val right = (unTranslatedRight / viewScaleX).toInt().coerceIn(0, rotatedBitmap.width)
        val bottom = (unTranslatedBottom / viewScaleY).toInt().coerceIn(0, rotatedBitmap.height)

        val width = (right - left).coerceAtLeast(0)
        val height = (bottom - top).coerceAtLeast(0)

        val finalWidth = width.coerceAtMost(rotatedBitmap.width - left)
        val finalHeight = height.coerceAtMost(rotatedBitmap.height - top)

        if (finalWidth <= 0 || finalHeight <= 0) return

        val cropped = Bitmap.createBitmap(rotatedBitmap, left, top, finalWidth, finalHeight)

        val file = File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { cropped.compress(Bitmap.CompressFormat.JPEG, 90, it) }

        val resultUri = Uri.fromFile(file)
        val intent = Intent().apply { putExtra("CROPPED_URI", resultUri.toString()) }
        setResult(RESULT_OK, intent)
        finish()
    }
    // In ImageEditorActivity.kt

    private fun fitPhotoInCropBounds() {
        val cropRect = binding.cropOverlay.getCropRect()
        val displayRect = binding.photoView.displayRect ?: return

        val scaleX = cropRect.width() / displayRect.width()
        val scaleY = cropRect.height() / displayRect.height()
        val scale = maxOf(scaleX, scaleY) // Use maxOf to COVER the crop

        val currentScale = binding.photoView.scale
        val newScale = (currentScale * scale)
            .coerceIn(binding.photoView.minimumScale, binding.photoView.maximumScale)

        val matrix = Matrix(binding.photoView.imageMatrix)
        matrix.postScale(newScale / currentScale, newScale / currentScale, displayRect.centerX(), displayRect.centerY())

        val newDisplayRect = RectF(displayRect)
        matrix.mapRect(newDisplayRect)

        val dx = cropRect.centerX() - newDisplayRect.centerX()
        val dy = cropRect.centerY() - newDisplayRect.centerY()
        matrix.postTranslate(dx, dy)

        binding.photoView.setDisplayMatrix(matrix)
    }
    private fun scaleCropToFitImage() {
        val bounds = binding.photoView.displayRect ?: return

        // Używamy getterów CropOverlayView
        val ratioValue = binding.cropOverlay.getCurrentCropRatio().ratio
        if (ratioValue == 0f) return // FREE ratio — nic nie zmieniamy

        binding.cropOverlay.scaleToFitBounds(bounds)
    }


}