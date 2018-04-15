package cl.prevupp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.view.animation.Animation
import android.view.animation.AlphaAnimation

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.app_name)
        setContentView(R.layout.activity_start)
        loadingConfiguration()
    }

    private fun loadingConfiguration() {
        val prevUPP = findViewById(R.id.prevUPP) as ImageView

        val startingApp = findViewById(R.id.startingApp) as TextView
        val dot3 = findViewById(R.id.dot3) as TextView
        val dot2 = findViewById(R.id.dot2) as TextView
        val dot1 = findViewById(R.id.dot1) as TextView

        //fadein
        val myFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein)
        prevUPP.startAnimation(myFadeInAnimation) //Set animation to your ImageView
        startingApp.startAnimation(myFadeInAnimation) //Set animation to your TextView
        dot3.startAnimation(myFadeInAnimation) //Set animation to your TextView
        dot2.startAnimation(myFadeInAnimation) //Set animation to your TextView
        dot1.startAnimation(myFadeInAnimation) //Set animation to your TextView

        val anim3 = AlphaAnimation(0.0f, 1.0f)
        anim3.duration = 200 //You can manage the time of the blink with this parameter
        anim3.startOffset = 200
        anim3.repeatMode = Animation.REVERSE
        anim3.repeatCount = Animation.INFINITE
        dot3.startAnimation(anim3)


        val anim2 = AlphaAnimation(0.0f, 1.0f)
        anim2.duration = 200 //You can manage the time of the blink with this parameter
        anim2.startOffset = 400
        anim2.repeatMode = Animation.REVERSE
        anim2.repeatCount = Animation.INFINITE
        dot2.startAnimation(anim2)


        val anim1 = AlphaAnimation(0.0f, 1.0f)
        anim1.duration = 20 //You can manage the time of the blink with this parameter
        anim1.startOffset = 400
        anim1.repeatMode = Animation.REVERSE
        anim1.repeatCount = Animation.INFINITE
        dot1.startAnimation(anim1)

        val mHandler = Handler()
        mHandler.postDelayed(Runnable {
            val intent = Intent(this, Configuracion::class.java)

            startActivityForResult(intent, 0)
        }, 5000L)
    }
}
