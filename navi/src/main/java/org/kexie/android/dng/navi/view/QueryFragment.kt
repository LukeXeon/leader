package org.kexie.android.dng.navi.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.uber.autodispose.AutoDispose.autoDisposable
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import mapper.Mapping
import org.kexie.android.common.widget.ProgressFragment
import org.kexie.android.dng.navi.R
import org.kexie.android.dng.navi.databinding.FragmentQueryBinding
import org.kexie.android.dng.navi.viewmodel.InputTipViewModel
import org.kexie.android.dng.navi.viewmodel.NaviViewModel
import org.kexie.android.dng.navi.widget.ScaleTransformer

@Mapping("dng/navi/query")
class QueryFragment:Fragment() {

    private lateinit var naviViewModel: NaviViewModel

    private lateinit var inputTipViewModel: InputTipViewModel

    private lateinit var binding: FragmentQueryBinding

    private val queryText = MutableLiveData<String>()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_query,
                container,
                false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this

        naviViewModel = ViewModelProviders.of(targetFragment!!)
                .get(NaviViewModel::class.java)

        inputTipViewModel = ViewModelProviders.of(this)
                .get(InputTipViewModel::class.java)

        val routeAdapter = RouteAdapter(emptyList(), this)

        binding.routeAdapter = routeAdapter

        binding.routePager.setPageTransformer(false, ScaleTransformer())

        Transformations.map(naviViewModel.routes) {
            it.keys.toList()
        }.observe(this, Observer {
            routeAdapter.ids = it;
            routeAdapter.notifyDataSetChanged()
        })

        queryText.observe(this, Observer {
            inputTipViewModel.query(it)
        })

        requireActivity().addOnBackPressedCallback(
                this,
                OnBackPressedCallback {
                    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        if (inputTipViewModel.inputTips.value?.size != 0) {
                            inputTipViewModel.inputTips.value = emptyList()
                            return@OnBackPressedCallback true
                        }
                        if (naviViewModel.routes.value?.size != 0) {
                            naviViewModel.routes.value = emptyMap()
                            return@OnBackPressedCallback true
                        }
                    }
                    false
                }
        )

        Observable.merge(naviViewModel.onSuccess, inputTipViewModel.onSuccess)
                .filter {
                    lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
                }.`as`(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    Toasty.success(requireContext(), it).show()
                }

        Observable.merge(naviViewModel.onError, inputTipViewModel.onError)
                .filter {
                    lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
                }.`as`(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    Toasty.error(requireContext(), it).show()
                }

        ProgressFragment.observe(naviViewModel.isLoading, this)
    }
}