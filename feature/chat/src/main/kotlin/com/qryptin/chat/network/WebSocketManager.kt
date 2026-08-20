package com.qryptin.chat.network

import android.util.Log
import com.qryptin.auth.network.NetworkConfig
import io.reactivex.Completable
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
    private var isConnected = false
    private var currentUserId: String? = null
    private var onMessageReceivedCallback: ((String) -> Unit)? = null

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .pingInterval(10, TimeUnit.SECONDS) // Keep connection alive at OkHttp level
        .build()

    private var connectionAttempts = 0
    private val urlFallbacks = listOf(NetworkConfig.WS_URL)

    fun connect(url: String? = null) {
        val targetUrl = url ?: urlFallbacks[connectionAttempts % urlFallbacks.size]
        
        if (isConnected) {
            Log.d("WebSocket", "Already connected, skipping connect call")
            return
        }
        
        Log.d("WebSocket", "Initiating Stomp connection to $targetUrl (Attempt ${connectionAttempts + 1})")

        lifecycleDisposable?.dispose()

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, targetUrl, null, okHttpClient)
        
        lifecycleDisposable = stompClient?.lifecycle()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { lifecycleEvent ->
                when (lifecycleEvent.type) {
                    LifecycleEvent.Type.OPENED -> {
                        Log.d("WebSocket", "Stomp connection OPENED successfully")
                        isConnected = true
                        connectionAttempts = 0 // Reset on success
                        // Re-subscribe if we were already waiting for a user
                        currentUserId?.let { subscribeToChat(it, onMessageReceivedCallback ?: {}) }
                    }
                    LifecycleEvent.Type.ERROR -> {
                        val exception = lifecycleEvent.exception
                        Log.e("WebSocket", "Stomp connection ERROR on $targetUrl", exception)
                        
                        if (exception?.message?.contains("404") == true) {
                            Log.w("WebSocket", "Endpoint 404 - will try next fallback in 5s")
                        } else if (exception is java.net.SocketTimeoutException || (exception?.message?.contains("ETIMEDOUT") == true)) {
                            Log.e("WebSocket", "CONNECTION HINT: Check if the IP $targetUrl is correct and reachable. " +
                                    "Ensure your backend is running and the firewall allows port ${targetUrl.substringAfterLast(":").substringBefore("/")}. " +
                                    "If using an emulator, try 10.0.2.2.")
                        }

                        isConnected = false
                        connectionAttempts++
                        // Automatic reconnection logic with the next fallback
                        scheduleReconnect()
                    }
                    LifecycleEvent.Type.CLOSED -> {
                        Log.w("WebSocket", "Stomp connection CLOSED")
                        isConnected = false
                    }
                    else -> Log.d("WebSocket", "Stomp event: ${lifecycleEvent.type}")
                }
            }

        stompClient?.withClientHeartbeat(10000)?.withServerHeartbeat(10000)
        stompClient?.connect()
    }

    private fun scheduleReconnect() {
        Completable.timer(5, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ 
                Log.d("WebSocket", "Attempting automatic reconnect...")
                connect()
            }, {
                Log.e("WebSocket", "Reconnect timer failed", it)
            })
    }

    fun subscribeToChat(userId: String, onMessageReceived: (String) -> Unit = {}) {
        this.currentUserId = userId
        if (onMessageReceived != {}) {
            this.onMessageReceivedCallback = onMessageReceived
        }
        topicDisposable?.dispose()
        
        if (stompClient == null || !isConnected) {
            Log.w("WebSocket", "Cannot subscribe yet, connection not ready. Will subscribe when OPENED.")
            return
        }

        // As per backend requirements, we ONLY subscribe to /topic/chat/$userId
        val topicPath = "/topic/chat/$userId"
        
        Log.d("WebSocket", "Subscribing to: $topicPath")

        topicDisposable = stompClient?.topic(topicPath)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ stompMessage ->
                Log.d("WebSocket", "MESSAGE RECEIVED: ${stompMessage.payload}")
                onMessageReceived(stompMessage.payload)
            }, { Log.e("WebSocket", "Sub error: $topicPath", it) })
    }

    fun sendMessage(jsonMessage: String) {
        if (!isConnected) {
            Log.w("WebSocket", "Not connected, cannot send message. Attempting reconnect.")
            connect()
            return
        }

        stompClient?.send("/app/chat.send", jsonMessage)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                Log.d("WebSocket", "STOMP SEND SUCCESS")
            }, { throwable ->
                Log.e("WebSocket", "STOMP SEND FAILED", throwable)
            })
    }

    fun disconnect() {
        stompClient?.disconnect()
        topicDisposable?.dispose()
        lifecycleDisposable?.dispose()
    }
}
