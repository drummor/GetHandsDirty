package com.android.sample.gethandsdirty.frames.poorframe

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import com.android.sample.gethandsdirty.R
import com.android.sample.gethandsdirty.frames.common.CostArrayAdapter
import com.android.sample.gethandsdirty.frames.common.FrameLineChartView
import com.android.sample.gethandsdirty.frames.common.OnFpsChangeListener
import com.android.sample.gethandsdirty.frames.common.OnFrameChangeListener

class PoorFrameActivity : AppCompatActivity() {
    private lateinit var frameLineChartView: FrameLineChartView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poor_frame)
        title = "简单利用Choreographer方式的采集Frame"
        frameLineChartView = findViewById(R.id.frame_line_char_view)
        initListView()
        PoorFrameTracker.getInstance().register(object : OnFpsChangeListener() {
            override fun onFpsChange(fps: Int) {
                frameLineChartView.addFps(fps)
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
            val starter = Intent(context, PoorFrameActivity::class.java)
            context.startActivity(starter)
        }
    }
}