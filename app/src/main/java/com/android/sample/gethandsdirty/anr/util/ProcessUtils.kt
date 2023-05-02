package com.android.sample.gethandsdirty.anr.util

import android.util.Log
import java.io.File
import java.io.RandomAccessFile

object ProcessUtils {
    fun fetchFids() {
        val file = File("/proc/" + android.os.Process.myPid() + "/fd/")
        Log.d("drummor", "fid的个数：" + file.listFiles().size)

        val threadFile = RandomAccessFile("/proc/" + android.os.Process.myPid() + "/status", "r")

        val hashMap = HashMap<String, String>()
        var str: String? = ""

        while (str != null) {
            str = threadFile.readLine()
            if (str != null && str.contains(":")) {
                val split = str.split(":")
                hashMap[split[0]] = split[1]
            }
        }
        Log.d("drummor", "当前线程数量${hashMap["Threads"]}")
        Log.d("drummor", "当前进程占用的实际物理内存${hashMap["VmRSS"]}")
    }
}