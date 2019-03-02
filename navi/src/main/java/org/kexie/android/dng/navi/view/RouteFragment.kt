package org.kexie.android.dng.navi.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amap.api.maps.AMap
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

    private lateinit var mapController: AMap

    private lateinit var viewModel: NaviViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (binding == null) {
            binding = DataBindingUtil.inflate(inflater,
                    R.layout.fragment_route, container,
                    false)
        }
        return binding!!.root
    }

    override fun onViewCreated(view: View,
                               savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding!!.lifecycleOwner = this

        viewModel = ViewModelProviders.of(targetFragment!!)
                .get(NaviViewModel::class.java)

        @Suppress("CAST_NEVER_SUCCEEDS")
        val mapFragment = childFragmentManager
                .findFragmentById(R.id.map_view)
                as TextureSupportMapFragment

        mapController = mapFragment.map

        val uiSettings = mapController.uiSettings

        uiSettings.isScrollGesturesEnabled = false

        uiSettings.isZoomGesturesEnabled = false

        uiSettings.isTiltGesturesEnabled = false

        uiSettings.isRotateGesturesEnabled = false

        uiSettings.isZoomControlsEnabled = false

    }

    fun apply(id: Int) {

        viewModel.routes.observe(this, Observer {

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

            binding!!.infosList.setGuideData(routeInfo.guideInfos)

            binding!!.setOnJumpToNavi {

                val request = Request.Builder()
                        .uri("dng/navi/navi")
                        .build()

                val parent = requireParentFragment()

                val manager = parent.requireFragmentManager()

                manager.beginTransaction()
                        .addToBackStack(null)
                        .runOnCommit {
                            manager.popBackStack()
                        }
                        .add(parent.id, Mapper.getOn(targetFragment!!, request))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
            }

            binding!!.setOnJumpToDetails {

                val bundle = Bundle()

                bundle.putInt("pathId", id)

                val request = Request.Builder()
                        .code(1)
                        .bundle(bundle)
                        .uri("dng/navi/details")
                        .build()

                val parent = requireParentFragment()

                val manager = parent.requireFragmentManager()

                manager.beginTransaction()
                        .addToBackStack(null)
                        .add(parent.id, Mapper.getOn(targetFragment!!, request))
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
            }
        })
    }


}


class RouteAdapter(var ids: List<Int>,
              var root: Fragment)
    : FragmentPagerAdapter(root.requireFragmentManager()) {

    private val fragments = SparseArrayCompat<RouteFragment>()

    override fun getCount(): Int {
        return ids.size
    }

    override fun getItem(position: Int): Fragment {

        var fragment = fragments[position]
        return if (fragment != null) {
            fragment.apply(ids[position])
            fragment
        } else {
            val request = Request.Builder()
                    .uri("dng/navi/route")
                    .code(1)
                    .build()
            fragment = Mapper.getOn(root.targetFragment!!, request) as RouteFragment
            fragments.put(position, fragment)
            fragment.apply(ids[position])
            fragment
        }
    }
}