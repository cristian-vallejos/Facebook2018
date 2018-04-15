package cl.prevupp

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.View
import android.widget.*
import java.util.ArrayList

class Bluetooth : AppCompatActivity() {

    // Declaracion de variables
    lateinit var  bluet: Switch
    private var bluetAdapter: BluetoothAdapter? = null
    lateinit var bluetIntent: Intent
    var REQUEST_BLUETOOTH = 1
    lateinit var listBluet: ListView
    private val bluetArray = ArrayList<Spanned>()
    lateinit var texto2: TextView
    private val TAG = "Settings"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.conf)
        setContentView(R.layout.activity_bluetooth)
    }

    public override fun onResume() {
        super.onResume()

        checkBT()
        // Switch
        bluet = findViewById<View>(R.id.switch1) as Switch
        bluet.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                BluetEnable()
            } else {
                BluetDisable()
            }
        }
        texto2 = findViewById<View>(R.id.textView2) as TextView

        // Bluetooth
        bluetAdapter = BluetoothAdapter.getDefaultAdapter()

        // Conexiones bluetooth
        val dispositivosPareados = bluetAdapter!!.bondedDevices

        // Agregar dispositivos pareados
        listBluet = findViewById<View>(R.id.pareados) as ListView
        if (dispositivosPareados.size > 0) {
            for (device in dispositivosPareados) {
                bluetArray.add(Html.fromHtml("<b>" + device.name + "<b><br>" + device.address))
            }
            listBluet.visibility = View.VISIBLE
            texto2.visibility = View.GONE
        } else {
            listBluet.visibility = View.GONE
            texto2.text = resources.getText(R.string.none_paired)
            texto2.visibility = View.VISIBLE
        }

        // Trabajar con la lista
        listBluet.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, bluetArray)
        listBluet.onItemClickListener = deviceClick
    }

    private fun checkBT() {
        bluet = findViewById<View>(R.id.switch1) as Switch
        bluetAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetAdapter == null) {
            Toast.makeText(baseContext, "Dispositivo no soporta bluetooth", Toast.LENGTH_SHORT).show()
        } else {
            if (bluetAdapter!!.isEnabled) {
                bluet.isChecked = true
                Log.d(TAG, "Bluetooth turned on")
            } else {
                bluet.isChecked = false
                Toast.makeText(baseContext, "Please, turn on your Bluetooth", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Activa el Bluetooth
    private fun BluetEnable() {
        bluetIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(bluetIntent, REQUEST_BLUETOOTH)
    }

    // Desactiva el Bluetooth
    private fun BluetDisable() {
        bluetAdapter!!.disable()
        bluetArray.clear()
        listBluet.adapter = null
    }

    private val deviceClick = AdapterView.OnItemClickListener { parent, v, position, id ->
        texto2.text = "\nEstablishing connection..."
        texto2.visibility = View.VISIBLE
        val info = (v as TextView).text.toString()
        val address = info.substring(info.length - 17)

        bluetIntent = Intent(this, Zonas::class.java)
        bluetIntent.putExtra("EXTRA_DEVICE_ADDRESS", address)
        startActivityForResult(bluetIntent, 0)
    }
}
