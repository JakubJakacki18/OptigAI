package pl.pb.optigai.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import pl.pb.optigai.utils.AppLogger
import pl.pb.optigai.utils.data.DetectionResult

class DetectionOverlay(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val boxes = mutableListOf<DetectionResult>()
    private var imageWidth = 0
    private var imageHeight = 0
    private var imageView: ImageView? = null

    private val boxPaint =
        Paint().apply {
            color = Color.RED
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
        boxes.clear()
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        this.imageView = imageView
        boxes.addAll(detections)
        invalidate()
    }

    private fun mapRectToView(rect: RectF): RectF {
        val scaleX = width.toFloat() / imageWidth.toFloat()
        val scaleY = height.toFloat() / imageHeight.toFloat()

        return RectF(
            rect.left * scaleX,
            rect.top * scaleY,
            rect.right * scaleX,
            rect.bottom * scaleY,
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        AppLogger.i("Image size: ${imageWidth}x$imageHeight")
        AppLogger.i("View size: ${width}x$height")
        AppLogger.i("ImageView size: ${imageView?.width}x${imageView?.height}")
        for (d in boxes) {
            if (d.boundingBox == null) continue
            AppLogger.i(d.boundingBox.toString())
            val rect =
                mapRectToView(
                    d.boundingBox,
                )
            AppLogger.i(rect.toString())
            canvas.drawRect(d.boundingBox, boxPaint)
            canvas.drawText("${d.text} ${(d.accuracy * 100).toInt()}%", rect.left, rect.top - 10, textPaint)
        }
    }
}
