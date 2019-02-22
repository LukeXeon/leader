package org.kexie.android.dng.navi.viewmodel.entity;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shixin on 2017/6/22.
 */

public class GuideInfo
{
    private String groupName;
    private int groupLen;
    private int groupTrafficLights;
    private int groupIconType;
    private int groupToll;
    private List<Step> steps;

    public GuideInfo() {
        steps = new ArrayList();
    }

    public List<Step> getSteps() {
        return steps;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getGroupLen() {
        return groupLen;
    }

    public void setGroupLen(int groupLen) {
        this.groupLen = groupLen;
    }

    public int getGroupTrafficLights() {
        return groupTrafficLights;
    }

    public void setGroupTrafficLights(int groupTrafficLights) {
        this.groupTrafficLights = groupTrafficLights;
    }

    public int getGroupIconType() {
        return groupIconType;
    }

    public void setGroupIconType(int groupIconType) {
        this.groupIconType = groupIconType;
    }

    public int getGroupToll() {
        return groupToll;
    }

    public void setGroupToll(int groupToll) {
        this.groupToll = groupToll;
    }

    public static class Step
    {
        private int stepIconType;
        private int stepDistance;
        private String stepRoadName;

        public Step(int iconType, String roadName, int distance) {
            stepIconType = iconType;
            stepDistance = distance;
            if (TextUtils.isEmpty(roadName)) {
                stepRoadName = "内部道路";
            } else {
                stepRoadName = roadName;
            }

        }

        public String getStepRoadName() {
            return stepRoadName;
        }

        public int getStepDistance() {
            return stepDistance;
        }

        public int getStepIconType() {
            return stepIconType;
        }
    }
}


