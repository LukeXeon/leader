package org.kexie.android.dng.navi.viewmodel.beans;

import com.amap.api.navi.model.AMapNaviPath;

import java.util.List;

public class PathDescription {
    public final int id;
    public final String name;
    public final String time;
    public final String length;
    public final AMapNaviPath path;
    public final List<StationDescription> stationDescriptions;

    private PathDescription(Builder builder) {
        id = builder.id;
        name = builder.name;
        time = builder.time;
        length = builder.length;
        path = builder.path;
        stationDescriptions = builder.stationDescriptions;
    }

    public static final class Builder {
        private String name;
        private String time;
        private String length;
        private AMapNaviPath path;
        private List<StationDescription> stationDescriptions;
        private int id;

        public Builder() {
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder time(String val) {
            time = val;
            return this;
        }

        public Builder length(String val) {
            length = val;
            return this;
        }

        public Builder path(AMapNaviPath val) {
            path = val;
            return this;
        }

        public Builder stationDescriptions(List<StationDescription> val) {
            stationDescriptions = val;
            return this;
        }

        public PathDescription build() {
            return new PathDescription(this);
        }

        public Builder id(int val) {
            id = val;
            return this;
        }
    }
}
