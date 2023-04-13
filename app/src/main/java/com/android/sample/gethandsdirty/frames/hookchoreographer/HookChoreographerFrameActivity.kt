package com.android.sample.gethandsdirty.frames.hookchoreographer

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import com.android.sample.gethandsdirty.R
import com.android.sample.gethandsdirty.frames.common.CostArrayAdapter
import com.android.sample.gethandsdirty.frames.common.FrameLineChartView
import com.android.sample.gethandsdirty.frames.common.OnFpsChangeListener

class HookChoreographerFrameActivity : AppCompatActivity(), LooperTracker.FrameListener {
    private lateinit var frameLineChartView: FrameLineChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hook_choreographer_frame)
        title = "hook choreographer方式统计frame"
        frameLineChartView = findViewById(R.id.frame_line_char_view)
        val list = ArrayList<String>()
        for (i in 1..100) {
            list.add("$i")
        }
        findViewById<ListView>(R.id.list_view).adapter =
            CostArrayAdapter(this, R.layout.array_adapter_item, list)
        LooperTracker.getFrameTracker().register(Listener())
    }


    override fun onFrame(startNs: Long, endNs: Long, dropFrame: Int) {
        frameLineChartView.addFps(dropFrame)
    }

    inner class Listener : OnFpsChangeListener(), LooperTracker.FrameListener {
        override fun onFpsChange(fps: Int) {
            frameLineChartView.addFps(fps)
        }

        override fun onFrame(startNs: Long, endNs: Long, dropFrame: Int) {
            onFrameChange(dropFrame)
        }
    }

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, HookChoreographerFrameActivity::class.java)
            context.startActivity(starter)
        }
    }
}