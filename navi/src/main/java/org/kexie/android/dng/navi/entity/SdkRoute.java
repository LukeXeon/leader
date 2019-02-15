package org.kexie.android.dng.navi.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveStep;

import org.kexie.android.dng.navi.R;
import org.kexie.android.dng.navi.model.JsonPoint;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luke on 2018/12/27.
 */

public final class SdkRoute
        implements Parcelable,
        Route
{
    protected SdkRoute(Parcel in)
    {
        from = in.readParcelable(JsonPoint.class.getClassLoader());
        to = in.readParcelable(JsonPoint.class.getClassLoader());
        path = in.readParcelable(DrivePath.class.getClassLoader());
    }

    public static final Creator<SdkRoute> CREATOR = new Creator<SdkRoute>()
    {
        @Override
        public SdkRoute createFromParcel(Parcel in)
        {
            return new SdkRoute(in);
        }

        @Override
        public SdkRoute[] newArray(int size)
        {
            return new SdkRoute[size];
        }
    };

    private SdkRoute(Builder builder)
    {
        from = builder.from;
        to = builder.to;
        path = builder.path;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(from, flags);
        dest.writeParcelable(to, flags);
        dest.writeParcelable(path, flags);
    }

    public static class Step
    {
        public final DriveStep driveStep;

        private Step(Builder builder)
        {
            driveStep = builder.driveStep;
        }

        public String getAction()
        {
            return driveStep.getAction();
        }

        public int getActionRes()
        {
            String actionName = driveStep.getAction();
            if (actionName == null || actionName.equals(""))
            {
                return R.mipmap.dir3;
            }
            if ("左转".equals(actionName))
            {
                return R.mipmap.dir2;
            }
            if ("右转".equals(actionName))
            {
                return R.mipmap.dir1;
            }
            if ("向左前方行驶".equals(actionName) || "靠左".equals(actionName))
            {
                return R.mipmap.dir6;
            }
            if ("向右前方行驶".equals(actionName) || "靠右".equals(actionName))
            {
                return R.mipmap.dir5;
            }
            if ("向左后方行驶".equals(actionName) || "左转调头".equals(actionName))
            {
                return R.mipmap.dir7;
            }
            if ("向右后方行驶".equals(actionName))
            {
                return R.mipmap.dir8;
            }
            if ("直行".equals(actionName))
            {
                return R.mipmap.dir3;
            }
            if ("减速行驶".equals(actionName))
            {
                return R.mipmap.dir4;
            }
            return R.mipmap.dir3;
        }

        public static final class Builder
        {
            private DriveStep driveStep;

            public Builder()
            {
            }

            public Builder driveStep(DriveStep val)
            {
                driveStep = val;
                return this;
            }

            public Step build()
            {
                return new Step(this);
            }
        }
    }

    public final JsonPoint from;
    public final JsonPoint to;
    public final DrivePath path;

    @Override
    public JsonPoint getFrom()
    {
        return from;
    }

    @Override
    public JsonPoint getTo()
    {
        return to;
    }

    @Override
    public List<JsonPoint> getPoints()
    {
        List<DriveStep> driveSteps = path.getSteps();
        List<JsonPoint> points = new ArrayList<>(driveSteps.size());
        for (DriveStep step : driveSteps)
        {
            points.add(new JsonPoint(step.getPolyline().get(0)));
        }
        return points;
    }

    public String getTime()
    {
        long second = path.getDuration();
        if (second > 3600)
        {
            long hour = second / 3600;
            long miniate = (second % 3600) / 60;
            return hour + "小时" + miniate + "分钟";
        }
        if (second >= 60)
        {
            long miniate = second / 60;
            return miniate + "分钟";
        }
        return second + "秒";
    }

    public String getLength()
    {
        int lenMeter = (int) path.getDistance();
        if (lenMeter > 10000) // 10 km
        {
            float dis = lenMeter / 1000;
            return dis + "千米";
        }
        if (lenMeter > 1000)
        {
            float dis = (float) lenMeter / 1000;
            DecimalFormat fnum = new DecimalFormat("##0.0");
            String dstr = fnum.format(dis);
            return dstr + "千米";
        }
        if (lenMeter > 100)
        {
            float dis = lenMeter / 50 * 50;
            return dis + "米";
        }
        float dis = lenMeter / 10 * 10;
        if (dis == 0)
        {
            dis = 10;
        }
        return dis + "米";
    }

    public String getName()
    {
        return path.getStrategy();
    }


    public List<Step> getSteps()
    {
        List<DriveStep> driveSteps = path.getSteps();
        List<Step> steps = new ArrayList<>(driveSteps.size());
        for (DriveStep driveStep : driveSteps)
        {
            steps.add(new Step.Builder().driveStep(driveStep).build());
        }
        return steps;
    }

    public static final class Builder
    {
        private JsonPoint from;
        private JsonPoint to;
        private DrivePath path;

        public Builder()
        {
        }

        public Builder from(JsonPoint val)
        {
            from = val;
            return this;
        }

        public Builder to(JsonPoint val)
        {
            to = val;
            return this;
        }

        public Builder path(DrivePath val)
        {
            path = val;
            return this;
        }

        public SdkRoute build()
        {
            return new SdkRoute(this);
        }
    }
}
