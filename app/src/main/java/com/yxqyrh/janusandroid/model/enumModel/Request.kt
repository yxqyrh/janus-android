package com.yxqyrh.janusandroid.model.enumModel

enum class Request(val request: String) {
    REGISTER("register"),
    JOIN("join"),
    LIST("list"),
    LIST_PARTICIPANTS("listparticipants"),
    MODERATE("moderate"),
    LIST_FORWARDERS("listforwarders"),
    KICK("kick"),
    DESTROY("destroy"),
    CREATE("create"),
    CALL("call"),
    ACCEPT("accept"),
    SET("set"),
    HANGUP("hangup"),
    LEAVE("leave"),
    CONFIGURE("configure"),
    START("start")
}