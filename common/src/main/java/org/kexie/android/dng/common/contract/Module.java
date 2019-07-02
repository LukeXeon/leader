package org.kexie.android.dng.common.contract;

/**
 * 模块路径
 */
public final class Module {
    public static final class Ux {
        /**
         * 登录界面
         */
        public static final String login = "/ux/login";
        /**
         * 收音机
         */
        public static final String fm = "/ux/fm";
        /**
         * 时间
         */
        public static final String time = "/ux/time";
        /**
         * 设置界面
         */
        public static final String setting = "/ux/setting";
        /**
         * APP商店
         */
        public static final String appStore = "/ux/store";
        /**
         * 用户个人信息
         */
        public static final String userInfo = "/ux/info";
        /**
         * 默认桌面
         */
        public static final String desktop = "/ux/desktop";
        /**
         * 天气
         */
        public static final String weather = "/ux/weather";
        /**
         * 应用列表
         */
        public static final String apps = "/ux/apps";
    }

    /**
     * 导航模块
     */
    public static final class Navi {
        /**
         * 导航主界面
         */
        public static final String navigator = "/navi/navigator";
        public static final String search = "/navi/search";
        public static final String location = "navi/location";
    }

    public static final class Media {
        /**
         * 图片浏览
         */
        public static final String photoViewer = "/media/photo/viewer";
        /**
         * 视频播放
         */
        public static final String videoPlayer = "/media/video/player";
        /**
         * 音乐播放
         */
        public static final String music = "/media/music";
        /**
         * 重定向到{@link Media#_$browser}
         */
        public static final String photo = "/media/photo/browse";
        public static final String video = "/media/video/browse";
        /**
         * 内部逻辑
         */
        public static final String _$browser = "/media/inner-use/browse";
    }

    public static final class Host {
        public final static String host = "/host/host";
    }

    public static final class Ai {

        public final static String nlp = "/ai/nlp";
        public final static String tts = "/ai/tts";
        public final static String asr = "/ai/asr";

        public final static String siri = "/ai/siri";
    }
}
