package kexie.android.navi.entity;

import java.util.Collections;
import java.util.List;

public class Route
{
    public final String name;
    public final String time;
    public final String length;
    public final List<Step> steps;

    public Route(String name,
                 String time,
                 String length,
                 List<Step> steps)
    {
        this.name = name;
        this.time = time;
        this.length = length;
        this.steps = Collections.unmodifiableList(steps);
    }
}
