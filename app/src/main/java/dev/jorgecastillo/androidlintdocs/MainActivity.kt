package dev.jorgecastillo.androidlintdocs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val colors = resources.getColorStateList(R.color.colorAccent)
        val input = TextInputLayout(this)
        input.error = ""
    }
}
