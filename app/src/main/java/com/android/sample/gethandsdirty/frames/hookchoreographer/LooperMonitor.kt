package com.android.sample.gethandsdirty.frames.hookchoreographer

import android.os.Looper
import android.util.Printer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class LooperMonitor(private val looper: Looper) {
    companion object {
        private val mLooperMonitorMap = ConcurrentHashMap<Looper, LooperMonitor>()
        fun getInstance(looper: Looper): LooperMonitor {
            var looperMonitor = mLooperMonitorMap[looper]
            if (looperMonitor == null) {
                looperMonitor = LooperMonitor(looper)
                mLooperMonitorMap[looper] = looperMonitor
            }
            return looperMonitor
        }
    }

    init {
        looper.setMessageLogging(LooperPrinter())
    }

    private val listeners = CopyOnWriteArrayList<LooperListener>()
    fun registerListener(listener: LooperListener) {
        listeners.add(listener)
    }

    inner class LooperPrinter : Printer {
        override fun println(x: String) {
            val isBegin = x[0] == '>'
            dispatch(isBegin, x)
        }
    }

    private fun dispatch(isBegin: Boolean, originString: String) {
        listeners.forEach {
            if (isBegin) {
                it.dispatchStart(originString)
            } else {
                it.dispatchEnd(originString)
            }
        }
    }

    interface LooperListener {
        fun dispatchStart(originString: String)
        fun dispatchEnd(originString: String)
    }
}