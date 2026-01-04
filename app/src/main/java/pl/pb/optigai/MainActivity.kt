/**
 * Main launcher activity for the app.
 *
 * Displays an animated eye vector drawable for a brief splash effect,
 * then automatically transitions to the [CameraActivity].
 *
 * The animation duration is controlled by [ANIMATION_DURATION].
 */
package pl.pb.optigai

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import pl.pb.optigai.ui.CameraActivity

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    /** Duration (in milliseconds) to show the splash animation before transitioning. */
    private val ANIMATION_DURATION = 800L
    /**
     * Called when the activity is first created.
     *
     * Sets the content view to [R.layout.activity_main], starts the eye animation,
     * and schedules a transition to [CameraActivity] after [ANIMATION_DURATION].
     *
     * @param savedInstanceState Bundle containing saved state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // <- konieczne!

        val eyeImage: ImageView = findViewById(R.id.eyeImageView)
        eyeImage.setImageResource(R.drawable.eye_animation)
        val drawable = eyeImage.drawable
        if (drawable is AnimatedVectorDrawable) {
            drawable.start()
        }

        eyeImage.postDelayed({
            openCameraActivityAndFinish()
        }, ANIMATION_DURATION)
    }
    /**
     * Opens the [CameraActivity] and finishes the current [MainActivity].
     *
     * Uses a fade-in/fade-out transition for smooth animation.
     */
    private fun openCameraActivityAndFinish() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
