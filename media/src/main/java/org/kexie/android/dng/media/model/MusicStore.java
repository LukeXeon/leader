package org.kexie.android.dng.media.model;

import android.app.Application;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

import com.bumptech.glide.Glide;

import org.kexie.android.dng.media.model.beans.Music;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;

public final class MusicStore {

    private static final AtomicBoolean sInit = new AtomicBoolean(false);

    private static MusicStore instance;

    public static MusicStore getInstance(Context context) {
        if (sInit.compareAndSet(false, true)) {
            instance = new MusicStore(context);
        }
        return instance;
    }

    private final Application context;

    private WeakReference<List<Music>> cache;

    private MusicStore(@NonNull Context context) {
        this.context = (Application) context.getApplicationContext();
    }

    @SuppressWarnings("unchecked")
    public List<Music> loadList() {
        if (cache != null) {
            List<Music> musics = cache.get();
            if (musics != null) {
                return musics;
            }
        }
        List<Music> list = new ArrayList<>();
        List<Object[]> futures = new ArrayList<>();
        Cursor cursor = context.getContentResolver()
                .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        , null, null, null,
                        MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return Collections.emptyList();
        }
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
            String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            //int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Resource.DURATION));
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            if (size > 1024 * 800) {
                Future<Drawable> future = requestDrawable(id, albumId);
                futures.add(new Object[]{future, name, singer, path});
            }
        }
        cursor.close();
        for (int i = 0; i < futures.size(); i++) {
            Object[] objects = futures.get(i);
            Future<Drawable> future = (Future<Drawable>) objects[0];
            Drawable drawable = null;
            try {
                drawable = future.get();
            } catch (Exception ignored) {
            }
            list.add(new Music(drawable, (String) objects[1], (String) objects[2], (String) objects[3]));
        }
        cache = new WeakReference<>(list);
        return list;
    }

    private Future<Drawable>
    requestDrawable(long songId,
                    long albumId) {
        if (songId < 0 && albumId < 0) {
            return null;
        }
        try {
            if (albumId < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songId + "/albumart");
                return Glide.with(context)
                        .load(uri)
                        .submit();
            } else {
                Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
                Uri uri = ContentUris.withAppendedId(albumArtUri, albumId);
                return Glide.with(context)
                        .load(uri)
                        .submit();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
