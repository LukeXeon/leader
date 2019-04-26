package org.kexie.android.dng.player.subtitle.reader;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: srt字幕读取器
 * @author: zhangliangming
 * @date: 2019-01-12 19:35
 **/
public class SrtSubtitleFileReader extends SubtitleFileReader {
    public SrtSubtitleFileReader() {
        setDefaultCharset(Charset.forName("UTF-16"));
    }

    @Override
    public SubtitleInfo readInputStream(InputStream in) throws Exception {
        if (in != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in,
                    getDefaultCharset()));
            StringBuilder fileContentSB = new StringBuilder();
            String lineInfo = "";
            while ((lineInfo = br.readLine()) != null) {
                fileContentSB.append(lineInfo).append("\n");
            }
            in.close();
            br.close();
            return readText(fileContentSB.toString(), null);
        }
        return null;
    }

    @Override
    public SubtitleInfo readText(String fileContentString, File saveFile) throws Exception {
        saveFile(saveFile, fileContentString);

        SubtitleInfo subtitleInfo = new SubtitleInfo();
        subtitleInfo.setDefaultCharset(getDefaultCharset());
        subtitleInfo.setExt(getSupportFileExt());

        List<SubtitleLineInfo> subtitleLineInfos = new ArrayList<SubtitleLineInfo>();

        String[] fileContents = fileContentString.split("\n\n");
        for (String subtitleLineString : fileContents) {
            parseSubtitleInfo(subtitleLineString, subtitleLineInfos);
        }

        //设置字幕
        if (subtitleLineInfos.size() > 0) {
            subtitleInfo.setSubtitleLineInfos(subtitleLineInfos);
        }

        return subtitleInfo;
    }

    /**
     * 解析字幕内容
     *
     * @param subtitleLineString 字幕行内容
     * @param subtitleLineInfos  字幕内容
     * @author: zhangliangming
     * @date: 2019-01-12 21:15
     */
    private void parseSubtitleInfo(String subtitleLineString, List<SubtitleLineInfo> subtitleLineInfos) {
        String[] subtitleLines = subtitleLineString.split("\n");
        if (subtitleLines.length >= 3) {
            SubtitleLineInfo subtitleLineInfo = new SubtitleLineInfo();
            String timeString = subtitleLines[1];
            boolean flag = parseSubtitleTime(timeString, subtitleLineInfo);
            if (!flag) return;

            //加载字幕
            StringBuilder subtitleHtmlString = new StringBuilder();
            StringBuilder subtitleTextString = new StringBuilder();
            for (int i = 2; i < subtitleLines.length; i++) {
                String[] result = SubtitleHelper.parseSubtitleText(subtitleLines[i]);
                subtitleTextString.append(result[0]);
                subtitleHtmlString.append(result[1]);
                if (i != subtitleLines.length - 1) {
                    subtitleTextString.append("\n");
                    subtitleHtmlString.append("<br>");
                }
            }
            subtitleLineInfo.setSubtitleText(subtitleTextString.toString());
            subtitleLineInfo.setSubtitleHtml(subtitleHtmlString.toString());
            subtitleLineInfos.add(subtitleLineInfo);
        }
    }

    /**
     * 解析字幕时间
     *
     * @param timeString
     * @param subtitleLineInfo
     */
    private boolean parseSubtitleTime(String timeString, SubtitleLineInfo subtitleLineInfo) {
        String regex = "\\d+:\\d+:\\d+,\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(timeString);
        if (matcher.find()) {
            int startTime = SubtitleHelper.parseSubtitleTime(matcher.group());
            subtitleLineInfo.setStartTime(startTime);
            if (matcher.find()) {
                int endTime = SubtitleHelper.parseSubtitleTime(matcher.group());
                subtitleLineInfo.setEndTime(endTime);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase(getSupportFileExt());
    }

    @Override
    public String getSupportFileExt() {
        return "srt";
    }
}
