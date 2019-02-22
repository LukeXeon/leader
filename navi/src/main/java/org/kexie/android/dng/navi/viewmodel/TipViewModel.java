package org.kexie.android.dng.navi.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;

import org.kexie.android.common.databinding.GenericQuickAdapter;
import org.kexie.android.dng.navi.viewmodel.entity.LiteTip;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;
import java8.util.stream.StreamSupport;

public class TipViewModel extends AndroidViewModel
{
    public static final String DEBUG_TEXT = "火车站";

    private static final String CITY = "西安";

    private final Executor singleTask = Executors.newSingleThreadExecutor();

    private final MutableLiveData<String> queryText = new MutableLiveData<>();

    private final PublishSubject<String> onLoading = PublishSubject.create();

    private final PublishSubject<String> onErrorMessage = PublishSubject.create();

    private final PublishSubject<String> onSuccessMessage = PublishSubject.create();

    private GenericQuickAdapter<LiteTip> adapter;

    public void bindAdapter(GenericQuickAdapter<LiteTip> adapter)
    {
        this.adapter = adapter;
    }

    public TipViewModel(@NonNull Application application)
    {
        super(application);
    }

    @MainThread
    public void query(String text)
    {
        singleTask.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                adapter.getData().clear();
                adapter.notifyDataSetChanged();
            });
            InputtipsQuery inputtipsQuery = new InputtipsQuery(text, CITY);
            Inputtips inputtips = new Inputtips(getApplication(), inputtipsQuery);
            try
            {
                StreamSupport.stream(inputtips.requestInputtips())
                        .filter(tip -> !TextUtils.isEmpty(tip.getPoiID()))
                        .map(x -> new LiteTip(x.getName(), x.getPoiID()))
                        .forEach(tip -> handler.post(() -> adapter.addData(tip)));
            } catch (Exception e)
            {
                e.printStackTrace();
                onErrorMessage.onNext("输入提示查询失败,请检查网络连接");
            }
        });
    }


    public Observable<String> getOnSuccessMessage()
    {
        return onSuccessMessage.observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<String> getOnErrorMessage()
    {
        return onErrorMessage.observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<String> getOnLoading()
    {
        return onLoading.observeOn(AndroidSchedulers.mainThread());
    }
}