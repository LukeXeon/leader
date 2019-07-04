package org.kexie.android.dng.navi.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.common.contract.ASR;
import org.kexie.android.dng.common.contract.LBS;
import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.common.util.LiveEvent;
import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.navi.BR;
import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.model.beans.Point;
import org.kexie.android.dng.navi.viewmodel.beans.TipText;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class SearchViewModel
        extends AndroidViewModel
        implements ASR.Handler {

    public final GenericQuickAdapter<TipText> tips
            = new GenericQuickAdapter<>(R.layout.item_tip, BR.tip);

    public final LiveEvent<String> speechResult = new LiveEvent<>();

    public final MutableLiveData<Integer> status = new MutableLiveData<>();

    public final MutableLiveData<String> speechPartText = new MutableLiveData<>();

    public ASR asr;

    private HandlerThread workerThread;

    private Handler worker;

    private Handler main;

    private LBS.Session session;

    public SearchViewModel(@NonNull Application application) {
        super(application);
        workerThread = new HandlerThread("search");
        workerThread.start();
        worker = new Handler(workerThread.getLooper());
        main = new Handler(Looper.getMainLooper());
        LBS lbs = (LBS) ARouter.getInstance().build(Module.Navi.location)
                .navigation(application);
        session = lbs.use();
        asr = (ASR) ARouter.getInstance().build(Module.Ai.asr).navigation();
        asr.addHandler(this);
    }

    public void search(String text) {
        worker.removeCallbacksAndMessages(null);
        worker.post(() -> {
            if (TextUtils.isEmpty(text)) {
                main.post(() -> tips.setNewData(Collections.emptyList()));
                return;
            }
            try {
                LBS.IPoint location = session.lastLocation();
                GeocodeSearch search = new GeocodeSearch(getApplication());
                RegeocodeQuery query = new RegeocodeQuery(
                        Point.form(location.getLongitude(), location.getLatitude())
                                .unBox(LatLonPoint.class), 200f, GeocodeSearch.AMAP);
                String city = search.getFromLocation(query).getCity();
                Logger.d(text + " " + city);
                InputtipsQuery inputTipsQuery = new InputtipsQuery(text, city);
                Inputtips inputTips = new Inputtips(getApplication(), inputTipsQuery);
                List<Tip> rawTips = inputTips.requestInputtips();
                List<TipText> tips = StreamSupport.stream(rawTips)
                        .filter(tip -> !TextUtils.isEmpty(tip.getPoiID()))
                        .map(x -> new TipText(x.getPoiID(), x.getName()))
                        .collect(Collectors.toList());
                main.post(() -> this.tips.setNewData(tips));
            } catch (Exception e) {
                e.printStackTrace();
                speechResult.post(null);
                main.post(() -> tips.setNewData(Collections.emptyList()));
            }
        });
    }

    @Override
    protected void onCleared() {
        asr.removeHandler(this);
        workerThread.quit();
        worker.removeCallbacksAndMessages(null);
        main.removeCallbacksAndMessages(null);
        session.close();
    }

    @Override
    public void onStatusUpdate(int status) {
        this.status.setValue(status);
    }

    @Override
    public void onVolumeUpdate(int value) {

    }

    @Override
    public void onWeakUp(@NonNull String text) {

    }

    @Override
    public void onResult(boolean isFinal, @NonNull String text) {
        if (isFinal) {
            speechResult.post(text);
        } else {
            speechPartText.setValue(text);
        }
    }
}
