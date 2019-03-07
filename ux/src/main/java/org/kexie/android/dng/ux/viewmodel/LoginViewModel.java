package org.kexie.android.dng.ux.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.mylhyl.zxing.scanner.encode.QREncode;

import org.kexie.android.dng.ux.model.LoginModule;
import org.kexie.android.dng.ux.model.entity.JsonPollingResult;
import org.kexie.android.dng.ux.model.entity.JsonQrcode;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.subjects.PublishSubject;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel
{
    private LoginModule loginModule;

    private final Executor singleTask = Executors.newSingleThreadExecutor();

    public final MutableLiveData<Drawable> qrcode = new MutableLiveData<>();

    public final PublishSubject<String> onError = PublishSubject.create();

    public final PublishSubject<String> onSuccess = PublishSubject.create();


    public LoginViewModel(@NonNull Application application)
    {
        super(application);

        singleTask.execute(()-> qrcode.postValue(getQrcode("test data")));


//        Retrofit retrofit = new Retrofit.Builder()
//                .addConverterFactory(GsonConverterFactory.create())
//                .client(new OkHttpClient())
//                .baseUrl("")
//                .build();
//        loginModule = retrofit.create(LoginModule.class);
//        requestQrcode();
    }

    private Drawable getQrcode(String s)
    {
        Bitmap bitmap = new QREncode.Builder(getApplication())
                .setContents(s)
                .build()
                .encodeAsBitmap();

        return new BitmapDrawable(getApplication().getResources(), bitmap);
    }

    public void requestQrcode()
    {
        singleTask.execute(() -> {
            try
            {
                Response<JsonQrcode> response
                        = loginModule.getQrcode().execute();
                if (response.code() == 200)
                {
                    JsonQrcode qrcode = response.body();

                    Drawable resource = getQrcode(Objects.requireNonNull(qrcode).value);

                    this.qrcode.postValue(resource);
                    polling();
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    private void polling() throws Exception
    {
        int statusCode = JsonPollingResult.CODE_STATE_CONTINUE;
        while (true)
        {
            Response<JsonPollingResult> pollingResponse
                    = loginModule.polling().execute();
            if (pollingResponse.code() != 200)
            {
                return;
            }
            switch (Objects.requireNonNull(pollingResponse.body()).statusCode)
            {
                case JsonPollingResult.CODE_STATE_CONTINUE:
                {
                    continue;
                }
                case JsonPollingResult.CODE_STATE_WAIT_FOR_CONTINUE:
                {
                    if (statusCode == JsonPollingResult.CODE_STATE_CONTINUE)
                    {
                        statusCode = JsonPollingResult.CODE_STATE_WAIT_FOR_CONTINUE;
                    }
                    continue;
                }
                case JsonPollingResult.CODE_STATE_SUCCESS:
                {
                    return;
                }
                case JsonPollingResult.CODE_STATE_INVALID:
                {
                    return;
                }
            }
            Thread.sleep(1000);
        }
    }

}
