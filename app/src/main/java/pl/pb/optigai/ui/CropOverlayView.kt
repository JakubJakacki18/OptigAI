package pl.pb.optigai.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.pow
import kotlin.math.sqrt
import androidx.core.graphics.toColorInt

class CropOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class Handle { NONE, MOVE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, LEFT, TOP, RIGHT, BOTTOM }

    enum class CropRatio(val ratio: Float) {
        FREE(0f),
        SQUARE(1f),
        R_4_3(4f / 3f),
        R_16_9(16f / 9f)
    }

    private val minSize = 100f
    private val touchTolerance = 50f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val handleSize = 20f

    private var imageBounds = RectF(0f, 0f, 0f, 0f)
    private var cropRect = RectF()
    private val tempRect = RectF()
    private val initialBounds = RectF()
    private val fallbackBounds = RectF()
    private var lastX = 0f
    private var lastY = 0f
    private var activeHandle = Handle.NONE
    private var currentCropRatio: CropRatio = CropRatio.FREE

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = ContextCompat.getColor(context, android.R.color.white)

        shadowPaint.color = "#80000000".toColorInt()
        shadowPaint.style = Paint.Style.FILL
    }

    fun getCropRect(): RectF = cropRect

    fun setImageBounds(rect: RectF) {
        if (imageBounds != rect) {
            imageBounds.set(rect)

            var left = cropRect.left.coerceAtLeast(imageBounds.left)
            var top = cropRect.top.coerceAtLeast(imageBounds.top)
            var right = cropRect.right.coerceAtMost(imageBounds.right)
            var bottom = cropRect.bottom.coerceAtMost(imageBounds.bottom)

            if (right - left < minSize) {
                right = (left + minSize).coerceAtMost(imageBounds.right)
                left = right - minSize
            }
            if (bottom - top < minSize) {
                bottom = (top + minSize).coerceAtMost(imageBounds.bottom)
                top = bottom - minSize
            }

            left = left.coerceAtLeast(imageBounds.left)
            top = top.coerceAtLeast(imageBounds.top)
            right = right.coerceAtMost(imageBounds.right)
            bottom = bottom.coerceAtMost(imageBounds.bottom)

            cropRect.set(left, top, right, bottom)

            invalidate()
        }
    }

    fun setCropRatio(ratio: CropRatio) {
        if (currentCropRatio != ratio) {
            currentCropRatio = ratio
            if (imageBounds.width() > 0 && imageBounds.height() > 0) {
                calculateInitialCropRect(imageBounds, ratio, cropRect)
                invalidate()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (cropRect.isEmpty) {
            if (width > 0 && height > 0) {
                val sourceBounds: RectF

                if (imageBounds.width() > 0) {
                    sourceBounds = imageBounds
                } else {
                    fallbackBounds.set(0f, 0f, width.toFloat(), height.toFloat())
                    sourceBounds = fallbackBounds
                }

                calculateInitialCropRect(sourceBounds, currentCropRatio, cropRect)
            } else {
                return
            }
        }

        drawShadow(canvas)

        canvas.drawRect(cropRect, paint)

        paint.style = Paint.Style.FILL
        canvas.drawCircle(cropRect.left, cropRect.top, handleSize / 2, paint)
        canvas.drawCircle(cropRect.right, cropRect.top, handleSize / 2, paint)
        canvas.drawCircle(cropRect.left, cropRect.bottom, handleSize / 2, paint)
        canvas.drawCircle(cropRect.right, cropRect.bottom, handleSize / 2, paint)
        paint.style = Paint.Style.STROKE
    }

    private fun drawShadow(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), cropRect.top, shadowPaint)
        canvas.drawRect(0f, cropRect.bottom, width.toFloat(), height.toFloat(), shadowPaint)
        canvas.drawRect(0f, cropRect.top, cropRect.left, cropRect.bottom, shadowPaint)
        canvas.drawRect(cropRect.right, cropRect.top, width.toFloat(), cropRect.bottom, shadowPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                activeHandle = findHandle(x, y)
                lastX = x
                lastY = y
                return activeHandle != Handle.NONE
            }
            MotionEvent.ACTION_MOVE -> {
                if (activeHandle != Handle.NONE) {
                    val dx = x - lastX
                    val dy = y - lastY
                    updateCropRect(dx, dy)
                    lastX = x
                    lastY = y
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activeHandle = Handle.NONE
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun findHandle(x: Float, y: Float): Handle {
        if (isNear(x, y, cropRect.left, cropRect.top)) return Handle.TOP_LEFT
        if (isNear(x, y, cropRect.right, cropRect.top)) return Handle.TOP_RIGHT
        if (isNear(x, y, cropRect.left, cropRect.bottom)) return Handle.BOTTOM_LEFT
        if (isNear(x, y, cropRect.right, cropRect.bottom)) return Handle.BOTTOM_RIGHT

        if (isNear(x, y, cropRect.left, cropRect.centerY())) return Handle.LEFT
        if (isNear(x, y, cropRect.right, cropRect.centerY())) return Handle.RIGHT
        if (isNear(x, y, cropRect.centerX(), cropRect.top)) return Handle.TOP
        if (isNear(x, y, cropRect.centerX(), cropRect.bottom)) return Handle.BOTTOM

        if (cropRect.contains(x, y)) return Handle.MOVE

        return Handle.NONE
    }

    private fun isNear(x: Float, y: Float, centerX: Float, centerY: Float): Boolean {
        return sqrt((x - centerX).pow(2) + (y - centerY).pow(2)) <= touchTolerance
    }

    private fun calculateInitialCropRect(bounds: RectF, ratio: CropRatio, outRect: RectF) {
        val margin = 50f

        initialBounds.set(bounds.left + margin, bounds.top + margin, bounds.right - margin, bounds.bottom - margin)
        val clampedBounds = initialBounds

        if (ratio == CropRatio.FREE) {
            outRect.set(clampedBounds)
            return
        }

        val ratioValue = ratio.ratio
        val boundsRatio = clampedBounds.width() / clampedBounds.height()
        var targetWidth = clampedBounds.width()
        var targetHeight = clampedBounds.height()

        if (ratioValue > boundsRatio) {
            targetHeight = targetWidth / ratioValue
        } else {
            targetWidth = targetHeight * ratioValue
        }

        val centerX = clampedBounds.centerX()
        val centerY = clampedBounds.centerY()

        outRect.set(
            centerX - targetWidth / 2,
            centerY - targetHeight / 2,
            centerX + targetWidth / 2,
            centerY + targetHeight / 2
        )
    }

    private fun updateCropRect(dx: Float, dy: Float) {
        val ratio = currentCropRatio.ratio
        tempRect.set(cropRect)

        when (activeHandle) {
            Handle.MOVE -> tempRect.offset(dx, dy)
            Handle.LEFT -> tempRect.left = tempRect.left + dx
            Handle.RIGHT -> tempRect.right = tempRect.right + dx
            Handle.TOP -> tempRect.top = tempRect.top + dy
            Handle.BOTTOM -> tempRect.bottom = tempRect.bottom + dy
            Handle.TOP_LEFT -> { tempRect.left += dx; tempRect.top += dy }
            Handle.TOP_RIGHT -> { tempRect.right += dx; tempRect.top += dy }
            Handle.BOTTOM_LEFT -> { tempRect.left += dx; tempRect.bottom += dy }
            Handle.BOTTOM_RIGHT -> { tempRect.right += dx; tempRect.bottom += dy }
            else -> return
        }

        tempRect.left = tempRect.left.coerceAtMost(tempRect.right - minSize)
        tempRect.right = tempRect.right.coerceAtLeast(tempRect.left + minSize)
        tempRect.top = tempRect.top.coerceAtMost(tempRect.bottom - minSize)
        tempRect.bottom = tempRect.bottom.coerceAtLeast(tempRect.top + minSize)

        if (ratio > 0f && activeHandle != Handle.MOVE) {
            enforceAspectRatio(tempRect, ratio, activeHandle)
        }

        if (activeHandle == Handle.MOVE) {
            val rectWidth = tempRect.width()
            val rectHeight = tempRect.height()

            tempRect.left = tempRect.left.coerceAtLeast(imageBounds.left)
            tempRect.top = tempRect.top.coerceAtLeast(imageBounds.top)

            tempRect.right = (tempRect.left + rectWidth).coerceAtMost(imageBounds.right)
            tempRect.bottom = (tempRect.top + rectHeight).coerceAtMost(imageBounds.bottom)

            tempRect.left = tempRect.left.coerceAtMost(tempRect.right - rectWidth)
            tempRect.top = tempRect.top.coerceAtMost(tempRect.bottom - rectHeight)

        } else {
            tempRect.left = tempRect.left.coerceAtLeast(imageBounds.left)
            tempRect.top = tempRect.top.coerceAtLeast(imageBounds.top)
            tempRect.right = tempRect.right.coerceAtMost(imageBounds.right)
            tempRect.bottom = tempRect.bottom.coerceAtMost(imageBounds.bottom)

            tempRect.left = tempRect.left.coerceAtMost(tempRect.right - minSize)
            tempRect.right = tempRect.right.coerceAtLeast(tempRect.left + minSize)
            tempRect.top = tempRect.top.coerceAtMost(tempRect.bottom - minSize)
            tempRect.bottom = tempRect.bottom.coerceAtLeast(tempRect.top + minSize)
        }

        cropRect.set(tempRect)
    }

    private fun enforceAspectRatio(rect: RectF, ratio: Float, handle: Handle) {
        val currentWidth = rect.width()
        val currentHeight = rect.height()

        val constrainByWidth = when(handle) {
            Handle.LEFT, Handle.RIGHT, Handle.TOP_LEFT, Handle.BOTTOM_LEFT, Handle.TOP_RIGHT, Handle.BOTTOM_RIGHT -> true
            else -> false
        }

        if (constrainByWidth) {
            val newHeight = (currentWidth / ratio).coerceAtLeast(minSize)
            when (handle) {
                Handle.TOP_LEFT, Handle.TOP_RIGHT -> rect.top = rect.bottom - newHeight
                Handle.BOTTOM_LEFT, Handle.BOTTOM_RIGHT -> rect.bottom = rect.top + newHeight
                else -> { /* LEFT/RIGHT only modify horizontal dimension, vertical position is fixed */ }
            }
        } else {
            val newWidth = (currentHeight * ratio).coerceAtLeast(minSize)

            val centerX = rect.centerX()
            rect.left = centerX - newWidth / 2
            rect.right = centerX + newWidth / 2
        }

        if (rect.width() < minSize) {
            rect.right = rect.left + minSize
            if (currentCropRatio.ratio > 0) rect.bottom = rect.top + minSize / currentCropRatio.ratio
        }
        if (rect.height() < minSize) {
            rect.bottom = rect.top + minSize
            if (currentCropRatio.ratio > 0) rect.right = rect.left + minSize * currentCropRatio.ratio
        }
    }
}
