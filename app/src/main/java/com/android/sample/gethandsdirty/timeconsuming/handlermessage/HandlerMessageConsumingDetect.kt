package com.android.sample.gethandsdirty.timeconsuming.handlermessage

import android.os.Looper
import com.android.sample.gethandsdirty.frames.hookchoreographer.LooperMonitor

object HandlerMessageConsumingDetect {

    fun init(callback: (Long) -> Unit) {
        LooperMonitor.getInstance(Looper.getMainLooper())
            .registerListener(ConsumingLooperListener(callback))
    }

    class ConsumingLooperListener(private val callback: (Long) -> Unit) : LooperMonitor.LooperListener {
        var startTime = 0L;
        override fun dispatchStart(originString: String) {
            startTime = System.currentTimeMillis()
        }

        override fun dispatchEnd(originString: String) {
            val cost = System.currentTimeMillis() - startTime
            if (cost > 2_000) {
                callback.invoke(cost)
            }
        }

    }
}