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

/**
 * DetectionOverlay
 *
 * Custom [View] that displays detection bounding boxes and overlay text on top of an [ImageView].
 * Primarily used to visualize results from object detection or text/Braille recognition.
 *
 * Features:
 * - Draws colored rectangles around detected objects.
 * - Displays overlay text above each bounding box.
 * - Supports dynamic mapping from image coordinates to view coordinates.
 * - Provides accessibility support with [ExploreByTouchHelper] for TalkBack users:
 *   - Virtual views for each detection.
 *   - Clickable bounding boxes with haptic feedback.
 *
 * Usage:
 * 1. Call [setDetections] with detection results, image dimensions, and the associated ImageView.
 * 2. Optionally, call [setAvailableColors] to specify a palette for bounding boxes.
 * 3. Add the view as an overlay on top of an [ImageView] displaying the analyzed image.
 *
 * Collaborates with:
 * - [DetectionResult] – represents detected objects with optional bounding boxes and accuracy.
 * - [BitmapCache] – typically provides the image being analyzed.
 *
 * Accessibility:
 * - Each bounding box is treated as a virtual view for screen readers.
 * - Provides description, bounds, and click action for TalkBack.
 */
class DetectionOverlay(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    /** List of pairs of detection results and assigned colors for drawing. */
    private val detectionResultsAndColors = mutableListOf<Pair<DetectionResult, Int>>()

    /** Width of the original image to map coordinates. */
    private var imageWidth = 0

    /** Height of the original image to map coordinates. */
    private var imageHeight = 0

    /** The ImageView on which the overlay is applied. */
    private var imageView: ImageView? = null

    /** List of available colors for bounding boxes. */
    private var colorsAvailable: List<Int?> = emptyList()

    /** Paint used for drawing bounding boxes. */
    private val boxPaint =
        Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

    /** Paint used for drawing overlay text. */
    private val textPaint =
        Paint().apply {
            color = Color.WHITE
            textSize = 42f
            typeface = Typeface.DEFAULT_BOLD
        }

    /**
     * Sets detection results to be displayed and associates random colors from [colorsAvailable].
     *
     * @param detections List of [DetectionResult] objects.
     * @param imageWidth Width of the source image.
     * @param imageHeight Height of the source image.
     * @param imageView The ImageView over which the overlay is drawn.
     */
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

    /**
     * Maps a bounding box from image coordinates to view coordinates, considering scaling and padding.
     *
     * @param rect Bounding box in image coordinates.
     * @return Mapped bounding box in view coordinates.
     */
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

    /** Draws bounding boxes and overlay text for each detection result. */

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

    /**
     * Sets the list of available colors for bounding boxes.
     *
     * @param colorsAvailable List of color integers.
     */
    fun setAvailableColors(colorsAvailable: List<Int?>) {
        this.colorsAvailable = colorsAvailable
    }

    /** Accessibility helper to support TalkBack for each detection bounding box. */
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

    /**
     * Returns formatted text for overlay based on detection result.
     * Includes accuracy as a percentage if available.
     *
     * @param detectionResult Detection result to generate text for.
     * @param resId Optional string resource for formatting text.
     * @return Overlay text with accuracy if available.
     */
    private fun getOverlayText(
        detectionResult: DetectionResult,
        resId: Int = R.string.detection_result_bounding_box_data,
    ): String =
        detectionResult.accuracy?.let { accuracy ->
            val percentage = String.format(Locale.getDefault(), "%.1f%%", accuracy * 100)
            context.getString(resId, detectionResult.text, percentage)
        } ?: ""
}
