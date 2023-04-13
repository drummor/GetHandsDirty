package com.android.sample.gethandsdirty.anr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.android.sample.gethandsdirty.R
import com.android.sample.gethandsdirty.anr.threadcostrecord.MainThreadTaskTracker
import com.android.sample.gethandsdirty.anr.util.CpuInfoUtils
import com.android.sample.gethandsdirty.anr.util.DeviceUtil
import com.android.sample.gethandsdirty.app.MyApp

class ANRActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anractivity)
        findViewById<Button>(R.id.bt_fetch_dump).setOnClickListener {
            Log.d("ANRActivity", "cpuInfo:${DeviceUtil.getAppCpuRate()}")
            Log.d("ANRActivity", "memoryInfo:${DeviceUtil.getAvailMemory(MyApp.myApp())}")
            Log.d(
                "ANRActivity",
                "readMaxFrequencies:${CpuInfoUtils.getInstance().readMaxFrequencies()}"
            )
            DeviceUtil.fetchSingleCpuTotalTime();
            Log.d("ANRActivity", "获取cpu的个数:" + CpuInfoUtils.getNumberOfCPUCores())
        }

        MainThreadTaskTracker.init()
        findViewById<Button>(R.id.bt_catch_anr).setOnClickListener {
            Thread.sleep(7_000)
            ANRInfoHunter.startCollect()
            MainThreadTaskTracker.dumpMainThreadTaskInfo()
        }
    }


    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, ANRActivity::class.java)
            context.startActivity(starter)
        }
    }
}