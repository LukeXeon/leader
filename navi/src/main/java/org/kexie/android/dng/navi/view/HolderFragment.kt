package org.kexie.android.dng.navi.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import mapper.Mapping
import org.kexie.android.dng.navi.viewmodel.NaviViewModel

@Mapping("dng/navi/main")
class HolderFragment:Fragment() {

    private lateinit var naviViewModel: NaviViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        naviViewModel = ViewModelProviders.of(this).get(NaviViewModel::class.java)

    }

}