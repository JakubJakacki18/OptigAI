package pl.pb.optigai.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.customview.widget.ExploreByTouchHelper
import pl.pb.optigai.R
import pl.pb.optigai.utils.AppLogger
import pl.pb.optigai.utils.data.DetectionResult
import java.util.Locale

class DetectionOverlay(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val detectionResultsAndColors = mutableListOf<Pair<DetectionResult, Int>>()
    private var imageWidth = 0
    private var imageHeight = 0
    private var imageView: ImageView? = null

    private var colorsAvailable: List<Int?> = emptyList()

    private val boxPaint =
        Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

    private val textPaint =
        Paint().apply {
            color = Color.WHITE
            textSize = 42f
            typeface = Typeface.DEFAULT_BOLD
        }

    fun setDetections(
        detections: List<DetectionResult>,
        imageWidth: Int,
        imageHeight: Int,
        imageView: ImageView,
    ) {
        detectionResultsAndColors.clear()
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        this.imageView = imageView
        detectionResultsAndColors.addAll(
            detections.map
                { detectionResult ->
                    detectionResult to (colorsAvailable.randomOrNull() ?: Color.BLUE)
                },
        )
        invalidate()
        // accessibilityHelper.invalidateRoot()
    }

    private fun mapRectToView(rect: RectF): RectF {
        val matrix = imageView!!.imageMatrix
        val mapped = RectF(rect)
        matrix.mapRect(mapped)
        val viewRect =
            RectF(
                imageView!!.paddingLeft.toFloat(),
                imageView!!.paddingTop.toFloat(),
                (imageView!!.width - imageView!!.paddingRight).toFloat(),
                (imageView!!.height - imageView!!.paddingBottom).toFloat(),
            )
        mapped.offset(viewRect.left, viewRect.top)
        return mapped
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        AppLogger.i("Image size: ${imageWidth}x$imageHeight")
        AppLogger.i("View size: ${width}x$height")
        AppLogger.i("ImageView size: ${imageView?.width}x${imageView?.height}")
        for (detectionResultAndColor in detectionResultsAndColors) {
            val detectionResult = detectionResultAndColor.first
            val color = detectionResultAndColor.second

            val boundingBox = detectionResult.boundingBox ?: continue
            AppLogger.i(detectionResult.boundingBox.toString())
            val rectangle =
                mapRectToView(
                    boundingBox,
                )
            AppLogger.i(rectangle.toString())
            AppLogger.i("Colors available: $colorsAvailable")
            boxPaint.color = color
            AppLogger.i("Using color: ${boxPaint.color}")
            canvas.drawRect(rectangle, boxPaint)
            val overlayText = getOverlayText(detectionResult)
            canvas.drawText(
                overlayText,
                rectangle.left,
                rectangle.top - 10,
                textPaint,
            )
        }
    }

    fun setAvailableColors(colorsAvailable: List<Int?>) {
        this.colorsAvailable = colorsAvailable
    }

    private val accessibilityHelper =
        object : ExploreByTouchHelper(this) {
            // returns index of the detectionResult which was touched
            override fun getVirtualViewAt(
                x: Float,
                y: Float,
            ): Int {
                // indices - returns range of detectionResultsAndColors list
                for (i in detectionResultsAndColors.indices) {
                    val rect = mapRectToView(detectionResultsAndColors[i].first.boundingBox ?: continue)
                    if (rect.contains(x, y)) return i
                }
                return INVALID_ID
            }

            // Thanks to this method, TalkBack will know how many virtual elements are in the view
            override fun getVisibleVirtualViews(virtualViewIds: MutableList<Int>) {
                virtualViewIds.addAll(detectionResultsAndColors.indices)
            }

            override fun onPopulateNodeForVirtualView(
                virtualViewId: Int,
                info: AccessibilityNodeInfoCompat,
            ) {
                val detectionResult = detectionResultsAndColors.getOrNull(virtualViewId)?.first ?: return
                val rect = mapRectToView(detectionResult.boundingBox ?: return)
                info.contentDescription = getOverlayText(detectionResult, R.string.detection_result_bounding_box_data_description)
                @Suppress("DEPRECATION")
                info.setBoundsInParent(
                    Rect(
                        rect.left.toInt(),
                        rect.top.toInt(),
                        rect.right.toInt(),
                        rect.bottom.toInt(),
                    ),
                )

                info.isClickable = true
                info.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK)
            }

            // Handle click action on bounding box
            override fun onPerformActionForVirtualView(
                virtualViewId: Int,
                action: Int,
                arguments: Bundle?,
            ): Boolean {
                if (action == AccessibilityNodeInfo.ACTION_CLICK) {
                    // Vibrate on click
                    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    return true
                }
                return false
            }
        }

    init {
        ViewCompat.setAccessibilityDelegate(this, accessibilityHelper)
        isFocusable = true
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    override fun onHoverEvent(event: MotionEvent): Boolean = accessibilityHelper.dispatchHoverEvent(event) || super.onHoverEvent(event)

    private fun getOverlayText(
        detectionResult: DetectionResult,
        resId: Int = R.string.detection_result_bounding_box_data,
    ): String =
        detectionResult.accuracy?.let { accuracy ->
            val percentage = String.format(Locale.getDefault(), "%.1f%%", accuracy * 100)
            context.getString(resId, detectionResult.text, percentage)
        } ?: ""
}
