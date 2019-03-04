package org.kexie.android.dng.navi.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProviders
import com.amap.api.maps.TextureSupportMapFragment
import com.amap.api.navi.view.RouteOverLay
import mapper.Mapper
import mapper.Mapping
import mapper.Request
import org.kexie.android.dng.navi.R
import org.kexie.android.dng.navi.databinding.FragmentDetailsBinding
import org.kexie.android.dng.navi.viewmodel.NaviViewModel


@Mapping("dng/navi/details")
class DetailsFragment : Fragment() {

    private lateinit var binding: FragmentDetailsBinding

    private lateinit var viewModel: NaviViewModel

    private lateinit var mapController:MapController

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_details,
                container,
                false)
        return binding.getRoot()
    }

    override fun onViewCreated(view: View,
                               savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        @Suppress("ClickableViewAccessibility")
        binding.root.setOnTouchListener { x, y -> true }

        @Suppress("CAST_NEVER_SUCCEEDS")
        val mapFragment = childFragmentManager
                .findFragmentById(R.id.map_view)
                as TextureSupportMapFragment

        mapController = mapFragment.map!!

        val bundle = arguments

        if (bundle != null) {

            viewModel = ViewModelProviders.of(targetFragment!!)
                    .get(NaviViewModel::class.java)

            val pathId = bundle.getInt("pathId")

            apply(pathId)

        }

    }

    private fun apply(pathId:Int) {

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
            moveCamera(com.amap.api.maps.CameraUpdateFactory.zoomOut())
            moveCamera(com.amap.api.maps.CameraUpdateFactory.zoomOut())
            moveCamera(com.amap.api.maps.CameraUpdateFactory.zoomOut())
        }

        binding.setOnBack { requireActivity().onBackPressed() }

        binding.setOnToNavi {

            val request = Request.Builder()
                    .uri("dng/navi/navi")
                    .code(1)
                    .build();

            viewModel.select(pathId)

            val target = targetFragment!!

            val manager = target.requireFragmentManager()

            requireFragmentManager()

            manager.beginTransaction()
                    .hide(target)
                    .addToBackStack(null)
                    .add(target.id, Mapper.getOn(target, request))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
        }
    }
}