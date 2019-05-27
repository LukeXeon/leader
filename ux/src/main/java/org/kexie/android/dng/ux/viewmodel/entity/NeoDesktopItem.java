package org.kexie.android.dng.ux.viewmodel.entity;

public final class NeoDesktopItem {
    public final int image;
    public final String name;
    public final String path;

    public NeoDesktopItem(String name, int image, String path) {
        this.image = image;
        this.name = name;
        this.path = path;
    }
}
