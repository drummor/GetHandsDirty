package com.android.sample.gethandsdirty.anr.threadcostrecord

import android.os.Looper
import android.util.Log
import android.util.Printer
import com.android.sample.gethandsdirty.frames.hookchoreographer.LooperMonitor

class MainThreadMsgInfo {
    var historyMessage: List<MessageInfo> = emptyList()
    var currentMessage: List<MessageInfo> = emptyList()
    var futureMessage: List<MessageInfo> = emptyList()

    companion object {

    }
}

/*
    主线程调度信息
 */
object MainThreadTaskTracker : LooperMonitor.LooperListener {

    private val historyMessage = ArrayList<MessageInfo>()
    private var currentMsgInfo: MessageInfo? = null

    @Volatile
    private var isInited = false

    fun init() {
        if (isInited) {
            return
        }
        isInited = true
        LooperMonitor.getInstance(Looper.getMainLooper()).registerListener(this)
    }

    override fun dispatchStart(originString: String) {
        val msgInfo = parseMessageInfo(originString)
        msgInfo.apply { this.startTime = System.currentTimeMillis() }
        currentMsgInfo = msgInfo
    }

    override fun dispatchEnd(originString: String) {
        currentMsgInfo?.let {
            it.endTime = System.currentTimeMillis()
            recordeMessageInfo(it)
        }
    }

    private fun recordeMessageInfo(msgInfo: MessageInfo) {
        msgInfo.cost = msgInfo.endTime - msgInfo.startTime
        //todo 分类型
        if (msgInfo.cost > 50) {
            historyMessage.add(msgInfo)
        }
    }

    fun dumpMainThreadTaskInfo(): String {
        val histMsg = dumpHistMessage()
        val pendingMsg = dumpPendingMessage()
        val runningMsg = dumpRunningMessage()
        return StringBuilder()
            .append("historyTask:$histMsg\n")
            .append("pendingTask:$pendingMsg\n")
            .append("runningTask:$runningMsg\n")
            .toString()
    }

    //历史消息
    fun dumpHistMessage(): String {
        val historyInfoStr = historyMessage.toString()
        historyMessage.clear()
        return historyInfoStr
    }

    //正在执行的消息
    private fun dumpRunningMessage(): String {
        //todo
        return ""

    }

    //等在执行的message
    private fun dumpPendingMessage(): String {
        val printer = DumpPrinter()
        Looper.getMainLooper().dump(printer, "")
        val pendingMsgStr = printer.stringBuilder.toString()
        Log.d("TaskRecorder", pendingMsgStr)
        return pendingMsgStr
    }

    class DumpPrinter : Printer {
        val stringBuilder = StringBuilder()
        override fun println(x: String) {
            stringBuilder.append(x).append("\n")
        }
    }
}
