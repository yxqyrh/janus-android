package com.yxqyrh.janusandroid.scarlet

import com.tinder.scarlet.websocket.WebSocketEvent
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import io.reactivex.Flowable

interface ScarletService {

    @Receive
    fun observeWebSocketEvent(): Flowable<WebSocketEvent>

    @Receive
    fun observeReceivedData(): Flowable<String>

    @Send
    fun sendData(data: String)
}