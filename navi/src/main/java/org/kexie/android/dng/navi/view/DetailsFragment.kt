package org.kexie.android.dng.navi.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.TextureSupportMapFragment
import org.kexie.android.dng.navi.R
import org.kexie.android.dng.navi.databinding.FragmentDetailsBinding
import org.kexie.android.dng.navi.viewmodel.NaviViewModel

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


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View,
                               savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

            mapController.moveCamera(CameraUpdateFactory.zoomOut())
            mapController.moveCamera(CameraUpdateFactory.zoomOut())
            mapController.moveCamera(CameraUpdateFactory.zoomOut())

            binding.setOnBack { requireActivity().onBackPressed() }

        }
    }
}

