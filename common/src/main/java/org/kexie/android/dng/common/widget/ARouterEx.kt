package org.kexie.android.dng.common.widget

import com.alibaba.android.arouter.launcher.ARouter


inline fun <reified T> navigationAs(path:String):T {
    return ARouter.getInstance().build(path).navigation() as T
}