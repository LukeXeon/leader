package org.kexie.android.dng.ux.model.beans;

public  class FunctionInfo
{
    public final String name;
    public final int iconRes;
    public final String uri;

    public FunctionInfo(String name, int iconRes, String uri)
    {
        this.iconRes = iconRes;
        this.name = name;
        this.uri = uri;
    }

    public static FunctionInfo from(String name, int icon, String path)
    {
        return new FunctionInfo(name, icon, path);
    }
}
