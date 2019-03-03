package org.kexie.android.dng.navi.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.TextureSupportMapFragment
import com.amap.api.navi.view.RouteOverLay
import mapper.Mapper
import mapper.Mapping
import mapper.Request
import org.kexie.android.dng.navi.R
import org.kexie.android.dng.navi.databinding.FragmentRouteBinding
import org.kexie.android.dng.navi.viewmodel.NaviViewModel

@Mapping("dng/navi/route")
class RouteFragment : Fragment() {

    private var binding: FragmentRouteBinding? = null

    private val id = MutableLiveData<Int>()

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

    private fun apply(pathId:Int) {

        val binding = this.binding!!

        val paths = viewModel.routes.value!!

        val routeInfo = paths.getValue(pathId)

        mapController.setMapStatusLimits(routeInfo.bounds)

        val routeOverLay = RouteOverLay(mapController,
                routeInfo.path,
                requireContext().applicationContext)

        routeOverLay.isTrafficLine = true

        routeOverLay.addToMap()

        with(mapController)
        {
            moveCamera(CameraUpdateFactory.zoomOut())
            moveCamera(CameraUpdateFactory.zoomOut())
            moveCamera(CameraUpdateFactory.zoomOut())
        }

        binding.infosList.setGuideData(routeInfo.guideInfos)

        binding.setOnJumpToNavi {

            val request = Request.Builder()
                    .uri("dng/navi/navi")
                    .code(1)
                    .build()

            viewModel.select(pathId)

            val parent = requireParentFragment()

            val manager = parent.requireFragmentManager()

            manager.beginTransaction()
                    .addToBackStack(null)
                    .add(parent.id, Mapper.getOn(parent, request))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
        }

        binding.setOnJumpToDetails {

            val bundle = Bundle()

            bundle.putInt("pathId", pathId)

            val request = Request.Builder()
                    .code(1)
                    .bundle(bundle)
                    .uri("dng/navi/details")
                    .build()

            val parent = requireParentFragment()

            val manager = parent.requireFragmentManager()

            manager.beginTransaction()
                    .addToBackStack(null)
                    .add(parent.id, Mapper.getOn(parent, request))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
        }
    }
}
