package org.kexie.android.dng.navi.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import com.amap.api.maps.AMap
import com.amap.api.maps.TextureSupportMapFragment
import com.amap.api.navi.view.RouteOverLay
import mapper.Mapper
import mapper.Mapping
import mapper.Request
import org.kexie.android.dng.navi.R
import org.kexie.android.dng.navi.databinding.FragmentRouteBinding
import org.kexie.android.dng.navi.viewmodel.NaviViewModel

typealias MapController = AMap

@Mapping("dng/navi/route")
class RouteFragment : Fragment() {

    private var binding: FragmentRouteBinding? = null

    private lateinit var mapController: MapController

    private lateinit var viewModel: NaviViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (binding == null) {
            binding = DataBindingUtil.inflate(inflater,
                    R.layout.fragment_route, container,
                    false)!!
        }
        return binding!!.root
    }

    override fun onViewCreated(view: View,
                               savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        val binding = this.binding!!

        binding.lifecycleOwner = this

        viewModel = ViewModelProviders.of(requireParentFragment())
                .get(NaviViewModel::class.java)

        @Suppress("CAST_NEVER_SUCCEEDS")
        val mapFragment = childFragmentManager
                .findFragmentById(R.id.map_view)
                as TextureSupportMapFragment

        mapController = mapFragment.map

        with(mapController.uiSettings)
        {
            isScrollGesturesEnabled = false
            isZoomGesturesEnabled = false
            isTiltGesturesEnabled = false
            isRotateGesturesEnabled = false
            isZoomControlsEnabled = false
        }

        val bundle = arguments
        if (bundle != null) {
            val pathId = bundle.getInt("pathId")
            apply(pathId)
        }
    }

    private fun apply(pathId: Int) {

        val binding = this.binding!!

        val paths = viewModel.routes.value!!

        val routeInfo = paths.getValue(pathId)

        binding.route = routeInfo

        RouteOverLay(mapController,
                routeInfo.path,
                requireContext().applicationContext)
                .apply {
                    isTrafficLine = true
                    addToMap()
                    zoomToSpan(200)
                }

        binding.setOnJumpToNavi {

            val request = Request.Builder()
                    .uri("dng/navi/navi")
                    .code(1)
                    .build()

            viewModel.select(pathId)

            val parent = requireParentFragment()

            val manager = parent.requireFragmentManager()

            manager.beginTransaction()
                    .hide(parent)
                    .addToBackStack(null)
                    .add(parent.id, Mapper.getOn(parent, request))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
        }

        binding.setOnJumpToDetails {
            viewModel.select(pathId)
        }
    }
}