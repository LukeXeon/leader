package org.kexie.android.dng.ux.model.beans;

/**
 * Created by Mr.小世界 on 2018/11/28.
 */

public class JsonPollingResult
{
    public static final int CODE_STATE_CONTINUE = 0;
    public static final int CODE_STATE_WAIT_FOR_CONTINUE = 1;
    public static final int CODE_STATE_SUCCESS = 2;
    public static final int CODE_STATE_INVALID = 3;
    public int statusCode;
}
