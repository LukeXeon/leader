package org.kexie.android.dng.navi.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amap.api.maps.CameraUpdateFactory
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

        val mapController = mapFragment.map!!

        val bundle = arguments

        if (bundle != null) {

            viewModel = ViewModelProviders.of(targetFragment!!)
                    .get(NaviViewModel::class.java)

            val id = bundle.getInt("pathId")

            viewModel.routes.observe(this,
                    Observer {

                        mapController.clear()

                        val routeInfo = it.getValue(id)

                        mapController.setMapStatusLimits(routeInfo.bounds)

                        val routeOverLay = RouteOverLay(mapController,
                                routeInfo.path,
                                requireContext().applicationContext)

                        routeOverLay.isTrafficLine = true

                        routeOverLay.addToMap()

                        mapController.moveCamera(CameraUpdateFactory.zoomOut())
                        mapController.moveCamera(CameraUpdateFactory.zoomOut())
                        mapController.moveCamera(CameraUpdateFactory.zoomOut())

                        binding.setOnBack { requireActivity().onBackPressed() }

                        binding.setOnToNavi {

                            val request = Request.Builder()
                                    .uri("dng/navi/navi")
                                    .build();

                            val parent = requireParentFragment()

                            val manager = parent.requireFragmentManager()

                            manager.beginTransaction()
                                    .addToBackStack(null)
                                    .runOnCommit {
                                        manager.popBackStack()
                                    }
                                    .add(id, Mapper.getOn(targetFragment!!, request))
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .commit()

                        }
                    })

        }
    }
}