package com.android.sample.gethandsdirty

import android.os.Bundle
import android.os.HandlerThread
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.sample.gethandsdirty.anr.ANRActivity
import com.android.sample.gethandsdirty.choreographer.FrameCallback
import com.android.sample.gethandsdirty.choreographer.MiniChoreographer
import com.android.sample.gethandsdirty.choreographer.MiniChoreographer.Companion.CALLBACK_INPUT
import com.android.sample.gethandsdirty.frames.framemetrics.FrameMetricAvailableActivity
import com.android.sample.gethandsdirty.frames.hookchoreographer.HookChoreographerFrameActivity
import com.android.sample.gethandsdirty.frames.poorframe.PoorFrameActivity
import com.android.sample.gethandsdirty.frames.srollframe.ScrollFrameActivity
import com.android.sample.gethandsdirty.timeconsuming.TimeConsumingDetectActivity

/*
  shell dumpsys gfxinfo com.android.sample.gethandsdirty

 */
class MainActivity : AppCompatActivity() {

    private val handlerThread = HandlerThread("--")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handlerThread.start()

        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.bt_poor_frame).setOnClickListener {
            PoorFrameActivity.start(this)
        }
        findViewById<Button>(R.id.bt_scroll_frame).setOnClickListener {
            ScrollFrameActivity.start(this)
        }

        findViewById<Button>(R.id.bt_hook_choreographer).setOnClickListener {
            HookChoreographerFrameActivity.start(this)
        }

        findViewById<View>(R.id.bt_send_call).setOnClickListener {
            MiniChoreographer.getInstance()
                .postCallback(CALLBACK_INPUT, 100, object : FrameCallback {
                    override fun doFrame(frameTimeNanos: Long) {
                        findViewById<TextView>(R.id.tv_info).text = "消息执行了：$frameTimeNanos"
                    }
                })
        }
        findViewById<View>(R.id.bt_window_metrics).setOnClickListener {
            FrameMetricAvailableActivity.start(this)
        }

        findViewById<Button>(R.id.bt_time).setOnClickListener {
            TimeConsumingDetectActivity.start(this)
        }
        findViewById<Button>(R.id.bt_about_anr).setOnClickListener {
            ANRActivity.start(this)
        }
    }

}