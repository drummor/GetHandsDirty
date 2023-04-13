package com.android.sample.gethandsdirty.timeconsuming

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_NORMAL
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.android.sample.gethandsdirty.R
import com.android.sample.gethandsdirty.frames.hookchoreographer.LooperMonitor
import com.android.sample.gethandsdirty.timeconsuming.handlermessage.HandlerMessageConsumingDetect
import com.android.sample.gethandsdirty.timeconsuming.idlehandler.IdleHandlerTracker
import com.android.sample.gethandsdirty.timeconsuming.others.TouchCostWindowCallback

class TimeConsumingDetectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_consuming_detect)
        title = "耗时检测"
        initCostDetect()
        findViewById<Button>(R.id.bt_detect_idle_handler).setOnClickListener {
            Looper.getMainLooper().queue.addIdleHandler {
                Thread.sleep(5_000)
                return@addIdleHandler true
            }
        }

        findViewById<Button>(R.id.bt_detect_handler_message).setOnClickListener {
            window.decorView.postDelayed({ Thread.sleep(3000) }, 100)

        }

        this.findViewById<Button>(R.id.bt_touch_event).setOnTouchListener { _, _ ->
            try {
                throw java.lang.NullPointerException()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("drummor", "" + e.toString())
            }

            Thread.sleep(2000)
            return@setOnTouchListener false
        }

        initSensor()
    }


    private fun initCostDetect() {
        HandlerMessageConsumingDetect.init {
            findViewById<TextView>(R.id.tv_info).text = "检测到了Handler Message执行耗时,耗时${it}ms"
        }

        IdleHandlerTracker.init {
            Log.d("TimeConsumingDetectActivity", "检测到了耗时IdleHandler耗时.")
            findViewById<TextView>(R.id.tv_info).text = "检测到了耗时IdleHandler,耗时${it}ms"
        }
        if (window.callback != null) {
            window.callback = TouchCostWindowCallback(window.callback)
        }
    }


    lateinit var sensorManager: SensorManager
    lateinit var sensor: Sensor
    private fun initSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(object : SensorEventListener2 {
            override fun onSensorChanged(event: SensorEvent?) {
                try {
                    throw java.lang.NullPointerException()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("drummor", "" + e.toString())
                }
                Thread.sleep(10_000)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }

            override fun onFlushCompleted(sensor: Sensor?) {

            }
        }, sensor, SENSOR_DELAY_NORMAL)

    }

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, TimeConsumingDetectActivity::class.java)
            context.startActivity(starter)
        }
    }
}