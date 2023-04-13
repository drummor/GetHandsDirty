package com.android.sample.gethandsdirty.anr

import android.app.ActivityManager
import android.app.ActivityManager.ProcessErrorStateInfo
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.sample.gethandsdirty.R

class ANRTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anrtest2)

        findViewById<View>(R.id.bt_catch_anr).setOnClickListener {
            Thread.sleep(10_000)
            getANRMsg()
        }
    }

    private fun getANRMsg(): String {
        val am = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val errorStateInfo: List<ProcessErrorStateInfo> =
            am.processesInErrorState
        if (errorStateInfo != null) {
            for (info in errorStateInfo) {
                if (info.condition == ProcessErrorStateInfo.NOT_RESPONDING) {
                    val anrInfo = StringBuilder()
                    anrInfo.append(info.processName)
                        .append("\n")
                        .append(info.shortMsg)
                        .append("\n")
                        .append(info.longMsg)
                    return anrInfo.toString()
                }
            }
        }
        return ""
    }
}