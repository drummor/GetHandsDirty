package com.android.sample.gethandsdirty.timeconsuming.idlehandler

import android.os.MessageQueue

class IdleHandlerWrapper(
    val orgIdleHandler: MessageQueue.IdleHandler,
    private val callback: (Long) -> Unit
) : MessageQueue.IdleHandler {


    override fun queueIdle(): Boolean {
        val startTime = System.currentTimeMillis()
        val ret = orgIdleHandler.queueIdle()
        val costTime = System.currentTimeMillis() - startTime
        if (costTime > 2_000) {
            callback.invoke(costTime)
        }
        return ret
    }
}