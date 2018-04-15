package cl.prevupp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.design.widget.BottomNavigationView
import android.view.View
import kotlinx.android.synthetic.main.activity_configuracion.*
import android.R.string.cancel
import android.app.AlertDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.*


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
        buttonbluetooth()
        buttongender()
        buttonweight()
    }

    private fun buttonbluetooth() {
        val botonblu = findViewById<View>(R.id.bluetooth) as Button
        botonblu.setOnClickListener {
            val i2 = Intent(applicationContext, Bluetooth::class.java)
            startActivity(i2)
        }
    }

    private fun buttonweight() {
        val botonwei = findViewById<View>(R.id.weight) as Button
        botonwei.setOnClickListener {

                // get prompts.xml view
                val li = LayoutInflater.from(this)
                val promptsView = li.inflate(R.layout.prompts, null)

                val alertDialogBuilder = AlertDialog.Builder(
                        this)

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView)

                val userInput = promptsView
                        .findViewById<View>(R.id.editTextDialogUserInput) as EditText

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                DialogInterface.OnClickListener { dialog, id ->
                                    // get user input and set it to result
                                    // edit text
                                    val peso = findViewById<View>(R.id.weight_var) as TextView
                                    val concatena = (userInput.text.toString())  + " Kg"
                                    peso.setText(concatena)
                                })
                        .setNegativeButton("Cancel",
                                DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })

                // create alert dialog
                val alertDialog = alertDialogBuilder.create()

                // show it
                alertDialog.show()


        }
    }

    private fun buttongender() {
        val clickListener = View.OnClickListener { view ->
            when (view.id) {
                R.id.gender -> {
                    showPopup(view)
                }
            }
        }

        gender.setOnClickListener(clickListener)
    }

    private fun showPopup(view: View) {
        var popup: PopupMenu? = null;
        popup = PopupMenu(this, view)
        popup.inflate(R.menu.header_menu)

        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {

                R.id.header1 -> {
                    val sexo = findViewById<View>(R.id.gender_var) as TextView
                    sexo.setText(item.title.toString())
                }
                R.id.header2 -> {
                    val sexo = findViewById<View>(R.id.gender_var) as TextView
                    sexo.setText(item.title.toString())
                }
            }

            true
        })

        popup.show()
    }
}


