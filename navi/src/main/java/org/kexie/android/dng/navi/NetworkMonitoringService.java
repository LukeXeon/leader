package org.kexie.android.dng.navi;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.navi.entity.NetRoute;
import org.kexie.android.dng.navi.model.Point;
import org.kexie.android.dng.navi.util.PointUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NetworkMonitoringService extends Service
{
    private static class Entity
    {
        @SerializedName("passPoints")
        private List<Point> points;
    }

    private Gson gson = PointUtil.getJsonConverter();
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
            Entity entity = gson.fromJson(text, Entity.class);
            NetRoute netRoute = new NetRoute();
            netRoute.setFrom(entity.points.get(0));
            netRoute.setTo(entity.points.get(entity.points.size() - 1));
            netRoute.setPoints(
                    new ArrayList<>(entity.points.subList(1, entity.points.size() - 1)));
            //NavigationFragment.startOf(NetworkMonitoringService.this, netRoute);
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

    @Override
    public void onCreate()
    {
        super.onCreate();
        singleTask.execute(new Runnable()
        {
            @Override
            public void run()
            {
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
            }
        });

    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
