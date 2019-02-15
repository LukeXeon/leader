package org.kexie.android.dng.navi.viewmodel.entity;

import java.util.List;

public class LiteRoute
{
    public final String name;
    public final String time;
    public final String length;
    public final List<LiteStep> steps;

    private LiteRoute(Builder builder)
    {
        name = builder.name;
        time = builder.time;
        length = builder.length;
        steps = builder.steps;
    }

    public static final class Builder
    {
        private String name;
        private String time;
        private String length;
        private List<LiteStep> steps;

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

        public Builder steps(List<LiteStep> val)
        {
            steps = val;
            return this;
        }

        public LiteRoute build()
        {
            return new LiteRoute(this);
        }
    }
}
