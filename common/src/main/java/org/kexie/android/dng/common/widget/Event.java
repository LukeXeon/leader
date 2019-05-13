package org.kexie.android.dng.common.widget;

import java.lang.reflect.Method;

public final class Event {

    Method mTarget;
    Object[] mArgs;

    private Event(Method method, Object[] args) {
        mTarget = method;
        mArgs = args;
    }

    private static final short POOL_MAX_SIZE = 10;
    private static short index = -1;
    private static final Event[] sPool = new Event[POOL_MAX_SIZE];

    static Event obtain(Method method, Object[] args) {
        if (index < 0) {
            return new Event(method, args);
        } else {
            Event event;
            synchronized (Event.class) {
                event = sPool[index];
                sPool[index--] = null;
            }
            event.mTarget = method;
            event.mArgs = args;
            return event;
        }
    }

    void recycle() {
        mTarget = null;
        mArgs = null;
        synchronized (Event.class) {
            if (index < POOL_MAX_SIZE - 1) {
                sPool[++index] = this;
            }
        }
    }
}
