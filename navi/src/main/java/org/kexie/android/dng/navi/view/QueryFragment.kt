package org.kexie.android.dng.navi.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.*
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.viewpager.widget.PagerAdapter
import com.amap.api.maps.AMap
import com.amap.api.maps.TextureSupportMapFragment
import com.amap.api.maps.model.MyLocationStyle
import com.orhanobut.logger.Logger
import com.uber.autodispose.AutoDispose.autoDisposable
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import mapper.BR
import mapper.Mapper
import mapper.Mapping
import mapper.Request
import org.kexie.android.common.databinding.GenericQuickAdapter
import org.kexie.android.common.widget.ProgressFragment
import org.kexie.android.dng.navi.R
import org.kexie.android.dng.navi.databinding.FragmentQueryBinding
import org.kexie.android.dng.navi.model.Point
import org.kexie.android.dng.navi.viewmodel.DEBUG_TEXT
import org.kexie.android.dng.navi.viewmodel.InputTipViewModel
import org.kexie.android.dng.navi.viewmodel.NaviViewModel
import org.kexie.android.dng.navi.viewmodel.entity.InputTip
import org.kexie.android.dng.navi.widget.ScaleTransformer

typealias MapController = AMap

@Mapping("dng/navi/query")
class QueryFragment:Fragment() {

    private lateinit var naviViewModel: NaviViewModel

    private lateinit var inputTipViewModel: InputTipViewModel

    private lateinit var mTipsAdapter: GenericQuickAdapter<InputTip>

    private lateinit var binding: FragmentQueryBinding

    private lateinit var mapController: MapController

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

        naviViewModel = ViewModelProviders.of(this)
                .get(NaviViewModel::class.java)

        inputTipViewModel = ViewModelProviders.of(this)
                .get(InputTipViewModel::class.java)

        @Suppress("CAST_NEVER_SUCCEEDS")
        val mapFragment = childFragmentManager
                .findFragmentById(R.id.map_view) as TextureSupportMapFragment

        lifecycle.addObserver(LifecycleEventObserver { lifecycleOwner: LifecycleOwner, event: Lifecycle.Event ->
            Logger.d("" + lifecycleOwner.lifecycle.currentState + " " + event)
        })

        mapController = mapFragment.map

        mTipsAdapter = GenericQuickAdapter<InputTip>(R.layout.item_tip, BR.inputTip)
                .apply {
                    setOnItemClickListener { adapter,
                                             view,
                                             position ->
                        val point = with(mapController.myLocation)
                        {
                            Point.form(longitude, latitude)
                        }
                        naviViewModel.query(data[position]!!, point)
                    }
                }

        binding.apply {
            lifecycleOwner = this@QueryFragment
            routePager.setPageTransformer(false, ScaleTransformer())
            routePager.offscreenPageLimit = 3
            query = queryText
            tipsAdapter = mTipsAdapter
        }

        val layoutStyle = MyLocationStyle()
                .apply {
                    interval(2000)

                }

        mapController.apply {
            myLocationStyle = layoutStyle
            isMyLocationEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
        }

        inputTipViewModel.inputTips.observe(this,
                Observer {
                    binding.isShowTips = !it.isEmpty()
                    mTipsAdapter.setNewData(it)
                })

        Transformations.map(naviViewModel.routes) {
            it.keys
        }.observe(this, Observer {
            val isEmpty = it.isEmpty()
            val fragments = if (isEmpty)
                emptyList<Fragment>()
            else
                it.map { id ->
                    val bundle = Bundle()
                    bundle.putInt("pathId", id)
                    Request.Builder()
                            .bundle(bundle)
                            .uri("dng/navi/route")
                            .build()
                }.map { request ->
                    Mapper.getOn(this, request)
                }

            binding.apply {
                isShowRoutes = !isEmpty
                routeAdapter = fastFragmentAdapter(fragments)
            }
        })

        queryText.observe(this, Observer {
            inputTipViewModel.query(it)
        })

        requireActivity().addOnBackPressedCallback(
                this,
                OnBackPressedCallback {
                    if (lifecycle.currentState == RESUMED) {
                        if (!naviViewModel.routes.value.isNullOrEmpty()) {
                            naviViewModel.routes.value = emptyMap()
                            return@OnBackPressedCallback true
                        }
                        if (!inputTipViewModel.inputTips.value.isNullOrEmpty()) {
                            inputTipViewModel.inputTips.value = emptyList()
                            return@OnBackPressedCallback true
                        }
                    }
                    return@OnBackPressedCallback false
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

        ProgressFragment.observeWith(naviViewModel.isLoading, this)

        queryText.value = DEBUG_TEXT

    }

    private fun fastFragmentAdapter(list: List<Fragment>): PagerAdapter {
        return object : FragmentPagerAdapter(childFragmentManager) {
            override fun getCount(): Int {
                return list.size
            }

            override fun getItem(position: Int): Fragment {
                return list[position]
            }
        }
    }
}


