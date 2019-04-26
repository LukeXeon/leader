package org.kexie.android.dng.player.subtitle.reader;

import android.text.TextUtils;

import com.blankj.utilcode.util.FileUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: 字幕处理工具
 * @author: zhangliangming
 * @date: 2019-01-12 15:57
 **/
public class SubtitleHelper {

    private final static Map<String, Class<? extends SubtitleFileReader>> sReaderTypes;

    static {
        sReaderTypes = new HashMap<>();
        sReaderTypes.put("ass", AssSubtitleFileReader.class);
        sReaderTypes.put("srt", SrtSubtitleFileReader.class);
    }

    /**
     * 获取支持的文件格式
     *
     * @return
     */
    public static Set<String> getSupportSubtitleExt() {
        return Collections.unmodifiableSet(sReaderTypes.keySet());
    }

    /**
     * 获取文件读取器
     *
     * @param file
     * @return
     */
    public static SubtitleFileReader getSubtitleFileReader(File file) {
        return getSubtitleFileReader(file.getName());
    }

    /**
     * 获取歌词文件读取器
     *
     * @param fileName
     * @return
     */
    public static SubtitleFileReader getSubtitleFileReader(String fileName) {
        String ext = FileUtils.getFileExtension(fileName);
        Class<? extends SubtitleFileReader> type = sReaderTypes.get(ext);
        SubtitleFileReader reader = null;
        if (type != null) {
            try {
                reader = type.newInstance();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return reader;
    }

    /**
     * 解析字幕文本
     *
     * @param subtitleLine
     * @return html格式对应的字幕文本
     */
    static String[] parseSubtitleText(String subtitleLine) {
        String[] result = {"", ""};
        String regex = "\\{[^{]+}";
        //去掉样式
        result[0] = subtitleLine.replaceAll(regex, "");
        //加载样式
        Pattern tempPattern = Pattern.compile(regex);
        Matcher tempMatcher = tempPattern.matcher(subtitleLine);
        if (tempMatcher.find()) {
            StringBuilder subtitleTextSB = new StringBuilder();
            String[] splitSubtitles = subtitleLine.split(regex, -1);
            int index = 0;

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(subtitleLine);
            //遍历样式字符串
            while (matcher.find()) {
                if (index == 0 && splitSubtitles.length > 0
                        && !TextUtils.isEmpty(splitSubtitles[0])) {
                    subtitleTextSB.append(splitSubtitles[0]);
                }
                String styleString = matcher.group();
                if (index + 1 >= splitSubtitles.length) {
                    break;
                }
                String splitSubtitle = splitSubtitles[index + 1];
                String subtitleText = getSubtitleText(styleString, splitSubtitle);
                subtitleTextSB.append(subtitleText);

                index++;
            }

            //如果没有样式
            if (index == 0 && splitSubtitles.length > 0
                    && !TextUtils.isEmpty(splitSubtitles[0])) {
                subtitleTextSB.append(splitSubtitles[0]);
            }
            //添加剩余的字幕内容
            for (index++; index < splitSubtitles.length; index++) {
                if (!TextUtils.isEmpty(splitSubtitles[index])) {
                    subtitleTextSB.append(splitSubtitles[index]);
                }
            }
            result[1] = subtitleTextSB.toString();
        } else {
            result[1] = subtitleLine;
        }
        return result;
    }

    /**
     * 获取字幕文本
     *
     * @param styleString   样式字符串
     * @param splitSubtitle 分隔后的字幕文本
     * @return
     */
    private static String getSubtitleText(String styleString, String splitSubtitle) {
        StringBuilder result = new StringBuilder();
        int start = styleString.indexOf("{");
        int end = styleString.lastIndexOf("}");
        styleString = styleString.substring(start + 1, end);
        styleString = styleString.replaceAll("\\\\", "\\$");
        if (styleString.contains("$")) {
            result.append("<font");
            String[] styles = styleString.split("\\$");
            StringBuilder splitSubtitleBuilder = new StringBuilder(splitSubtitle);
            for (String style1 : styles) {
                String style = style1;
                if (style.startsWith("fn")) {
                    String face = style.substring("fn".length()).trim();
                    result.append(" face=\"")
                            .append(face)
                            .append("\"");
                } else if (style.startsWith("fs")) {
                    String size = style.substring("fs".length()).trim();
                    result.append(" size=\"")
                            .append(size)
                            .append("\"");
                } else if (style.startsWith("b1")
                        || style.startsWith("i1")
                        || style.startsWith("u1")
                        || style.startsWith("s1")) {
                    //b<0/1>  粗体，i<0/1>
                    // 斜体，u<0/1> 下划线，
                    // s<0/1>  删除线（0=关闭，1=开启）

                    if (style.startsWith("b1")) {
                        splitSubtitleBuilder = new StringBuilder("<b>" + splitSubtitleBuilder + "</b>");
                    } else if (style.startsWith("i1")) {
                        splitSubtitleBuilder = new StringBuilder("<i>" + splitSubtitleBuilder + "</i>");
                    } else if (style.startsWith("u1")) {
                        splitSubtitleBuilder = new StringBuilder("<u>" + splitSubtitleBuilder + "</u>");
                    } else if (style.startsWith("s1")) {
                        splitSubtitleBuilder = new StringBuilder("<s>" + splitSubtitleBuilder + "</s>");
                    }

                } else if (style.startsWith("c&H") || style.startsWith("1c&H")) {
                    //c&H<bbggrr>&     改变主体颜色（同1c）
                    //1c&H<bbggrr>&   改变主体颜色
                    int endIndex = style.lastIndexOf("&");
                    style = style.substring(0, endIndex).trim();
                    String color = "";
                    if (style.startsWith("c&H")) {
                        color = convertRgbColor(style.substring("c&H".length()).trim());
                    } else {
                        color = convertRgbColor(style.substring("1c&H".length()).trim());
                    }
                    result.append(" color=\"#")
                            .append(color)
                            .append("\"");

                }
            }
            splitSubtitle = splitSubtitleBuilder.toString();
            result.append(">");
        }
        //修改成html标签
        if (result.length() > 0) {
            result.append(splitSubtitle);
            result.append("</font>");
        } else {
            result.append(splitSubtitle);
        }
        return result.toString();
    }

    private static String convertArgbColor(String abgrColorString) {
        if (abgrColorString.length() == 8) {
            return abgrColorString.substring(6, 8)
                    + abgrColorString.substring(4, 6)
                    + abgrColorString.substring(2, 4);
        }
        return abgrColorString.substring(4, 6)
                + abgrColorString.substring(2, 4)
                + abgrColorString.substring(0, 2);
    }

    /**
     * 获取rgb颜色字符串
     *
     * @param bgrColorString
     * @return
     */
    private static String convertRgbColor(String bgrColorString) {
        return convertArgbColor(bgrColorString);
    }

    /**
     * 获取bgr颜色字符串
     *
     * @param rgbColorString
     * @return
     */
    private static String convertBgrColor(String rgbColorString) {
        return convertRgbColor(rgbColorString);
    }

    /**
     * 获取abgr颜色字符串
     *
     * @param argbColorString
     * @return
     */
    private static String convertAbgrColor(String argbColorString) {
        return convertRgbColor(argbColorString);
    }

    /**
     * 根据当前播放进度获取当前行字幕内容
     *
     * @param subtitleLineInfos
     * @param curPlayingTime
     * @param playOffset
     * @return
     */
    private static int getLineNumber(List<SubtitleLineInfo> subtitleLineInfos,
                                     long curPlayingTime,
                                     long playOffset) {
        if (subtitleLineInfos != null && subtitleLineInfos.size() > 0) {
            //添加歌词增量
            long nowPlayingTime = curPlayingTime + playOffset;
            for (int i = 0; i < subtitleLineInfos.size(); i++) {
                SubtitleLineInfo subtitleLineInfo = subtitleLineInfos.get(i);
                int lineStartTime = subtitleLineInfo.getStartTime();
                int lineEndTime = subtitleLineInfo.getEndTime();
                if (nowPlayingTime < lineStartTime) {
                    return -1;
                } else if (nowPlayingTime >= lineStartTime && nowPlayingTime <= lineEndTime) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 解析字幕时间
     *
     * @param timeString 00:00:00,000
     * @return
     */
    static int parseSubtitleTime(String timeString) {
        timeString = timeString.replace(",", ":");
        timeString = timeString.replace(".", ":");
        String timedata[] = timeString.split(":");
        int second = 1000;
        int minute = 60 * second;
        int hour = 60 * minute;
        int msec = 0;
        if (timedata[3].length() == 2) {
            msec = Integer.parseInt(timedata[3]) * 10;
        } else {
            msec = Integer.parseInt(timedata[3]);
        }
        return Integer.parseInt(timedata[0]) * hour
                + Integer.parseInt(timedata[1]) * minute
                + Integer.parseInt(timedata[2]) * second + msec;
    }

    /**
     * 毫秒转时间字符串
     *
     * @param msecTotal
     * @return 00:00:00,000
     */
    private static String parseHHMMSSFFFString(int msecTotal) {
        int msec = msecTotal % 1000;
        msecTotal /= 1000;
        int minute = msecTotal / 60;
        int hour = minute / 60;
        int second = msecTotal % 60;
        minute %= 60;
        return String.format(Locale.CHINA,
                "%02d:%02d:%02d,%03d",
                hour, minute, second, msec);
    }

    /**
     * 毫秒转时间字符串
     *
     * @param msecTotal
     * @return 00:00:00.00
     */
    private static String parseHHMMSSFFString(int msecTotal) {
        int msec = msecTotal % 1000;
        msecTotal /= 1000;
        int minute = msecTotal / 60;
        int hour = minute / 60;
        int second = msecTotal % 60;
        minute %= 60;
        return String.format(Locale.CHINA,
                "%02d:%02d:%02d.%02d",
                hour, minute, second, msec / 10);
    }

    /**
     * 毫秒转时间字符串
     *
     * @param msecTotal
     * @return 00:00:00
     */
    private static String parseHHMMSSString(int msecTotal) {
        msecTotal /= 1000;
        int minute = msecTotal / 60;
        int hour = minute / 60;
        int second = msecTotal % 60;
        minute %= 60;
        return String.format(Locale.CHINA,
                "%02d:%02d:%02d",
                hour, minute, second);
    }
}