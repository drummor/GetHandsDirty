package com.android.sample.gethandsdirty.anr

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.android.sample.gethandsdirty.app.MyApp

data class ANRInfo(
    var pid: Int = 0,
    var uid: Int = 0,
    var processName: String = "",
    var shortMsg: String = "",
    var longMsg: String = "",
    var tag: String = ""
)


object ANRInfoHunter {
    private const val THREAD_NAME = "thread_anr_collect"
    private fun parseAnrInfo(stateInfo: ActivityManager.ProcessErrorStateInfo): ANRInfo {
        return ANRInfo().apply {
            this.pid = stateInfo.pid
            this.uid = stateInfo.uid
            this.processName = stateInfo.processName
            this.shortMsg = stateInfo.shortMsg
            this.longMsg = stateInfo.longMsg
            this.tag = stateInfo.tag
        }
    }

    @Volatile
    var isChecking: Boolean = false
    fun startCollect() {
        if (isChecking) {
            return
        }
        isChecking = true
        Thread {
            while (true) {
                val am = MyApp.myApp().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val processesInErrorStates = am.processesInErrorState
                if (processesInErrorStates == null || processesInErrorStates.isEmpty()) {
                    Thread.sleep(100)
                    continue
                }
                val anrInfoList = ArrayList<ANRInfo>()
                processesInErrorStates.forEach { errorInfo ->
                    anrInfoList.add(parseAnrInfo(errorInfo))
                }
                Log.d("ANRInfoCollector", anrInfoList.toString())
                return@Thread
            }
        }.apply {
            this.name = THREAD_NAME
        }.start()
    }
}