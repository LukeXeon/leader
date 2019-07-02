package org.kexie.android.dng.ai.viewmodel.beans;

public class TextMessage {
    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;
    public final int type;
    public final String text;

    public TextMessage(int type, String text) {
        this.type = type;
        this.text = text;
    }
}