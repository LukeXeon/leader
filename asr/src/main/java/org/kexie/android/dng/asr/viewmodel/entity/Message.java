package org.kexie.android.dng.asr.viewmodel.entity;

public final class Message
{
    public static final int TYPE_AI = 0;
    public static final int TYPE_USER = 1;

    public final int type;
    public final String text;

    public Message(int type, String text)
    {
        this.type = type;
        this.text = text;
    }
}
