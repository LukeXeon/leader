package org.kexie.android.dng.navi.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amap.api.maps.AMap
import com.amap.api.navi.AMapNaviView
import com.uber.autodispose.AutoDispose.autoDisposable
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from
import es.dmoral.toasty.Toasty
import io.reactivex.android.schedulers.AndroidSchedulers
import mapper.Mapping
import org.kexie.android.common.widget.ProgressFragment
import org.kexie.android.dng.navi.R
import org.kexie.android.dng.navi.databinding.FragmentNavigationBinding
import org.kexie.android.dng.navi.model.Query
import org.kexie.android.dng.navi.viewmodel.NaviViewModel
import org.kexie.android.dng.navi.widget.NaviViewFragment

const val ARG = "route"

@Mapping("dng/navi/navi")
class NaviFragment : Fragment() {

    private lateinit var viewModel: NaviViewModel

    private lateinit var binding: FragmentNavigationBinding

    private lateinit var naviView: AMapNaviView

    private lateinit var mapController: AMap

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_navigation, container,
                false)
        return binding.root
    }

    override fun onViewCreated(view: View,
                               savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //initViews

        val fragment = childFragmentManager
                .findFragmentById(R.id.fragment_navi) as NaviViewFragment

        naviView = fragment.innerView!!

        mapController = naviView.map

        val target = targetFragment

        if (target != null) {

            viewModel = ViewModelProviders.of(target)
                    .get(NaviViewModel::class.java)

            viewModel.start()

            requireActivity().addOnBackPressedCallback(this, OnBackPressedCallback {
                requireFragmentManager().popBackStack()
                requireFragmentManager().popBackStack()
                return@OnBackPressedCallback true
            })

        } else {

            viewModel = ViewModelProviders.of(this)
                    .get(NaviViewModel::class.java)

            val bundle = arguments

            if (bundle != null) {

                val query = bundle.getParcelable<Query>("query")!!

                with(viewModel) {

                    query(query)

                    onError.observeOn(AndroidSchedulers.mainThread())
                            .filter {
                                lifecycle.currentState.isAtLeast(RESUMED)
                            }.`as`(autoDisposable(from(this@NaviFragment, ON_DESTROY)))
                            .subscribe {
                                Toasty.error(requireContext(), it).show()
                            }

                    viewModel.onSuccess.observeOn(AndroidSchedulers.mainThread())
                            .filter {
                                lifecycle.currentState.isAtLeast(RESUMED)
                            }.`as`(autoDisposable(from(this@NaviFragment, ON_DESTROY)))
                            .subscribe {
                                Toasty.error(requireContext(), it).show()
                            }

                    routes.observe(this@NaviFragment, Observer {
                        it.keys.singleOrNull()?.let { x ->
                            viewModel.select(x)
                        }
                    })

                }
            }

        }

        ProgressFragment.observeWith(viewModel.isLoading, this)
    }
}
