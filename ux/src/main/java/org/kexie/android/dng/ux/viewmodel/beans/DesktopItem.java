package org.kexie.android.dng.ux.viewmodel.beans;

public final class DesktopItem {
    public final int image;
    public final String name;
    public final String path;

    public DesktopItem(String name, int image, String path) {
        this.image = image;
        this.name = name;
        this.path = path;
    }
}
