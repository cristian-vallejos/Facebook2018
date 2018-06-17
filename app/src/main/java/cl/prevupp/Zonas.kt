package cl.prevupp

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.RingtoneManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.annotation.ColorInt
import android.support.v4.app.NotificationCompat
import android.support.v4.widget.ImageViewCompat
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import java.io.*
import java.util.*

/**
 * Clase para la muestra de la pantalla principal
 *
*/
class Zonas : AppCompatActivity() {

    internal var circles = ArrayList<ImageView>()
    internal var last_sensors = ArrayList(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0))
    internal var notifDisplayed = ArrayList(Arrays.asList(false, false, false, false, false, false, false, false))

    lateinit var valores: Handler
    internal val RECIEVE_MESSAGE = 1      // Status  for Handler
    private var btAdapter: BluetoothAdapter? = null
    private var btSocket: BluetoothSocket? = null
    private val sb = StringBuilder()
    private val BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var address: String? = null
    private var conexion: ConnectedThread? = null
    private val TAG = "Principal"
    var change: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.zonas)
        setContentView(R.layout.activity_zonas)
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        checkBT()

        //Identificacion de Circunferencias
        circles.add(findViewById(R.id.sensor1) as ImageView)
        circles.add(findViewById(R.id.sensor3) as ImageView)
        circles.add(findViewById(R.id.sensor5) as ImageView)
        circles.add(findViewById(R.id.sensor6) as ImageView)
        circles.add(findViewById(R.id.sensor7) as ImageView)
        circles.add(findViewById(R.id.sensor4) as ImageView)
        circles.add(findViewById(R.id.sensor2) as ImageView)
        circles.add(findViewById(R.id.sensor0) as ImageView)

        valores = object : Handler() {
            override fun handleMessage(msg: android.os.Message) {
                when (msg.what) {
                    RECIEVE_MESSAGE                                                   // if receive massage
                    -> {
                        val readBuf = msg.obj as ByteArray
                        val readMessage = String(readBuf, 0, msg.arg1)              // create string from bytes array
                        sb.append(readMessage)                                             // append string
                        val endOfLineIndex = sb.indexOf("\n")                              // determine the end-of-line
                        if (endOfLineIndex > 0) {                                           // if end-of-line,
                            val sbprint = sb.substring(0, endOfLineIndex - 1)             // extract string

                            sb.delete(0, sb.length)
                            val sensors = sbprint.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                            if (sensors.size == circles.size) {
                                /************************************************************************
                                 * Dibujo de círculos & Envio de notificacion en caso de Estado Crítico *
                                 */

                                for (i in sensors.indices) {
                                    if (isInteger(sensors[i])) {
                                        if (Integer.parseInt(sensors[i]) > last_sensors.get(i) + last_sensors.get(i) * 0.05 || Integer.parseInt(sensors[i]) < last_sensors.get(i) - last_sensors.get(i) * 0.05) {
                                            change = true
                                            break
                                        } else {
                                            change = false
                                        }
                                    }
                                }

                                    for (i in sensors.indices) {
                                        if (isInteger(sensors[i])) {
                                            last_sensors[i] = Integer.parseInt(sensors[i])
                                            val value = Integer.parseInt(sensors[i])
                                            val px130 = 130 * applicationContext.getResources().getDisplayMetrics().density

                                            if (value < 10)
                                                circles[i].visibility = View.INVISIBLE
                                            else
                                                circles[i].visibility = View.VISIBLE

                                            circles[i].getLayoutParams().width = value * px130.toInt() / 1000
                                            circles[i].getLayoutParams().height = value * px130.toInt() / 1000
                                            circles[i].requestLayout()
                                            circles[i].invalidate()

                                            if (!change) {
                                                if ((Integer.parseInt(sensors[i]) >= 0 && Integer.parseInt(sensors[i]) < 450)) {
                                                    ImageViewCompat.setImageTintList(circles[i], ColorStateList.valueOf(Color.parseColor("#8ee776")))
                                                } else if ((Integer.parseInt(sensors[i]) >= 450 && Integer.parseInt(sensors[i]) < 550)) {
                                                    ImageViewCompat.setImageTintList(circles[i], ColorStateList.valueOf(Color.parseColor("#ebe57d")))
                                                } else if ((Integer.parseInt(sensors[i]) >= 550 && Integer.parseInt(sensors[i]) < 700)) {
                                                    ImageViewCompat.setImageTintList(circles[i], ColorStateList.valueOf(Color.parseColor("#e99a5a")))

                                                    displayNotificationOfState(i, "Critical")
                                                } else if (Integer.parseInt(sensors[i]) >= 700) {
                                                    ImageViewCompat.setImageTintList(circles[i], ColorStateList.valueOf(Color.parseColor("#eb7d7d")))

                                                    displayNotificationOfState(i, "Extreme")
                                                }
                                            } else {
                                                if ((Integer.parseInt(sensors[i]) >= 0 && Integer.parseInt(sensors[i]) < 450)) {
                                                    ImageViewCompat.setImageTintList(circles[i], ColorStateList.valueOf(Color.parseColor("#8ee776")))
                                                } else if ((Integer.parseInt(sensors[i]) >= 450 && Integer.parseInt(sensors[i]) < 550)) {
                                                    ImageViewCompat.setImageTintList(circles[i], ColorStateList.valueOf(Color.parseColor("#ebe57d")))
                                                } else if ((Integer.parseInt(sensors[i]) >= 550 && Integer.parseInt(sensors[i]) < 700)) {
                                                    ImageViewCompat.setImageTintList(circles[i], ColorStateList.valueOf(Color.parseColor("#e99a5a")))

                                                    displayNotificationOfState(i, "Critical")
                                                } else if (Integer.parseInt(sensors[i]) >= 700) {
                                                    ImageViewCompat.setImageTintList(circles[i], ColorStateList.valueOf(Color.parseColor("#eb7d7d")))

                                                    displayNotificationOfState(i, "Extreme")
                                                }
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkBT() {
        if (btAdapter == null) {
            Toast.makeText(baseContext, "Dispositivo no soporta bluetooth", Toast.LENGTH_SHORT).show()
        } else {
            if (btAdapter!!.isEnabled) {
            } else {
                Toast.makeText(baseContext, "Encender bluetooth", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*private fun buttonPantallaConfiguracion() {
        val botonC = findViewById(R.id.botonConfiguracion) as ImageButton
        botonC.setOnClickListener {
            try {
                btSocket!!.close()
            } catch (e2: IOException) {
            }

            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }*/


    @Throws(IOException::class)
    private fun crearBluetoothSocket(device: BluetoothDevice): BluetoothSocket {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID)
    }

    public override fun onResume() {
        super.onResume()

        val intent = intent
        address = intent.getStringExtra("EXTRA_DEVICE_ADDRESS")
        val device = btAdapter!!.getRemoteDevice(address)

        try {
            btSocket = crearBluetoothSocket(device)
        } catch (e: IOException) {
            Toast.makeText(baseContext, "Fallida conexion", Toast.LENGTH_SHORT).show()
        }

        try {
            btSocket!!.connect()
        } catch (e: IOException) {
            try {
                btSocket!!.close()
            } catch (e2: IOException) {

            }

        }

        conexion = ConnectedThread(btSocket!!)
        conexion!!.start()
    }

    public override fun onPause() {
        val intent = intent
        address = intent.getStringExtra("EXTRA_DEVICE_ADDRESS")
        val device = btAdapter!!.getRemoteDevice(address)

        try {
            btSocket = crearBluetoothSocket(device)
        } catch (e: IOException) {
            Toast.makeText(baseContext, "Fallida conexion", Toast.LENGTH_SHORT).show()
        }

        try {
            btSocket!!.connect()
        } catch (e: IOException) {
            try {
                btSocket!!.close()
            } catch (e2: IOException) {

            }

        }

        conexion = ConnectedThread(btSocket!!)
        conexion!!.start()

        super.onPause()
    }


    private inner class ConnectedThread(socket: BluetoothSocket) : Thread() {
        private val mInStream: InputStream?

        init {
            var tmpIn: InputStream? = null

            try {
                tmpIn = socket.inputStream
            } catch (e: IOException) {
            }

            mInStream = tmpIn
        }

        override fun run() {
            val buffer = ByteArray(2048)
            var bytes: Int
            while (true) {
                try {
                    bytes = mInStream!!.read(buffer)
                    valores.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget()
                } catch (e: IOException) {
                    break
                }

            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            true
        } else false
    }

    protected fun displayNotificationOfState(zone: Int, state: String) {
        val i = Intent(this, CloseItNow::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, i, 0)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val noti: Notification

        val ticker = "Conflict on Pressure Zone #" + (zone + 1).toString()
        val contentTitle = "Pressure Zone #" + (zone + 1).toString() + " on " + state + " state"
        var contentText: CharSequence = ""
        if (state == "Critical") {
            contentText = "Please, change your posture"
        } else if (state == "Extreme") {
            contentText = "You must change your posture inmmediately"
        }
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        noti = NotificationCompat.Builder(this)
                    .setContentIntent(pendingIntent)
                    .setTicker(ticker)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setSmallIcon(R.drawable.icono_48)
                    .addAction(R.drawable.icono_48, ticker, pendingIntent)
                    .setVibrate(longArrayOf(100, 250, 100, 500))
                    .setSound(alarmSound)
                    .build()
        nm.notify(zone, noti)
    }

    fun isInteger(s: String): Boolean {
        var isValidInteger = false
        try {
            Integer.parseInt(s)
            // s is a valid integer
            isValidInteger = true
        } catch (ex: NumberFormatException) {
            // s is not an integer
        }

        return isValidInteger
    }
}