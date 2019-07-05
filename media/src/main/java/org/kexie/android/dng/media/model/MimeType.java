package org.kexie.android.dng.media.model;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class MimeType {

    // IMAGE
    public final static String JPG = "image/jpeg";
    public final static String JPEG = "image/jpeg";
    public final static String GIF = "image/gif";
    public final static String PNG = "image/png";
    public final static String BMP = "image/x-ms-bmp";
    public final static String WBMP = "image/vnd.wap.wbmp";
    public final static String DNG = "image/x-adobe-dng";
    public final static String CR2 = "image/x-canon-cr2";
    public final static String NEF = "image/x-nikon-nef";
    public final static String NRW = "image/x-nikon-nrw";
    public final static String ARW = "image/x-sony-arw";
    public final static String RW2 = "image/x-panasonic-rw2";
    public final static String ORF = "image/x-olympus-orf";
    public final static String RAF = "image/x-fuji-raf";
    public final static String PEF = "image/x-pentax-pef";
    public final static String SRW = "image/x-samsung-srw";

    // VIDEO
    public final static String MPEG = "video/mpeg";
    public final static String MPG = "video/mpeg";
    public final static String MP4 = "video/mp4";
    public final static String M4V = "video/mp4";
    public final static String MOV = "video/quicktime";
    public final static String THREEGP = "video/3gpp";
    public final static String THREEGPP = "video/3gpp";
    public final static String THREEG2 = "video/3gpp2";
    public final static String THREEGPP2 = "video/3gpp2";
    public final static String MKV = "video/x-matroska";
    public final static String WEBM = "video/webm";
    public final static String TS = "video/mp2ts";
    public final static String AVI = "video/avi";
    public final static String WMV = "video/x-ms-wmv";
    public final static String ASF = "video/x-ms-asf";

    private MimeType() {
        throw new AssertionError();
    }

    public static String[] values() {
        try {
            Field[] fields = MimeType.class.getDeclaredFields();
            List<String> list = new ArrayList<>(fields.length);
            for (Field field : fields) {
                int m = field.getModifiers();
                if (Modifier.isStatic(m) && Modifier.isPublic(m)
                        && String.class.equals(field.getType())) {
                    list.add((String) field.get(null));
                }
            }
            return list.toArray(new String[0]);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
