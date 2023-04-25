package com.android.sample.gethandsdirty.anr

import android.util.Log
import com.android.sample.gethandsdirty.anr.threadcostrecord.MainThreadTaskTracker

/*
   ANR监控采集入口
 */
class ANRTracker {
    companion object {
        const val LOG_TAG = "ANRTracker"
    }

    @Volatile
    private var initialed = false

    @Volatile
    private var inInit = false
    fun init() {
        if (initialed || inInit) {
            Log.d(LOG_TAG, " 不要重复初始化 ")
            return
        }
        initialed = true
        inInit = true
        //主线程调度采集初始化
        MainThreadTaskTracker.init()
        inInit = false
        initialed = true
    }

    fun dumpAnrInfo() {
        MainThreadTaskTracker.dumpMainThreadTaskInfo()
        ANRInfoHunter.startCollect()
    }
}