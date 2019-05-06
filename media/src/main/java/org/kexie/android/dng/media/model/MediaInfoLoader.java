package org.kexie.android.dng.media.model;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

import com.bumptech.glide.Glide;

import org.kexie.android.dng.media.model.entity.MusicInfo;
import org.kexie.android.dng.media.model.entity.PhotoInfo;
import org.kexie.android.dng.media.model.entity.VideoInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

public class MediaInfoLoader {
    public static List<PhotoInfo> getPhotoInfos(Context context) {
        List<PhotoInfo> list = new ArrayList<>();
        String[] projection = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE
        };
        //全部图片
        String where = MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=?";
        //指定格式
        String[] whereArgs = {"image/jpeg", "image/png", "image/jpg"};
        //查询

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, where, whereArgs,
                MediaStore.Images.Media.DATE_MODIFIED + " desc ");
        if (cursor == null) {
            return Collections.emptyList();
        }
        //遍历
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            //获取图片的名称
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE)); // 大小
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            if (size < 5 * 1024 * 1024) {
                //<5M
                PhotoInfo materialBean = new PhotoInfo(title, path);
                list.add(materialBean);
            }
        }
        cursor.close();
        return list;
    }

    public static List<VideoInfo> getVideoInfos(Context context) {
        String[] projection = {
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE
        };
        //全部图片
        String where = MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Video.Media.MIME_TYPE + "=? or "
                + MediaStore.Video.Media.MIME_TYPE + "=? or "
                + MediaStore.Video.Media.MIME_TYPE + "=? or "
                + MediaStore.Video.Media.MIME_TYPE + "=? or "
                + MediaStore.Video.Media.MIME_TYPE + "=? or "
                + MediaStore.Video.Media.MIME_TYPE + "=? or "
                + MediaStore.Video.Media.MIME_TYPE + "=? or "
                + MediaStore.Video.Media.MIME_TYPE + "=?";
        String[] whereArgs = {"video/mp4", "video/3gp",
                "video/aiv", "video/rmvb", "video/vob", "video/flv",
                "video/mkv", "video/mov", "video/mpg"};
        List<VideoInfo> list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, where, whereArgs, MediaStore.Video.Media.DATE_ADDED + " DESC ");
        if (cursor == null) {
            return list;
        }
        try {
            while (cursor.moveToNext()) {
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)); // 大小
                if (size < 600 * 1024 * 1024) {//<600M
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)); // 路径
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                    VideoInfo mediaModel = new VideoInfo(title, path);
                    list.add(mediaModel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return list;
    }

    public static List<MusicInfo> getMusicInfos(Context context) {
        List<MusicInfo> list = new ArrayList<>();
        List<Future<Drawable>> futures = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                , null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return Collections.emptyList();
        }
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
            String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            //int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            if (size > 1024 * 800) {
                Future<Drawable> future = requestDrawable(context, id, albumId);
                MusicInfo song = new MusicInfo(name, path, singer);
                list.add(song);
                futures.add(future);
            }
        }
        cursor.close();
        for (int i = 0; i < futures.size(); i++) {
            Future<Drawable> future = futures.get(i);
            try {
                list.get(i).drawable = future.get();
            } catch (Exception ignored) {

            }
        }
        return list;
    }

    private static Future<Drawable>
    requestDrawable(Context context,
                    long songId,
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
