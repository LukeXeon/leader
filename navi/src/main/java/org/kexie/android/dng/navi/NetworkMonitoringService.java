package org.kexie.android.dng.navi;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.orhanobut.logger.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NetworkMonitoringService extends Service
{

    private Executor singleTask = Executors.newSingleThreadExecutor();
    private WebSocketFactory factory
            = new WebSocketFactory()
            .setConnectionTimeout(10 * 1000);
    private WebSocketAdapter adapter = new WebSocketAdapter()
    {
        @Override
        public void onTextMessage(WebSocket websocket,
                                  String text)
                throws Exception
        {
            Logger.d(text);

            //NaviFragment.startOf(NetworkMonitoringService.this, netRoute);
        }

        @Override
        public void onConnected(WebSocket websocket,
                                Map<String, List<String>> headers) throws Exception
        {
            websocket.sendText("能C\n");
        }
    };

    public NetworkMonitoringService()
    {
    }

    private static Gson getGson()
    {
        return new Gson();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        singleTask.execute(() -> {
            try
            {
                WebSocket  webSocket = factory
                        .createSocket("ws://172.20.10.5:8080/navigator/websocket/device/navigation");
                webSocket.addListener(adapter);
                webSocket.connect();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });

    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
