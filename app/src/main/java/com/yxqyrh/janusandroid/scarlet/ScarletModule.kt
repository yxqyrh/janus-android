package com.yxqyrh.janusandroid.scarlet

import com.yxqyrh.janusandroid.utils.Logger.log
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.ShutdownReason
import com.tinder.scarlet.websocket.okhttp.OkHttpWebSocket
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class ScarletModule {

    companion object {
//        private const val serverUrl = "ws://139.224.43.144:8188"//"ws://janus.conf.meetecho.com/ws"*/
        private const val serverUrl = "ws://janus.conf.meetecho.com/ws"
    }

    private val protocol by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        OkHttpWebSocket(
            with(OkHttpClient.Builder()) {
                addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                addInterceptor(Interceptor { chain ->
                    return@Interceptor chain.proceed(with(chain.request().newBuilder()) {
                        addHeader("Sec-WebSocket-Protocol", "janus-protocol")
                        build()
                    })
                })
                connectTimeout(10, TimeUnit.SECONDS)
                readTimeout(10, TimeUnit.SECONDS)
                build()
            }, OkHttpWebSocket.SimpleRequestFactory(
                {
                    Request.Builder().url(serverUrl).build()
                },
                { ShutdownReason.GRACEFUL }
            )
        )
    }

    private val configuration by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {

        Scarlet.Configuration(
//            lifecycle = AndroidLifecycle.ofApplicationForeground(application),
            streamAdapterFactories = listOf(RxJava2StreamAdapterFactory())
        )
    }

    private val scarlet by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        log("scarlet created")
        Scarlet(protocol, configuration)
    }

    private val scarletService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        log("scarletService created")
        scarlet.create<ScarletService>()
    }

    val scarletRepository by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        log("scarletRepository created")
        ScarletRepository(scarletService)
    }
}