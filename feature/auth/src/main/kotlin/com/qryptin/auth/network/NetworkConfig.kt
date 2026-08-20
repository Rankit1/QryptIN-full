package com.qryptin.auth.network

/**
 * Global network configuration for the app.
 */
object NetworkConfig {
    /**
     * The host and port for the backend server.
     * 
     * - Use "10.0.2.2:8081" for the Android Emulator to connect to localhost.
     * - Use your machine's local IP (e.g., "192.168.1.x:8081") when testing on a physical device.
     */
    private const val HOST = "192.168.1.6:8081"

    /**
     * Base URL for REST API calls.
     */
    const val BASE_URL = "http://$HOST/"

    /**
     * WebSocket URL for STOMP connection.
     * Note: We use "ws" directly as the backend exposes the standard websocket endpoint.
     */
    const val WS_URL = "ws://$HOST/ws"
}
