package org.kexie.android.dng.navi.viewmodel.entity;

public class LiteRouteInfo
{
    public final String name;
    public final String time;
    public final String length;

    private LiteRouteInfo(Builder builder)
    {
        name = builder.name;
        time = builder.time;
        length = builder.length;
    }

    public static final class Builder
    {
        private String name;
        private String time;
        private String length;

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

        public LiteRouteInfo build()
        {
            return new LiteRouteInfo(this);
        }
    }
}
