package org.kexie.android.dng.player.widget;

/**
 * @Description: 文件处理类
 * @author: zhangliangming
 * @date: 2019-01-13 16:19
 **/
public class FileUtils {

    public static String getFileExt(String fileName) {
        int pos = fileName.lastIndexOf(".");
        if (pos == -1)
            return "";
        return fileName.substring(pos + 1).toLowerCase();
    }
}
