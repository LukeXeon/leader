package org.kexie.android.dng.navi.viewmodel.beans;

import com.amap.api.navi.model.AMapTrafficStatus;

import java.util.List;

public class NaviDescription {
    public final int allLength;
    public final List<AMapTrafficStatus> trafficStatuses;
    public final int pathRetainDistance;
    public final int iconType;
    public final String nextRoadName;
    public final String nextRoadDistance;
    public final int curStep;

    public NaviDescription(int allLength,
                           List<AMapTrafficStatus> trafficStatuses,
                           int pathRetainDistance,
                           int iconType,
                           String nextRoadName,
                           String nextRoadDistance,
                           int curStep) {
        this.allLength = allLength;
        this.trafficStatuses = trafficStatuses;
        this.pathRetainDistance = pathRetainDistance;
        this.iconType = iconType;
        this.nextRoadName = nextRoadName;
        this.nextRoadDistance = nextRoadDistance;
        this.curStep = curStep;
    }
}
