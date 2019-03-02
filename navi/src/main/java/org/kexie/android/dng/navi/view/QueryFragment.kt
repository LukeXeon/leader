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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProviders
import com.orhanobut.logger.Logger
import com.uber.autodispose.AutoDispose.autoDisposable
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import mapper.BR
import mapper.Mapping
import org.kexie.android.common.databinding.GenericQuickAdapter
import org.kexie.android.common.widget.ProgressFragment
import org.kexie.android.dng.navi.R
import org.kexie.android.dng.navi.databinding.FragmentQueryBinding
import org.kexie.android.dng.navi.viewmodel.InputTipViewModel
import org.kexie.android.dng.navi.viewmodel.NaviViewModel
import org.kexie.android.dng.navi.viewmodel.entity.InputTip
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

        naviViewModel = ViewModelProviders.of(this)
                .get(NaviViewModel::class.java)

        inputTipViewModel = ViewModelProviders.of(this)
                .get(InputTipViewModel::class.java)

        val routeAdapter = RouteAdapter(emptyList(), this)

        binding.routeAdapter = routeAdapter

        binding.routePager.setPageTransformer(false, ScaleTransformer())

        val tipAdapter = GenericQuickAdapter<InputTip>(R.layout.item_tip, BR.inputTip)

        binding.tipsAdapter = tipAdapter

        binding.query = queryText

        inputTipViewModel.inputTips.observe(this,
                Observer {
                    binding.isShowTips = !it.isEmpty()
                    tipAdapter.setNewData(it)
                })

        Transformations.map(naviViewModel.routes) {
            it.keys.toList()
        }.observe(this, Observer {
            binding.isShowRoutes = !it.isEmpty()
            routeAdapter.ids = it
            routeAdapter.notifyDataSetChanged()
        })

        queryText.observe(this, Observer {
            Logger.d(it)
            //inputTipViewModel.query(it)
        })

        requireActivity().addOnBackPressedCallback(
                this,
                OnBackPressedCallback {
                    if (lifecycle.currentState.isAtLeast(RESUMED)) {
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
                .observeOn(AndroidSchedulers.mainThread())
                .filter {
                    lifecycle.currentState.isAtLeast(RESUMED)
                }.`as`(autoDisposable(from(this, ON_DESTROY)))
                .subscribe {
                    Toasty.success(requireContext(), it).show()
                }

        Observable.merge(naviViewModel.onError, inputTipViewModel.onError)
                .observeOn(AndroidSchedulers.mainThread())
                .filter {
                    lifecycle.currentState.isAtLeast(RESUMED)
                }.`as`(autoDisposable(from(this, ON_DESTROY)))
                .subscribe {
                    Toasty.error(requireContext(), it).show()
                }

        ProgressFragment.observe(naviViewModel.isLoading, this)

    }
}