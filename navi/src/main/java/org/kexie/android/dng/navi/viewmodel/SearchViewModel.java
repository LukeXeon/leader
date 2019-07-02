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
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.common.contract.LBS;
import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.common.widget.GenericQuickAdapter;
import org.kexie.android.dng.navi.BR;
import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.model.beans.Point;
import org.kexie.android.dng.navi.viewmodel.beans.TipText;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class SearchViewModel extends AndroidViewModel {

    public final GenericQuickAdapter<TipText> tips = new GenericQuickAdapter<>(R.layout.item_tip, BR.tip);

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
                List<TipText> tips = StreamSupport.stream(inputTips.requestInputtips())
                        .filter(tip -> !TextUtils.isEmpty(tip.getPoiID()))
                        .map(x -> new TipText(x.getPoiID(), x.getName()))
                        .collect(Collectors.toList());
                main.post(() -> this.tips.setNewData(tips));
            } catch (Exception e) {
                e.printStackTrace();
                main.post(() -> tips.setNewData(Collections.emptyList()));
            }
        });
    }

    @Override
    protected void onCleared() {
        workerThread.quit();
        worker.removeCallbacksAndMessages(null);
        main.removeCallbacksAndMessages(null);
        session.close();
    }
}
