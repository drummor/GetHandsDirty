package com.android.sample.gethandsdirty.timeconsuming.idlehandler

import android.os.*
import android.os.MessageQueue.IdleHandler


object IdleHandlerTracker {

    fun init(whenIdleHandlerRunTooLong: (Long) -> Unit) {
        injectMyIdleHandlerList(whenIdleHandlerRunTooLong)
    }

    private fun injectMyIdleHandlerList(callback: (Long) -> Unit) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return
            }

            val mainQueue = Looper.getMainLooper().queue
            val field = MessageQueue::class.java.getDeclaredField("mIdleHandlers")
            field.isAccessible = true
            val idleHdlList = IdleHandlerArrayList<IdleHandler>(callback)
            field[mainQueue] = idleHdlList
        } catch (t: Throwable) {

        }
    }
}
