package com.android.sample.gethandsdirty.timeconsuming

import android.content.Context
import android.content.Intent
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
    }

    private fun initCostDetect() {
        HandlerMessageConsumingDetect.init {
            findViewById<TextView>(R.id.tv_info).text = "检测到了Handler Message执行耗时,耗时${it}ms"
        }

        IdleHandlerTracker.init {
            Log.d("TimeConsumingDetectActivity", "检测到了耗时IdleHandler耗时.")
            findViewById<TextView>(R.id.tv_info).text = "检测到了耗时IdleHandler,耗时${it}ms"
        }
    }

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, TimeConsumingDetectActivity::class.java)
            context.startActivity(starter)
        }
    }
}