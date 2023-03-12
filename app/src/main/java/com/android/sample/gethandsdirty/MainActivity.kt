package com.android.sample.gethandsdirty

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.android.sample.gethandsdirty.choreographer.FrameCallback
import com.android.sample.gethandsdirty.choreographer.MiniChoreographer
import com.android.sample.gethandsdirty.choreographer.MiniChoreographer.Companion.CALLBACK_INPUT

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.bt_send_call_back).setOnClickListener {
            MiniChoreographer.getInstance().postCallback(callbackType = CALLBACK_INPUT,
                delayMillis = 0,
                action = object : FrameCallback {
                    override fun doFrame(frameTimeNanos: Long) {
                        findViewById<TextView>(R.id.tv_info).text = "收到了消息:${frameTimeNanos}"
                    }
                })
        }
    }
}