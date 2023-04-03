package com.android.sample.gethandsdirty.anr

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Debug
import android.util.Log
import androidx.annotation.WorkerThread
import com.android.sample.gethandsdirty.app.MyApp
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.NumberFormat

object PerformanceUtils {
    private var CPU_CMD_INDEX = -1

    @JvmStatic
    fun getMemory(): Float {
        val mActivityManager: ActivityManager? =
            MyApp.myApp().getSystemService(Context.ACTIVITY_SERVICE)
                    as ActivityManager?
        var mem = 0.0f
        try {
            var memInfo: Debug.MemoryInfo? = null
            if (Build.VERSION.SDK_INT > 28) {
                // 统计进程的内存信息 totalPss
                memInfo = Debug.MemoryInfo()
                Debug.getMemoryInfo(memInfo)
            } else {
                //As of Android Q, for regular apps this method will only return information about the memory info for the processes running as the caller's uid;
                // no other process memory info is available and will be zero. Also of Android Q the sample rate allowed by this API is significantly limited, if called faster the limit you will receive the same data as the previous call.
                val memInfos =
                    mActivityManager?.getProcessMemoryInfo(intArrayOf(android.os.Process.myPid()))
                memInfos?.firstOrNull()?.apply {
                    memInfo = this
                }
            }
            memInfo?.apply {
                val totalPss = totalPss
                if (totalPss >= 0) {
                    mem = totalPss / 1024.0f
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mem
    }


    /**
     * 8.0以下获取cpu的方式
     *
     * @return
     */
    private fun getCPUData(): String {
        val commandResult =
            ShellUtils.execCommand("top -n 1 | grep ${android.os.Process.myPid()}", false)
        val msg = commandResult.successMsg
        Log.d("PerformUtils", "getCPUData  msg: $msg")
        return try {
            msg.split("\\s+".toRegex())[CPU_CMD_INDEX]
        } catch (e: Exception) {
            "0.5%"
        }
    }

    @WorkerThread
    fun getCpu(): String {
        if (CPU_CMD_INDEX == -1) {
            getCpuIndex()
        }
        if (CPU_CMD_INDEX == -1) {
            return ""
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getCpuDataForO()
        } else {
            getCPUData()
        }
    }

    /**
     * 8.0以上获取cpu的方式
     *
     * @return
     */
    private fun getCpuDataForO(): String {
        return try {
            val commandResult =
                ShellUtils.execCommand("top -n 1 | grep ${android.os.Process.myPid()}", false)
            var cpu = 0F
            Log.d("PerformUtils", "getCPUData  msg: ${commandResult.successMsg}")
            commandResult.successMsg.split("\n").forEach {
                val cpuTemp = it.split("\\s+".toRegex())
                val cpuRate = cpuTemp[CPU_CMD_INDEX].toFloatOrNull()?.div(
                    Runtime.getRuntime()
                        .availableProcessors()
                )?.div(100) ?: 0F
                cpu += cpuRate
            }
            NumberFormat.getPercentInstance().format(cpu)
        } catch (e: Exception) {
            ""
        }
    }

    private fun getCpuIndex() {
        try {
            val process = Runtime.getRuntime().exec("top -n 1")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String? = null
            while (reader.readLine().also { line = it } != null) {
                line?.let {
                    line = it.trim { it <= ' ' }
                    line?.apply {
                        val tempIndex = getCPUIndex(this)
                        if (tempIndex != -1) {
                            CPU_CMD_INDEX = tempIndex
                        }
                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun getCPUIndex(line: String): Int {
        if (line.contains("CPU")) {
            val titles = line.split("\\s+".toRegex()).toTypedArray()
            for (i in titles.indices) {
                if (titles[i].contains("CPU")) {
                    return i
                }
            }
        }
        return -1
    }
}