package pl.pb.optigai.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat

import androidx.core.graphics.toColorInt

class CropOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class CropRatio(val ratio: Float) {
        FREE(0f), SQUARE(1f), R_4_3(4f / 3f), R_16_9(16f / 9f)
    }

    private val minSize = 100f
    private val handleSize = 20f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = ContextCompat.getColor(context, android.R.color.white)
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = "#80000000".toColorInt()
    }

    private var imageBounds = RectF(0f, 0f, 0f, 0f)
    private var cropRect = RectF()
    private var currentCropRatio: CropRatio = CropRatio.FREE


    fun getCropRect(): RectF = cropRect
    fun getCurrentCropRatio(): CropRatio = currentCropRatio

    fun setCropRatio(ratio: CropRatio) {
        currentCropRatio = ratio
        scaleToFitBounds(imageBounds)
    }
    fun setImageBounds(bounds: RectF) {
        imageBounds.set(bounds)
        scaleToFitBounds(bounds)
    }

    // Skalowanie croppera do podanych bounds (np. przy rotacji)
    fun scaleToFitBounds(bounds: RectF) {
        if (bounds.isEmpty) return

        val ratioValue = currentCropRatio.ratio
        var maxWidth = bounds.width()
        var maxHeight = bounds.height()

        if (ratioValue > 0f) {
            val boundsRatio = maxWidth / maxHeight
            if (ratioValue > boundsRatio) {
                maxHeight = maxWidth / ratioValue
            } else {
                maxWidth = maxHeight * ratioValue
            }
        }

        maxWidth = maxWidth.coerceAtLeast(minSize)
        maxHeight = maxHeight.coerceAtLeast(minSize)

        val centerX = bounds.centerX()
        val centerY = bounds.centerY()
        cropRect.set(
            centerX - maxWidth / 2,
            centerY - maxHeight / 2,
            centerX + maxWidth / 2,
            centerY + maxHeight / 2
        )
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (cropRect.isEmpty) return

        // Shadow
        canvas.drawRect(0f, 0f, width.toFloat(), cropRect.top, shadowPaint)
        canvas.drawRect(0f, cropRect.bottom, width.toFloat(), height.toFloat(), shadowPaint)
        canvas.drawRect(0f, cropRect.top, cropRect.left, cropRect.bottom, shadowPaint)
        canvas.drawRect(cropRect.right, cropRect.top, width.toFloat(), cropRect.bottom, shadowPaint)

        // Rectangle
        canvas.drawRect(cropRect, paint)

        // Handles
        paint.style = Paint.Style.FILL
        canvas.drawCircle(cropRect.left, cropRect.top, handleSize / 2, paint)
        canvas.drawCircle(cropRect.right, cropRect.top, handleSize / 2, paint)
        canvas.drawCircle(cropRect.left, cropRect.bottom, handleSize / 2, paint)
        canvas.drawCircle(cropRect.right, cropRect.bottom, handleSize / 2, paint)
        paint.style = Paint.Style.STROKE
    }

    // Optional: touch handling (MOVE / resize) – nie zmienione tutaj
    override fun onTouchEvent(event: MotionEvent): Boolean { return false }
}
