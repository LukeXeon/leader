package org.kexie.android.dng.ux.widget;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class CookieCache implements okhttp3.CookieJar {

    private ArrayMap<HttpUrl, List<Cookie>> cache = new ArrayMap<>();

    @Override
    public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
        cache.put(url, cookies);
    }

    @NonNull
    @Override
    public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
        List<Cookie> cookies = cache.get(url);
        return cookies == null ? Collections.emptyList() : cookies;
    }
}
