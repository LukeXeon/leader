package org.kexie.android.dng.navi.widget

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.amap.api.navi.AMapNaviView

typealias NaviView = AMapNaviView

class NaviViewFragment : Fragment() {
    var innerView: NaviView? = null
        private set

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return innerView
    }

    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
        if (innerView == null) {
            innerView = NaviView(context)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (innerView == null) {
            innerView = NaviView(context)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        innerView!!.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        innerView!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        innerView!!.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (innerView != null) {
            innerView!!.onDestroy()
            innerView = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        innerView!!.onSaveInstanceState(outState)
    }
}
