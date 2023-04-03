package com.android.sample.gethandsdirty.frames.srollframe

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import com.android.sample.gethandsdirty.R
import com.android.sample.gethandsdirty.frames.common.CostArrayAdapter
import com.android.sample.gethandsdirty.frames.common.FrameLineChartView
import com.android.sample.gethandsdirty.frames.common.OnFpsChangeListener

class ScrollFrameActivity : AppCompatActivity() {
    private lateinit var frameLineChartView: FrameLineChartView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scroll_frame)
        title = "采滑动帧率监控"
        initListView()
        frameLineChartView = findViewById(R.id.frame_line_char_view)
        ScrollFrameMonitor.getInstance(this.window)
            .registerListener(object : OnFpsChangeListener() {
                override fun onFpsChange(fps: Int) {
                    frameLineChartView.addFps(fps.coerceAtMost(60));
                }
            })
    }

    private fun initListView() {
        val listView = findViewById<ListView>(R.id.list_view);
        val list = ArrayList<String>()
        for (i in 1..100) {
            list.add("$i")
        }
        listView.adapter = CostArrayAdapter(this, R.layout.array_adapter_item, list)
    }

    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, ScrollFrameActivity::class.java)
            context.startActivity(starter)
        }
    }
}