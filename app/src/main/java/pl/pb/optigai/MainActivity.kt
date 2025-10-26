package pl.pb.optigai

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import pl.pb.optigai.ui.CameraActivity

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private val ANIMATION_DURATION = 800L

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

    private fun openCameraActivityAndFinish() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
