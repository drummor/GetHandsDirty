package com.android.sample.gethandsdirty.memory

import android.app.ActivityManager
import android.content.Context
import com.android.sample.gethandsdirty.app.MyApp

object MemoryCheck {
    data class MemoryInfo(
        var runtimeFreeMemory: Int = 0,
        var runtimeMaxMemroy: Int = 0,
        var runtimeTotalMemory: Int = 0,

        var sysAvailable: Int = 0,
        var isLowMemory: Boolean = false,
        var threshold: Int = 0,

        var pidInfo: String = ""

    )

    fun fetchMemoryInfo(): MemoryInfo {
        val info = MemoryInfo()
        runtimeMemory(info)
        sysMemory(info)
        pssInfo(info)
        return info
    }

    /*
        信息采集
     */
    fun runtimeMemory(info: MemoryInfo) {
        val runtime = Runtime.getRuntime()
        val free = runtime.freeMemory() / 1024f / 1024f
        val max = runtime.maxMemory() / 1024f / 1024f
        val total = runtime.totalMemory() / 1024f / 1024f

        info.runtimeFreeMemory = free.toInt()
        info.runtimeMaxMemroy = max.toInt()
        info.runtimeTotalMemory = total.toInt()

    }

    //App的可用内慈宁宫
    private fun sysMemory(info: MemoryInfo) {
        val memoryInfo = ActivityManager.MemoryInfo()
        val am =
            MyApp.myApp().applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.getMemoryInfo(memoryInfo)
        val avail = memoryInfo.availMem / 1024f / 1024f
        val isLow = memoryInfo.lowMemory
        val threshold = memoryInfo.threshold / 2024f / 1024f

        info.sysAvailable = avail.toInt()
        info.isLowMemory = isLow
        info.threshold = threshold.toInt()
    }

    private fun pssInfo(info: MemoryInfo) {
        val am =
            MyApp.myApp().applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pid = android.os.Process.myPid()
        val processMemoryInfo = am.getProcessMemoryInfo(intArrayOf(pid))
        //Debug.getMemoryInfo()
        info.pidInfo = processMemoryInfo[0].memoryStats.toString()
    }
}