package org.kexie.android.dng.navi.viewmodel.beans;

import android.graphics.Bitmap;

import com.autonavi.ae.gmap.gloverlay.GLCrossVector.AVectorCrossAttr;

public class ModelCrossImage {
    public final byte[] buffer;
    public final AVectorCrossAttr attr;
    public final Bitmap res;
    public ModelCrossImage(byte[] buffer, AVectorCrossAttr attr, Bitmap res) {
        this.buffer = buffer;
        this.attr = attr;
        this.res = res;
    }
}
