package org.kexie.android.dng.navi.viewmodel.entity

import android.graphics.Bitmap
import com.autonavi.ae.gmap.gloverlay.GLCrossVector

data class ModeCross(
        val buffer:ByteArray,
        val attr: GLCrossVector.AVectorCrossAttr,
        val res:Bitmap)