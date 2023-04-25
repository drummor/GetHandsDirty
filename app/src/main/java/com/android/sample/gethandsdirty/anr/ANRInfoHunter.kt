package com.android.sample.gethandsdirty.anr

import android.app.ActivityManager
import android.content.Context
import com.android.sample.gethandsdirty.app.MyApp
import java.util.concurrent.Callable
import java.util.concurrent.Executors

data class ANRSysInfo(
    var pid: Int = 0,
    var uid: Int = 0,
    var processName: String = "",
    var shortMsg: String = "",
    var longMsg: String = "",
    var tag: String = ""
)

/*
   收集ANR时系统信息
 */
object ANRInfoHunter {
    private const val THREAD_NAME = "thread_anr_collect"
    private const val TRY_COUNT = 100
    private const val TRY_INTERVAL_TIME = 50L
    private val anrHunterExecutor = Executors.newSingleThreadExecutor {
        Thread().apply {
            this.name = THREAD_NAME
        }
    }

    @Volatile
    var isChecking: Boolean = false
    fun startCollect() {
        if (isChecking) {
            return
        }
        isChecking = true
        anrHunterExecutor.submit(Callable<List<ANRSysInfo>> {
            var count = 0
            while (count++ < TRY_COUNT) {
                val am = MyApp.myApp().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val processesInErrorStates = am.processesInErrorState
                if (processesInErrorStates == null || processesInErrorStates.isEmpty()) {
                    Thread.sleep(TRY_INTERVAL_TIME)
                    continue
                }
                val anrInfoList = ArrayList<ANRSysInfo>()
                processesInErrorStates.forEach { errorInfo ->
                    anrInfoList.add(parseAnrInfo(errorInfo))
                }
                return@Callable anrInfoList
            }
            return@Callable emptyList<ANRSysInfo>()
        })
    }

    private fun parseAnrInfo(stateInfo: ActivityManager.ProcessErrorStateInfo): ANRSysInfo {
        return ANRSysInfo().apply {
            this.pid = stateInfo.pid
            this.uid = stateInfo.uid
            this.processName = stateInfo.processName
            this.shortMsg = stateInfo.shortMsg
            this.longMsg = stateInfo.longMsg
            this.tag = stateInfo.tag
        }
    }
}