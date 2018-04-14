package cl.prevupp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.app_name)
        setContentView(R.layout.activity_start)
        buttonPantallaPrincipal()
    }

    private fun buttonPantallaPrincipal() {

    }
}
