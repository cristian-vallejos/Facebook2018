package cl.prevupp

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.RingtoneManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import java.io.*
import java.util.*

/**
 * Clase para la muestra de la pantalla principal
 *
**

class Zonas : AppCompatActivity() {

    internal var boxes = ArrayList<RelativeLayout>()
    internal var circles = ArrayList<ImageView>()
    internal var last_sensors = ArrayList(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0))

    internal var valores: Handler
    internal val RECIEVE_MESSAGE = 1      // Status  for Handler
    private var btAdapter: BluetoothAdapter? = null
    private var btSocket: BluetoothSocket? = null
    private val sb = StringBuilder()
    private var conexion: ConnectedThread? = null
    private var config: Configuration? = null

    //Timers para tiempo de vida & notificaciones de presión prolongada
    private var timer: Int = 0
    private var change: Boolean = false
    private val prolongedCriticPressureTime = ArrayList(Collections.nCopies(8, 0))   //in seconds
    private val prolongedExtremPressureTime = ArrayList(Collections.nCopies(8, 0))   //in seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.zonas)
        config = resources.configuration
        if (config!!.smallestScreenWidthDp >= 321)
            setContentView(R.layout.principal_redmi)
        else
            setContentView(R.layout.principal_alcatel)
        buttonPantallaConfiguracion()
        timer = 0
        change = false
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        checkBT()

        //Identificacion de Layouts
        boxes.add(findViewById(R.id.box1_3) as RelativeLayout)
        boxes.add(findViewById(R.id.box1_4) as RelativeLayout)
        boxes.add(findViewById(R.id.box2_3) as RelativeLayout)
        boxes.add(findViewById(R.id.box2_2) as RelativeLayout)
        boxes.add(findViewById(R.id.box2_4) as RelativeLayout)
        boxes.add(findViewById(R.id.box2_1) as RelativeLayout)
        boxes.add(findViewById(R.id.box1_2) as RelativeLayout)
        boxes.add(findViewById(R.id.box1_1) as RelativeLayout)

        //Identificacion de Circunferencias
        circles.add(findViewById(R.id.c1_3) as ImageView)
        circles.add(findViewById(R.id.c1_4) as ImageView)
        circles.add(findViewById(R.id.c2_3) as ImageView)
        circles.add(findViewById(R.id.c2_2) as ImageView)
        circles.add(findViewById(R.id.c2_4) as ImageView)
        circles.add(findViewById(R.id.c2_1) as ImageView)
        circles.add(findViewById(R.id.c1_2) as ImageView)
        circles.add(findViewById(R.id.c1_1) as ImageView)

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
                                /******************************************
                                 * Guardado de información en archivo .txt *
                                 */

                                val calendar = Calendar.getInstance()

                                val day = calendar.get(Calendar.DAY_OF_MONTH)
                                val month = calendar.get(Calendar.MONTH) + 1
                                //  Meses se indexan desde el 0. Ej: Enero [0], ..., Diciembre [11]
                                val year = calendar.get(Calendar.YEAR)
                                val FILE = day.toString() + "-" + month.toString() + "-" + year.toString() + ".txt"

                                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                                val minutes = calendar.get(Calendar.MINUTE)
                                val seconds = calendar.get(Calendar.SECOND)
                                val LINE = hour.toString() + ":" + minutes.toString() + ":" + seconds.toString() + "#"

                                try {
                                    if (timer % 120 == 0) {
                                        // Creates a file in the primary external storage space of the
                                        // current application.
                                        // If the file does not exists, it is created.
                                        val file = File(applicationContext.getExternalFilesDir(null), FILE)
                                        if (!file.exists())
                                            file.createNewFile()

                                        // Adds a line to the file
                                        val writer = BufferedWriter(FileWriter(file, true /*append*/))
                                        writer.write(LINE + sbprint + "\n")
                                        writer.close()
                                    }
                                } catch (e: IOException) {
                                    Log.e("ReadWriteFile", "Unable to write to $FILE file!")
                                }

                                /************************************************************************
                                 * Dibujo de círculos & Envio de notificacion en caso de Estado Crítico *
                                 */

                                for (i in sensors.indices) {
                                    if (isInteger(sensors[i])) {
                                        if (Integer.parseInt(sensors[i]) > last_sensors[i] + last_sensors[i] * 0.1 || Integer.parseInt(sensors[i]) < last_sensors[i] - last_sensors[i] * 0.1) {
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

                                        if (!change) {
                                            if (/*(Integer.parseInt(sensors[i]) >= 0 && Integer.parseInt(sensors[i]) < 500) || */timer <= 300) {
                                                val circle_fine_state: GradientDrawable
                                                if (config!!.smallestScreenWidthDp >= 321)
                                                    circle_fine_state = drawCircle(value / 5, 0) //20
                                                else
                                                    circle_fine_state = drawCircle(value / 20, 0)
                                                circles[i].background = circle_fine_state

                                                //  Reset of High Pressure timer for the 'i' sensor
                                                prolongedCriticPressureTime[i] = 0
                                                prolongedExtremPressureTime[i] = 0
                                            } else if (/*(Integer.parseInt(sensors[i]) >= 500 && Integer.parseInt(sensors[i]) < 600) || */timer > 300 && timer <= 600) {
                                                val circle_regular_state: GradientDrawable
                                                if (config!!.smallestScreenWidthDp >= 321)
                                                    circle_regular_state = drawCircle(value / 4, 0) //16
                                                else
                                                    circle_regular_state = drawCircle(value / 16, 0)
                                                circles[i].background = circle_regular_state

                                                //  Reset of High Pressure timer for the 'i' sensor
                                                prolongedCriticPressureTime[i] = 0
                                                prolongedExtremPressureTime[i] = 0
                                            } else if (/*(Integer.parseInt(sensors[i]) >= 650 && Integer.parseInt(sensors[i]) < 750) || */timer > 600 && timer <= 900) {
                                                //  Adding to High Pressure timer for the 'i' sensor
                                                prolongedCriticPressureTime[i] = prolongedCriticPressureTime[i] + 1
                                                prolongedExtremPressureTime[i] = 0
                                                if (prolongedCriticPressureTime[i] == 1) {
                                                    displayNotificationOfState(i, "Crítico")
                                                }

                                                val circle_critical_state: GradientDrawable
                                                if (config!!.smallestScreenWidthDp >= 321)
                                                    circle_critical_state = drawCircle(value / 3, 2) //12
                                                else
                                                    circle_critical_state = drawCircle(value / 12, 0)
                                                circles[i].background = circle_critical_state
                                            } else if (/*Integer.parseInt(sensors[i]) >= 750 || */timer > 900) {
                                                //  Adding to High Pressure timer for the 'i' sensor
                                                prolongedExtremPressureTime[i] = prolongedExtremPressureTime[i] + 1
                                                prolongedCriticPressureTime[i] = 0
                                                if (prolongedExtremPressureTime[i] == 1) {
                                                    displayNotificationOfState(i, "Extremo")
                                                }

                                                val circle_extreme_state: GradientDrawable
                                                if (config!!.smallestScreenWidthDp >= 321) {
                                                    if (value >= 480) {
                                                        circle_extreme_state = drawCircle(240, 3)
                                                    } else {
                                                        circle_extreme_state = drawCircle(value / 2, 3)  //8
                                                    }
                                                } else {
                                                    if (value >= 480) {
                                                        circle_extreme_state = drawCircle(60, 3)
                                                    } else {
                                                        circle_extreme_state = drawCircle(value / 8, 3)
                                                    }
                                                }
                                                circles[i].background = circle_extreme_state
                                            }
                                        } else {
                                            timer = 0

                                            val circle_fine_state: GradientDrawable
                                            if (config!!.smallestScreenWidthDp >= 321)
                                                circle_fine_state = drawCircle(value / 5, 0) //20
                                            else
                                                circle_fine_state = drawCircle(value / 20, 0)
                                            circles[i].background = circle_fine_state

                                            //  Reset of High Pressure timer for the 'i' sensor
                                            prolongedCriticPressureTime[i] = 0
                                            prolongedExtremPressureTime[i] = 0
                                        }
                                    }
                                }
                                timer = timer + 1

                                /*for (int i = 0; i < sensors.length; i++) {
                                    if (isInteger(sensors[i])) {
                                        if (Integer.parseInt(sensors[i]) >= 0 && Integer.parseInt(sensors[i]) < 100) {
                                            GradientDrawable circle_fine_state;
                                            if (config.smallestScreenWidthDp >= 321) {
                                                circle_fine_state = drawCircle(Integer.parseInt(sensors[i]), 0);
                                            }
                                            else {
                                                circle_fine_state = drawCircle(Integer.parseInt(sensors[i])/4, 0);
                                            }
                                            circles.get(i).setBackground(circle_fine_state);

                                            //  Reset of High Pressure timer for the 'i' sensor
                                            prolongedCriticPressureTime.set(i, 0);
                                            prolongedExtremPressureTime.set(i, 0);
                                        } else if (Integer.parseInt(sensors[i]) >= 100 && Integer.parseInt(sensors[i]) < 150) {
                                            GradientDrawable circle_regular_state;
                                            if (config.smallestScreenWidthDp >= 321) {
                                                circle_regular_state = drawCircle(Integer.parseInt(sensors[i]), 1);
                                            }
                                            else {
                                                circle_regular_state = drawCircle(Integer.parseInt(sensors[i])/4, 1);
                                            }
                                            circles.get(i).setBackground(circle_regular_state);

                                            //  Reset of High Pressure timer for the 'i' sensor
                                            prolongedCriticPressureTime.set(i, 0);
                                            prolongedExtremPressureTime.set(i, 0);
                                        } else if (Integer.parseInt(sensors[i]) >= 150 && Integer.parseInt(sensors[i]) < 200) {
                                            //  Adding to High Pressure timer for the 'i' sensor
                                            prolongedCriticPressureTime.set(i, prolongedCriticPressureTime.get(i)+1);
                                            prolongedExtremPressureTime.set(i, 0);
                                            if (prolongedCriticPressureTime.get(i) == 1){
                                                displayNotificationOfState(i, "Crítico");
                                            }
                                            else if (prolongedCriticPressureTime.get(i)%600 == 0){
                                                displayNotificationOfProlongedHighPressure(i, "Crítico");
                                            }

                                            GradientDrawable circle_critical_state;
                                            if (config.smallestScreenWidthDp >= 321) {
                                                circle_critical_state = drawCircle(Integer.parseInt(sensors[i]), 2);
                                            }
                                            else {
                                                circle_critical_state = drawCircle(Integer.parseInt(sensors[i])/4, 2);
                                            }
                                            circles.get(i).setBackground(circle_critical_state);
                                        }
                                        else if (Integer.parseInt(sensors[i]) >= 200) {
                                            //  Adding to High Pressure timer for the 'i' sensor
                                            prolongedExtremPressureTime.set(i, prolongedExtremPressureTime.get(i)+1);
                                            prolongedCriticPressureTime.set(i, 0);
                                            if (prolongedExtremPressureTime.get(i) == 1){
                                                displayNotificationOfState(i, "Extremo");
                                            }
                                            else if (prolongedExtremPressureTime.get(i)%1800 == 0){
                                                displayNotificationOfProlongedHighPressure(i, "Extremo");
                                            }

                                            GradientDrawable circle_extreme_state;
                                            if (config.smallestScreenWidthDp >= 321) {
                                                if (Integer.parseInt(sensors[i]) < 240)
                                                    circle_extreme_state = drawCircle(Integer.parseInt(sensors[i]), 3);
                                                else
                                                    circle_extreme_state = drawCircle(240, 3);
                                            }
                                            else {
                                                if (Integer.parseInt(sensors[i]) < 240)
                                                    circle_extreme_state = drawCircle(Integer.parseInt(sensors[i])/4, 3);
                                                else
                                                    circle_extreme_state = drawCircle(60, 3);
                                            }
                                            circles.get(i).setBackground(circle_extreme_state);
                                        }
                                    }
                                }*/
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

    private fun buttonPantallaConfiguracion() {
        val botonC = findViewById(R.id.botonConfiguracion) as ImageButton
        botonC.setOnClickListener {
            try {
                btSocket!!.close()
            } catch (e2: IOException) {
            }

            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }


    @Throws(IOException::class)
    private fun crearBluetoothSocket(device: BluetoothDevice): BluetoothSocket {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID)
    }

    public override fun onResume() {
        super.onResume()

        val intent = intent
        address = intent.getStringExtra(Configuracion.EXTRA_DEVICE_ADDRESS)
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
        address = intent.getStringExtra(Configuracion.EXTRA_DEVICE_ADDRESS)
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

        val ticker = "Conflicto en Zona de Presión " + (zone + 1).toString()
        val contentTitle = "Zona de Presión " + (zone + 1).toString() + " en Estado " + state
        var contentText: CharSequence = ""
        if (state == "Crítico") {
            contentText = "Por favor, cambia tu postura"
        } else if (state == "Extremo") {
            contentText = "Debes cambiar tu postura lo antes posible"
        }
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (config!!.smallestScreenWidthDp >= 321) {
            noti = NotificationCompat.Builder(this)
                    .setContentIntent(pendingIntent)
                    .setTicker(ticker)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setSmallIcon(R.drawable.icono_192)
                    .addAction(R.drawable.icono_192, ticker, pendingIntent)
                    .setVibrate(longArrayOf(100, 250, 100, 500))
                    .setSound(alarmSound)
                    .build()
        } else {
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
        }
        nm.notify(zone, noti)
    }

    protected fun displayNotificationOfProlongedHighPressure(zone: Int, state: String) {
        val i = Intent(this, CloseItNow::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, i, 0)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val noti: Notification

        val ticker = "Conflicto en Zona de Presión " + (zone + 1).toString()
        val contentTitle = "La Zona de Presión " + (zone + 1).toString() + " lleva"
        val contentText = "demasiado tiempo en Estado $state"
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (config!!.smallestScreenWidthDp >= 321) {
            noti = NotificationCompat.Builder(this)
                    .setContentIntent(pendingIntent)
                    .setTicker(ticker)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setSmallIcon(R.drawable.icono_192)
                    .addAction(R.drawable.icono_192, ticker, pendingIntent)
                    .setVibrate(longArrayOf(100, 250, 100, 500))
                    .setSound(alarmSound)
                    .build()
        } else {
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
        }
        nm.notify(zone, noti)
    }

    fun drawCircle(diameter: Int, state: Int): GradientDrawable {
        val gd = GradientDrawable()
        gd.gradientType = GradientDrawable.RADIAL_GRADIENT
        if (state == 0) {
            if (diameter < 5) {
                gd.gradientRadius = 40f
                gd.colors = intArrayOf(Color.parseColor("#80000000"), Color.parseColor("#80000000"), Color.parseColor("#80000000"))
                gd.setStroke(1 / 2, Color.parseColor("#80000000"))
            } else {
                gd.gradientRadius = (diameter * 8 / 10).toFloat()
                gd.colors = intArrayOf(Color.parseColor("#FFFF00"), Color.parseColor("#02ae02"), Color.parseColor("#00ce00"))
                gd.setStroke(1 / 2, Color.parseColor("#039a03"))
            }
            gd.shape = GradientDrawable.OVAL
        } else if (state == 1) {
            gd.colors = intArrayOf(Color.parseColor("#FF0000"), Color.parseColor("#f9d920"), Color.parseColor("#5cd65c"))
            gd.gradientRadius = (diameter * 126 / 100).toFloat()
            gd.shape = GradientDrawable.OVAL
            gd.setStroke(1 / 2, Color.parseColor("#039a03"))
        } else if (state == 2) {
            gd.colors = intArrayOf(Color.parseColor("#FF0000"), Color.parseColor("#ffd633"), Color.parseColor("#ffe066"))
            gd.gradientRadius = (diameter * 14 / 10).toFloat()
            gd.shape = GradientDrawable.OVAL
            gd.setStroke(1 / 2, Color.parseColor("#ffdb4d"))
        } else {
            gd.colors = intArrayOf(Color.parseColor("#FF0000"), Color.parseColor("#ff3300"), Color.parseColor("#ff9933"))
            gd.gradientRadius = (diameter * 88 / 100).toFloat()
            gd.shape = GradientDrawable.OVAL
            gd.setStroke(1 / 2, Color.parseColor("#ffcc66"))
        }
        gd.setSize(diameter * 3 / 2, diameter * 3 / 2)
        return gd
    }

    companion object {
        private val BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private var address: String? = null
        private val TAG = "Zonas"

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
}*/