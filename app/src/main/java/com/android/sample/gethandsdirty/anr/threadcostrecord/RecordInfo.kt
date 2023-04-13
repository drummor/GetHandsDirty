package com.android.sample.gethandsdirty.anr.threadcostrecord

enum class TaskType {
    HUGE,
    SMALL,
    IDlE,
}


class TaskInfo {
    val target: Object? = null;
    val callback = 0;
    val what = 0;
}

class RecordInfo() {
    var recordType: TaskType? = null;
    var messageInfo: TaskInfo? = null//message信息
    var stackInfo: String? = ""//堆栈信息
    var taskCost: String? = null//耗时信息
}

data class MessageInfo(
    var handler: String = "",
    var callback: String = "",
    var what: Int = -1,
    var other: String = "",

    var startTime: Long = 0,
    var endTime: Long = 0,
    var cost: Long = 0
)


fun parseMessageInfo(msg: String): MessageInfo {
    var boxMessage: MessageInfo = MessageInfo()
    try {
        var msg = msg.trim { it <= ' ' }
        var msgA = msg.split(":".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val what = msgA[1].trim { it <= ' ' }.toInt()
        msgA = msgA[0].split("\\{.*\\}".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val callback = msgA[1]

        msgA = msgA[0].split("\\(".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        msgA = msgA[1].split("\\)".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val handler = msgA[0]
        msgA = msg.split("\\{".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        msgA = msgA[1].split("\\}".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        boxMessage.apply {
            this.handler = handler
            this.callback = callback
            this.what = what
            this.other = msgA[0]
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return boxMessage
}

object TaskInfoManager {
    private val list = ArrayList<TaskInfo>()
    fun appendInfo(taskInfo: TaskInfo) {
        list.add(taskInfo)
    }

    fun dumpMessage() {
    }
}