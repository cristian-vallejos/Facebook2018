package cl.prevupp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.design.widget.BottomNavigationView
import kotlinx.android.synthetic.main.activity_configuracion.*

/**
 * Clase para la configuracion de los distintos datos
**/

class Configuracion : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                setTitle("Home")

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                setTitle("Settings")
                //setContentView(R.layout.activity_main2)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)
        setTitle("Settings")
        navigation.getMenu().getItem(1).setChecked(true);
        //navigation.getMenu().removeItem(navigation.getMenu().getItem(1).itemId )
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

    }
}