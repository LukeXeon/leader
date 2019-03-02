package org.kexie.android.dng.navi.model;

import java.util.List;

/**
 * Created by Luke on 2018/12/27.
 */

public class Query
{
    public final Point from;
    public final Point to;
    public final List<Point> ways;
    public final int mode;

    private Query(Builder builder)
    {
        from = builder.from;
        to = builder.to;
        ways = builder.ways;
        mode = builder.mode;
    }

    public static final class Builder
    {
        private Point from;
        private Point to;
        private List<Point> ways;
        private int mode;

        public Builder()
        {
        }

        public Builder from(Point val)
        {
            from = val;
            return this;
        }

        public Builder to(Point val)
        {
            to = val;
            return this;
        }

        public Builder ways(List<Point> val)
        {
            ways = val;
            return this;
        }


        public Builder mode(int val)
        {
            mode = val;
            return this;
        }

        public Query build()
        {
            return new Query(this);
        }
    }
}
