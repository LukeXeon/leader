package org.kexie.android.dng.navi.viewmodel;

import android.os.Bundle;

import com.orhanobut.logger.Logger;

import androidx.lifecycle.ViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;
import mapper.Request;

public class RouteViewModel extends ViewModel
{
    private int holdId;

    private final PublishSubject<Request> onJump = PublishSubject.create();

    public void init(Bundle bundle)
    {
        holdId = bundle.getInt("pathId");
    }

    public void jumpToDetails()
    {
        Bundle bundle = new Bundle();
        bundle.putInt("pathId", holdId);
        Request request = new Request.Builder()
                .uri("dng/navi/details")
                .bundle(bundle)
                .build();
        onJump.onNext(request);
    }

    public Observable<Request> getOnJump()
    {
        return onJump.observeOn(AndroidSchedulers.mainThread())
                .doOnNext((x)->Logger.d(this+""));
    }

    public void jumpToNavi()
    {
        Bundle bundle = new Bundle();
        bundle.putInt("pathId",holdId);
        Request request = new Request.Builder()
                .uri("dng/navi/navi")
                .bundle(bundle)
                .build();
        onJump.onNext(request);
    }
}
