package org.kexie.android.dng.navi.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.orhanobut.logger.Logger;

import org.kexie.android.common.databinding.GenericQuickAdapter;
import org.kexie.android.dng.navi.viewmodel.entity.InputTip;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
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

    private final MutableLiveData<Boolean> isShowTips = new MutableLiveData<>();

    private final MutableLiveData<String> queryText = new MutableLiveData<>();

    private final PublishSubject<String> onErrorMessage = PublishSubject.create();

    private final PublishSubject<String> onSuccessMessage = PublishSubject.create();

    private GenericQuickAdapter<InputTip> adapter;


    public TipViewModel(@NonNull Application application)
    {
        super(application);
        isShowTips.setValue(false);
    }

    @MainThread
    public void query(String text)
    {
        Logger.d(text);
        isShowTips.setValue(false);
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
                        .map(x -> new InputTip(x.getName(), x.getPoiID()))
                        .forEach(tip -> handler.post(() -> adapter.addData(tip)));
                isShowTips.postValue(true);
                onSuccessMessage.onNext("搜索成功");
            } catch (Exception e)
            {
                isShowTips.postValue(false);
                e.printStackTrace();
                onErrorMessage.onNext("输入提示查询失败,请检查网络连接");
            }
        });
    }

    public void bindAdapter(GenericQuickAdapter<InputTip> adapter)
    {
        this.adapter = adapter;
    }

    public LiveData<Boolean> getIsShowTips()
    {
        return isShowTips;
    }

    public LiveData<String> getQueryText()
    {
        return queryText;
    }

    public Observable<String> getOnSuccessMessage()
    {
        return onSuccessMessage.observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<String> getOnErrorMessage()
    {
        return onErrorMessage.observeOn(AndroidSchedulers.mainThread());
    }
}