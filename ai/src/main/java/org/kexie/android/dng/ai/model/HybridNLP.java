package org.kexie.android.dng.ai.model;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.orhanobut.logger.Logger;

import org.kexie.android.dng.ai.R;
import org.kexie.android.dng.ai.widget.CookieCache;
import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.common.contract.NLP;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

@Route(path = Module.Ai.nlp)
public class HybridNLP implements NLP {

    private static final String[] navigationPrefix = {
            "打开","启动","开启"
    };
    private static final Navigation[] navigationTargets = {
            item(Module.Navi.navigator, "导航", "地图"),
            item(Module.Ux.time, "时间", "闹钟"),
            item(Module.Ux.fm, "收音机"),
            item(Module.Media.gallery, "影库", "相册", "图库", "视频", "多媒体"),
            item(Module.Ux.apps, "APP", "应用"),
            item(Module.Media.music, "音乐", "歌曲"),
            item(Module.Ux.userInfo, "个人中心", "信息"),
            item(Module.Ux.weather, "天气"),
            item(Module.Ux.appStore, "商店"),
            item(Module.Ux.setting, "设置")
    };

    private static final String TYPE_TEXT = "text";

    private TuringApi turingApi;
    private Context context;

    @Override
    public void init(Context context) {
        this.context = context.getApplicationContext();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(this.context.getString(R.string.ai_open_api_url))
                .client(new OkHttpClient.Builder()
                        .callTimeout(3, TimeUnit.SECONDS)
                        .cookieJar(new CookieCache())
                        .build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        turingApi = retrofit.create(TuringApi.class);
    }

    @Nullable
    @Override
    public Object process(String text) {
        Object result = processByLocal(text);
        if (result != null) {
            return result;
        }
        return processByNetwork(text);
    }

    private Object processByLocal(String text) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        for (String prefix : navigationPrefix) {
            int index = text.indexOf(prefix);
            if (index != -1) {
                text = text.substring(index + prefix.length());
                Logger.d(text);
                for (Navigation navigation : navigationTargets) {
                    for (String key : navigation.keywords) {
                        if (text.contains(key)) {
                            return navigation;
                        }
                    }
                }
            }
        }
        return null;
    }

    private Object processByNetwork(String text) {
        TuringRequest request = new TuringRequest();
        request.reqType = 0;
        request.perception = new TuringRequest.Perception();
        request.perception.inputText = new TuringRequest.Perception.InputText();
        request.perception.inputText.text = text;
        request.userInfo = new TuringRequest.UserInfo();
        request.userInfo.apiKey = context.getString(R.string.ai_api_key);
        request.userInfo.userId = context.getString(R.string.ai_user_id);
        Call<TuringResponse> call = turingApi.request(request);
        try {
            Response<TuringResponse> response = call.execute();
            TuringResponse result = response.body();
            if (result != null && result.results != null && result.results.size() > 0) {
                TuringResponse.Result r = result.results.get(0);
                String text1 = r.values.text;
                if (TYPE_TEXT.equals(r.resultType)
                        && !TextUtils.isEmpty(text1)) {
                    return text1;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Navigation item(String path, String... key) {
        return new Navigation(key, path);
    }

    private static final class Navigation
            implements Behavior {

        final String[] keywords;

        final String path;

        private Navigation(String[] keywords, String path) {
            this.keywords = keywords;
            this.path = path;
        }

        @Override
        public void done(Object input) {
            Fragment fragment = (Fragment) input;
            int id = fragment.getId();
            fragment.requireFragmentManager()
                    .popBackStackImmediate();
            Fragment target = (Fragment) ARouter.getInstance()
                    .build(path)
                    .navigation();
            fragment.requireFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .add(id, target)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commitAllowingStateLoss();

        }
    }

    private interface TuringApi {
        @POST("/openapi/api/v2")
        Call<TuringResponse> request(@Body TuringRequest request);
    }

    private static class TuringRequest {
        /**
         * reqType : 0
         * perception : {"inputText":{"text":"附近的酒店"},"inputImage":{"url":"imageUrl"},"selfInfo":{"location":{"city":"北京","province":"北京","street":"信息路"}}}
         * userInfo : {"apiKey":"","userId":""}
         */

        private int reqType;
        private Perception perception;
        private UserInfo userInfo;

        public int getReqType() {
            return reqType;
        }

        public void setReqType(int reqType) {
            this.reqType = reqType;
        }

        public Perception getPerception() {
            return perception;
        }

        public void setPerception(Perception perception) {
            this.perception = perception;
        }

        public UserInfo getUserInfo() {
            return userInfo;
        }

        public void setUserInfo(UserInfo userInfo) {
            this.userInfo = userInfo;
        }

        public static class Perception {
            /**
             * inputText : {"text":"附近的酒店"}
             * inputImage : {"url":"imageUrl"}
             * selfInfo : {"location":{"city":"北京","province":"北京","street":"信息路"}}
             */

            private InputText inputText;
            private InputImage inputImage;
            private SelfInfo selfInfo;

            public InputText getInputText() {
                return inputText;
            }

            public void setInputText(InputText inputText) {
                this.inputText = inputText;
            }

            public InputImage getInputImage() {
                return inputImage;
            }

            public void setInputImage(InputImage inputImage) {
                this.inputImage = inputImage;
            }

            public SelfInfo getSelfInfo() {
                return selfInfo;
            }

            public void setSelfInfo(SelfInfo selfInfo) {
                this.selfInfo = selfInfo;
            }

            public static class InputText {
                /**
                 * text : 附近的酒店
                 */

                private String text;

                public String getText() {
                    return text;
                }

                public void setText(String text) {
                    this.text = text;
                }
            }

            public static class InputImage {
                /**
                 * url : imageUrl
                 */

                private String url;

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }
            }

            public static class SelfInfo {
                /**
                 * location : {"city":"北京","province":"北京","street":"信息路"}
                 */

                private Location location;

                public Location getLocation() {
                    return location;
                }

                public void setLocation(Location location) {
                    this.location = location;
                }

                public static class Location {
                    /**
                     * city : 北京
                     * province : 北京
                     * street : 信息路
                     */

                    private String city;
                    private String province;
                    private String street;

                    public String getCity() {
                        return city;
                    }

                    public void setCity(String city) {
                        this.city = city;
                    }

                    public String getProvince() {
                        return province;
                    }

                    public void setProvince(String province) {
                        this.province = province;
                    }

                    public String getStreet() {
                        return street;
                    }

                    public void setStreet(String street) {
                        this.street = street;
                    }
                }
            }
        }

        public static class UserInfo {
            /**
             * apiKey :
             * userId :
             */

            private String apiKey;
            private String userId;

            public String getApiKey() {
                return apiKey;
            }

            public void setApiKey(String apiKey) {
                this.apiKey = apiKey;
            }

            public String getUserId() {
                return userId;
            }

            public void setUserId(String userId) {
                this.userId = userId;
            }
        }
    }

    private static class TuringResponse {
        /**
         * intent : {"code":10005,"intentName":"","actionName":"","parameters":{"nearby_place":"酒店"}}
         * results : [{"groupType":1,"resultType":"url","values":{"url":"http://m.elong.com/hotel/0101/nlist/#indate=2016-12-10&outdate=2016-12-11&keywords=%E4%BF%A1%E6%81%AF%E8%B7%AF"}},{"groupType":1,"resultType":"text","values":{"text":"亲，已帮你找到相关酒店信息"}}]
         */

        private Intent intent;
        private List<Result> results;

        public Intent getIntent() {
            return intent;
        }

        public void setIntent(Intent intent) {
            this.intent = intent;
        }

        public List<Result> getResults() {
            return results;
        }

        public void setResults(List<Result> results) {
            this.results = results;
        }

        public static class Intent {
            /**
             * code : 10005
             * intentName :
             * actionName :
             * parameters : {"nearby_place":"酒店"}
             */

            private int code;
            private String intentName;
            private String actionName;
            private Parameters parameters;

            public int getCode() {
                return code;
            }

            public void setCode(int code) {
                this.code = code;
            }

            public String getIntentName() {
                return intentName;
            }

            public void setIntentName(String intentName) {
                this.intentName = intentName;
            }

            public String getActionName() {
                return actionName;
            }

            public void setActionName(String actionName) {
                this.actionName = actionName;
            }

            public Parameters getParameters() {
                return parameters;
            }

            public void setParameters(Parameters parameters) {
                this.parameters = parameters;
            }

            public static class Parameters {
                /**
                 * nearby_place : 酒店
                 */

                private String nearby_place;

                public String getNearby_place() {
                    return nearby_place;
                }

                public void setNearby_place(String nearby_place) {
                    this.nearby_place = nearby_place;
                }
            }
        }

        public static class Result {
            /**
             * groupType : 1
             * resultType : url
             * values : {"url":"http://m.elong.com/hotel/0101/nlist/#indate=2016-12-10&outdate=2016-12-11&keywords=%E4%BF%A1%E6%81%AF%E8%B7%AF"}
             */

            private int groupType;
            private String resultType;
            private Values values;

            public int getGroupType() {
                return groupType;
            }

            public void setGroupType(int groupType) {
                this.groupType = groupType;
            }

            public String getResultType() {
                return resultType;
            }

            public void setResultType(String resultType) {
                this.resultType = resultType;
            }

            public Values getValues() {
                return values;
            }

            public void setValues(Values values) {
                this.values = values;
            }

            public static class Values {
                /**
                 * url : http://m.elong.com/hotel/0101/nlist/#indate=2016-12-10&outdate=2016-12-11&keywords=%E4%BF%A1%E6%81%AF%E8%B7%AF
                 */

                private String url;

                private String text;

                public void setText(String text) {
                    this.text = text;
                }

                public String getText() {
                    return text;
                }

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }
            }
        }
    }
}