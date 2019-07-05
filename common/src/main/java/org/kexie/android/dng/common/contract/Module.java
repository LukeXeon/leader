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
        public static final String location = "/navi/location";
    }

    public static final class Media {
        public static final String music = "/media/music";
        public static final String gallery = "/media/gallery";
    }

    public static final class Host {
        public final static String host = "/host/host";
    }

    public static final class Ai {
        public final static String nlp = "/ai/nlp";
        public final static String tts = "/ai/tts";
        public final static String asr = "/ai/asr";

        public final static String siri = "/ai/siri";
        public final static String speaker = "/ai/speaker";
    }
}
