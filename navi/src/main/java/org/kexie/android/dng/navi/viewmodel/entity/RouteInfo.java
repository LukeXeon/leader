package org.kexie.android.dng.navi.viewmodel.entity;

import com.amap.api.navi.model.AMapNaviPath;

import java.util.List;

public class RouteInfo
{
    public final String name;
    public final String time;
    public final String length;
    public final AMapNaviPath path;
    public final List<GuideInfo> guideInfos;

    private RouteInfo(Builder builder)
    {
        name = builder.name;
        time = builder.time;
        length = builder.length;
        path = builder.path;
        guideInfos = builder.guideInfos;
    }

    public static final class Builder
    {
        private String name;
        private String time;
        private String length;
        private AMapNaviPath path;
        private List<GuideInfo> guideInfos;

        public Builder()
        {
        }

        public Builder name(String val)
        {
            name = val;
            return this;
        }

        public Builder time(String val)
        {
            time = val;
            return this;
        }

        public Builder length(String val)
        {
            length = val;
            return this;
        }

        public Builder path(AMapNaviPath val)
        {
            path = val;
            return this;
        }

        public Builder guideInfos(List<GuideInfo> val)
        {
            guideInfos = val;
            return this;
        }

        public RouteInfo build()
        {
            return new RouteInfo(this);
        }
    }
}
