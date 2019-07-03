package org.kexie.android.dng.media.model;

import android.content.Context;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Interceptor;
import com.alibaba.android.arouter.facade.callback.InterceptorCallback;
import com.alibaba.android.arouter.facade.template.IInterceptor;
import com.alibaba.android.arouter.launcher.ARouter;

import org.kexie.android.dng.common.contract.Module;

@Interceptor(priority = 1)
public class SplitFlow implements IInterceptor {
    @Override
    public void process(Postcard postcard, InterceptorCallback callback) {
        if (Module.Media.video.equals(postcard.getPath())
                || Module.Media.photo.equals(postcard.getPath())) {
            postcard = ARouter.getInstance()
                    .build(browser)
                    .withString("target", postcard.getPath());
        }
        callback.onContinue(postcard);
    }

    /**
     * 内部逻辑
     */
    public static final String browser = "/media/inner-use/browse";

    @Override
    public void init(Context context) {
    }
}
