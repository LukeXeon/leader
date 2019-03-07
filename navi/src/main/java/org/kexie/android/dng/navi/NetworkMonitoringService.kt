package org.kexie.android.dng.navi

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.gson.Gson
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketFactory
import com.orhanobut.logger.Logger
import java.util.concurrent.Executors

class NetworkMonitoringService : Service() {

    private val singleTask = Executors.newSingleThreadExecutor()
    private val factory = WebSocketFactory().apply {
        connectionTimeout = 10 * 1000
    }

    private val adapter = object : WebSocketAdapter() {

        @Throws(Exception::class)
        override fun onTextMessage(websocket: WebSocket?,
                                   text: String?) {
            Logger.d(text)

            //NaviFragment2.startOf(NetworkMonitoringService.this, netRoute);
        }

        @Throws(Exception::class)
        override fun onConnected(websocket: WebSocket,
                                 headers: Map<String, List<String>>?) {
            websocket.sendText("能C\n")
        }
    }

    private val gson: Gson
        get() = Gson()

    override fun onCreate() {
        super.onCreate()
        singleTask.execute {
            try {

                val webSocket = factory
                        .createSocket("ws://172.20.10.5:8080/navigator/websocket/device/navigation")
                webSocket.addListener(adapter)
                webSocket.connect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
