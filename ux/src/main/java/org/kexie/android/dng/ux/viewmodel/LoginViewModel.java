package org.kexie.android.dng.ux.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.mylhyl.zxing.scanner.encode.QREncode;

import org.kexie.android.dng.ux.model.LoginModule;
import org.kexie.android.dng.ux.model.beans.JsonPollingResult;
import org.kexie.android.dng.ux.model.beans.JsonQrcode;
import org.kexie.android.dng.ux.widget.CookieCache;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginViewModel extends AndroidViewModel {
    private LoginModule loginModule;

    private final ExecutorService singleTask = Executors.newSingleThreadExecutor();

    public final MutableLiveData<Drawable> qrcode = new MutableLiveData<>();

    public final MutableLiveData<String> text = new MutableLiveData<>();

    public final MutableLiveData<Boolean> result = new MutableLiveData<>();

    private Future<?> future;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .cookieJar(new CookieCache())
                        .build())
                .baseUrl("https://www.hao123.com")
                .build();
        loginModule = retrofit.create(LoginModule.class);
        requestQrcode();
    }

    private Drawable getQrcode(String s) {
        Bitmap bitmap = new QREncode.Builder(getApplication())
                .setContents(s)
                .build()
                .encodeAsBitmap();
        return new BitmapDrawable(getApplication().getResources(), bitmap);
    }

    public void requestQrcode() {
        text.setValue("使用微信小程序扫码登录");
        future = singleTask.submit(() -> {
            boolean r;
            try {
                Response<JsonQrcode> response
                        = loginModule.getQrcode().execute();
                if (response.code() == 200) {
                    JsonQrcode qrcode = response.body();
                    if (qrcode == null) {
                        return;
                    }
                    Drawable resource = getQrcode(qrcode.value);
                    this.qrcode.postValue(resource);
                    r = polling();
                } else {
                    r = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                text.postValue("加载二维码出错,请点击重试");
                r = false;
            }
            result.postValue(r);
        });
    }

    private boolean polling() throws Exception {
        int statusCode = JsonPollingResult.CODE_STATE_CONTINUE;
        while (true) {
            Response<JsonPollingResult> pollingResponse
                    = loginModule.polling().execute();
            if (pollingResponse.code() != 200) {
                return false;
            }
            JsonPollingResult responseBody = pollingResponse.body();
            if (responseBody == null) {
                return false;
            }
            switch (responseBody.statusCode) {
                case JsonPollingResult.CODE_STATE_CONTINUE: {
                    continue;
                }
                case JsonPollingResult.CODE_STATE_WAIT_FOR_CONTINUE: {
                    if (statusCode == JsonPollingResult.CODE_STATE_CONTINUE) {
                        statusCode = JsonPollingResult.CODE_STATE_WAIT_FOR_CONTINUE;
                    }
                    continue;
                }
                case JsonPollingResult.CODE_STATE_SUCCESS: {
                    return true;
                }
                case JsonPollingResult.CODE_STATE_INVALID: {
                    return true;
                }
            }
            Thread.sleep(1000);
        }
    }

    @Override
    protected void onCleared() {
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
        }
    }
}
