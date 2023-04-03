package com.android.sample.gethandsdirty.frames.framemetrics

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.FrameMetrics
import android.view.Window
import android.widget.ListView
import com.android.sample.gethandsdirty.R
import com.android.sample.gethandsdirty.choreographer.NANOS_PER_MS
import com.android.sample.gethandsdirty.frames.common.CostArrayAdapter
import com.android.sample.gethandsdirty.frames.common.FrameLineChartView
import com.android.sample.gethandsdirty.frames.common.OnFpsChangeListener

class FrameMetricAvailableActivity : AppCompatActivity() {

    private lateinit var frameLineChartView: FrameLineChartView
    private val onFpsChangeListener = object : OnFpsChangeListener() {
        override fun onFpsChange(fps: Int) {
            frameLineChartView.post { frameLineChartView.addFps(fps) }
        }
    }

    private val metricsAvailableListener =
        Window.OnFrameMetricsAvailableListener { window, frameMetrics, dropCountSinceLastInvocation ->
            val intent = frameMetrics?.getMetric(FrameMetrics.INTENDED_VSYNC_TIMESTAMP) ?: 0
            val vsync = frameMetrics?.getMetric(FrameMetrics.VSYNC_TIMESTAMP) ?: 0
            val animation = frameMetrics?.getMetric(FrameMetrics.ANIMATION_DURATION) ?: 0
            val measureCost = frameMetrics?.getMetric(FrameMetrics.LAYOUT_MEASURE_DURATION) ?: 0

            val vsyncTotal = frameMetrics?.getMetric(FrameMetrics.TOTAL_DURATION) ?: 0
            val dropFrame = ((vsyncTotal / NANOS_PER_MS) / 16.66667f).toInt()
            onFpsChangeListener.onFrameChange(dropFrame = dropFrame)
        }
    private val handlerThread = HandlerThread("-- ")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frame_metric_available)
        title = "利用官方FrameMetricsAvailable采集FPS"
        handlerThread.start()
        frameLineChartView = findViewById(R.id.frame_line_char_view)
        initListView()
        this.window.addOnFrameMetricsAvailableListener(
            metricsAvailableListener, Handler(handlerThread.looper)
        )
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
            val starter = Intent(context, FrameMetricAvailableActivity::class.java)
            context.startActivity(starter)
        }
    }
}