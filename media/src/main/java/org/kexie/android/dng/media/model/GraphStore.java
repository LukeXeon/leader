package org.kexie.android.dng.media.model;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import org.kexie.android.dng.media.model.beans.Graph;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class GraphStore {
    private static final AtomicBoolean sInit = new AtomicBoolean(false);
    private static final String _ID = MediaStore.Files.FileColumns._ID;
    private static final String DATA = MediaStore.Files.FileColumns.DATA;
    private static final String DISPLAY_NAME = MediaStore.Files.FileColumns.DISPLAY_NAME;
    private static final String MIME_TYPE = MediaStore.Files.FileColumns.MIME_TYPE;
    private static final String BUCKET_DISPLAY_NAME = "bucket_display_name";
    private static final String[] COLUMNS_NAME = {
            _ID,
            DATA,
            DISPLAY_NAME,
            MIME_TYPE, BUCKET_DISPLAY_NAME
    };

    private static GraphStore instance;

    public static GraphStore getInstance(Context context) {
        if (sInit.compareAndSet(false, true)) {
            instance = new GraphStore(context);
        }
        return instance;
    }

    private final Application application;

    private GraphStore(Context context) {
        application = (Application) context.getApplicationContext();
    }

    private Cursor getCursor() {
        String[] mimeTypes = MimeType.values();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mimeTypes.length; i++) {
            sb.append("mime_type=?");
            if (i + 1 < mimeTypes.length) {
                sb.append(" or ");
            }
        }
        String selection = sb.toString();
        Uri uri = MediaStore.Files.getContentUri("external");
        return application.getContentResolver()
                .query(uri, COLUMNS_NAME, selection, mimeTypes,
                        MediaStore.Files.FileColumns.DATE_ADDED + " DESC");
    }

    public List<Graph.Album> loadAlbums() {
        Cursor cursor = getCursor();
        TreeMap<String, List<Graph>> albums = new TreeMap<>();
        List<Graph> all = new LinkedList<>();
        if (cursor != null) {
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    String _id = cursor.getString(cursor
                            .getColumnIndex(_ID));
                    String _data = cursor.getString(cursor
                            .getColumnIndex(DATA));
                    String _display_name = cursor.getString(cursor
                            .getColumnIndex(DISPLAY_NAME));
                    String mime_type = cursor.getString(cursor
                            .getColumnIndex(MIME_TYPE));
                    String bucket_display_name = cursor.getString(cursor
                            .getColumnIndex(BUCKET_DISPLAY_NAME));
                    Graph resource = new Graph(
                            _id,
                            _data,
                            _display_name,
                            mime_type,
                            bucket_display_name);
                    List<Graph> graphs = albums.get(bucket_display_name);
                    if (graphs == null) {
                        graphs = new LinkedList<>();
                        albums.put(bucket_display_name, graphs);
                    }
                    graphs.add(resource);
                    all.add(resource);
                } while (cursor.moveToNext());
            }
            cursor.close();
            if (!albums.isEmpty()) {
                Graph.Album scopeAll = new Graph.Album("全部", all);
                List<Graph.Album> result = new LinkedList<>();
                result.add(scopeAll);
                for (Map.Entry<String, List<Graph>> entry : albums.entrySet()) {
                    result.add(new Graph.Album(entry.getKey(),
                            entry.getValue()));
                }
                return result;
            }
        }
        return Collections.emptyList();
    }
}
