package pl.pb.optigai.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import pl.pb.optigai.R

class SettingsActivity : AppCompatActivity() {

    @SuppressLint("UseKtx")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val radioGroup = findViewById<RadioGroup>(R.id.viewRadioGroup)
        val backButton: ImageView = findViewById(R.id.backButton)
        val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Read the saved preference and set the correct radio button
        val isGridView = sharedPref.getBoolean("isGridView", true)
        if (isGridView) {
            radioGroup.check(R.id.gridRadioButton)
        } else {
            radioGroup.check(R.id.listRadioButton)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            with(sharedPref.edit()) {
                putBoolean("isGridView", checkedId == R.id.gridRadioButton)
                apply()
            }
        }

        // Set the back button click listener
        backButton.setOnClickListener {
            finish()
        }
    }
}