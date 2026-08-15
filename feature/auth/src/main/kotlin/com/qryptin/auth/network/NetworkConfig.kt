package com.qryptin.auth.network

/**
 * Global network configuration for the app.
 */
object NetworkConfig {
    /**
     * The host and port for the backend server.
     * 
     * - Use "10.0.2.2:8080" for the Android Emulator to connect to localhost.
     * - Use your machine's local IP (e.g., "192.168.x.x:8080") when testing on a physical device.
     */
    private const val HOST = "192.168.0.158:8080"

    /**
     * Base URL for REST API calls.
     */
    const val BASE_URL = "http://$HOST/"

    /**
     * WebSocket URL for STOMP connection.
     */
    const val WS_URL = "ws://$HOST/ws"
}
