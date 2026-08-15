package com.qryptin.chat.network

import android.util.Log
import com.qryptin.auth.network.NetworkConfig
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import java.util.concurrent.TimeUnit

class WebSocketManager {
    private var stompClient: StompClient? = null
    private var topicDisposable: Disposable? = null
    private var lifecycleDisposable: Disposable? = null

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    fun connect(url: String = NetworkConfig.WS_URL) {
        // Clear previous subscriptions if any
        topicDisposable?.dispose()
        lifecycleDisposable?.dispose()

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url, null, okHttpClient)
        
        lifecycleDisposable = stompClient?.lifecycle()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { lifecycleEvent ->
                when (lifecycleEvent.type) {
                    LifecycleEvent.Type.OPENED -> Log.d("WebSocket", "Stomp connection opened at $url")
                    LifecycleEvent.Type.ERROR -> {
                        Log.e("WebSocket", "Stomp connection error at $url", lifecycleEvent.exception)
                        // Optional: add retry logic here if needed
                    }
                    LifecycleEvent.Type.CLOSED -> Log.d("WebSocket", "Stomp connection closed")
                    else -> {}
                }
            }

        // Add persistent heartbeat (default is 10000ms)
        stompClient?.withClientHeartbeat(10000)?.withServerHeartbeat(10000)

        stompClient?.connect()
    }

    fun subscribeToChat(userId: String, onMessageReceived: (String) -> Unit) {
        topicDisposable = stompClient?.topic("/topic/chat/$userId")
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ stompMessage ->
                onMessageReceived(stompMessage.payload)
            }, { throwable ->
                Log.e("WebSocket", "Error on subscribe", throwable)
            })
    }

    fun sendMessage(jsonMessage: String) {
        stompClient?.send("/app/chat.send", jsonMessage)?.subscribe({
            Log.d("WebSocket", "Message sent successfully")
        }, { throwable ->
            Log.e("WebSocket", "Error sending message", throwable)
        })
    }

    fun disconnect() {
        stompClient?.disconnect()
        topicDisposable?.dispose()
        lifecycleDisposable?.dispose()
    }
}
