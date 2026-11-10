package com.hato.data.realtime

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.hato.data.api.ApiClient
import com.hato.data.dto.AlertEventDto
import com.hato.data.dto.PositionEventDto
import okhttp3.*

class LiveSocketManager(
    private val onPositionReceived: (PositionEventDto) -> Unit,
    private val onAlertReceived: (AlertEventDto) -> Unit
) {
    private val gson = Gson()
    private var webSocket: WebSocket? = null
    var isConnected = false
        private set

    fun connect(farmId: String) {
        val httpUrl = ApiClient.baseUrl
        val wsUrl = if (httpUrl.startsWith("https://")) {
            httpUrl.replace("https://", "wss://") + "ws/live?farmId=$farmId"
        } else {
            httpUrl.replace("http://", "ws://") + "ws/live?farmId=$farmId"
        }

        val request = Request.Builder()
            .url(wsUrl)
            .build()

        webSocket = ApiClient.okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                Log.d("LiveSocketManager", "Conectado al WebSocket en vivo: $wsUrl")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JsonParser.parseString(text).asJsonObject
                    val type = json.get("type")?.asString
                    when (type) {
                        "position" -> {
                            val event = gson.fromJson(text, PositionEventDto::class.java)
                            onPositionReceived(event)
                        }
                        "alert" -> {
                            val event = gson.fromJson(text, AlertEventDto::class.java)
                            onAlertReceived(event)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("LiveSocketManager", "Error parseando mensaje WS", e)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                Log.d("LiveSocketManager", "Cerrando WebSocket: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                Log.w("LiveSocketManager", "Fallo de conexión WebSocket: ${t.message}")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "App cerrada")
        webSocket = null
        isConnected = false
    }
}
