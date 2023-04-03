package com.android.sample.gethandsdirty.timeconsuming.idlehandler

import android.os.MessageQueue

internal class IdleHandlerArrayList<T>(
    private val callback: (Long) -> Unit
) :
    ArrayList<Any>() {
    private var map: MutableMap<MessageQueue.IdleHandler, IdleHandlerWrapper> =
        HashMap()

    override fun add(o: Any): Boolean {
        if (o is MessageQueue.IdleHandler) {
            val myIdleHandler =
                IdleHandlerWrapper(o, callback)
            map[o] = myIdleHandler
            return super.add(myIdleHandler)
        }
        return super.add(o)
    }

    override fun remove(o: Any): Boolean {
        return if (o is IdleHandlerWrapper) {
            val idleHandler: MessageQueue.IdleHandler = o.orgIdleHandler
            map.remove(idleHandler)
            super.remove(o)
        } else {
            val myIdleHandler: IdleHandlerWrapper? = map.remove(o)
            if (myIdleHandler != null) {
                super.remove(myIdleHandler)
            } else super.remove(o)
        }
    }
}