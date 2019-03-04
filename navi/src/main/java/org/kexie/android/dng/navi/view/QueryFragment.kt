package org.kexie.android.dng.navi.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.PagerAdapter
import com.amap.api.maps.AMap
import com.amap.api.maps.TextureSupportMapFragment
import com.amap.api.maps.model.MyLocationStyle
import com.uber.autodispose.AutoDispose.autoDisposable
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from
import es.dmoral.toasty.Toasty
import io.reactivex.android.schedulers.AndroidSchedulers
import mapper.Mapper
import mapper.Mapping
import mapper.Request
import org.kexie.android.common.databinding.GenericQuickAdapter
import org.kexie.android.common.widget.ProgressFragment
import org.kexie.android.dng.navi.BR
import org.kexie.android.dng.navi.R
import org.kexie.android.dng.navi.databinding.FragmentQueryBinding
import org.kexie.android.dng.navi.model.Point
import org.kexie.android.dng.navi.viewmodel.InputTipViewModel
import org.kexie.android.dng.navi.viewmodel.NaviViewModel
import org.kexie.android.dng.navi.viewmodel.entity.InputTip
import org.kexie.android.dng.navi.widget.ScaleTransformer

typealias MapController = AMap

@Mapping("dng/navi/query")
class QueryFragment:Fragment() {

    private lateinit var mNaviViewModel: NaviViewModel

    private lateinit var mInputTipViewModel: InputTipViewModel

    private lateinit var mTipsAdapter: GenericQuickAdapter<InputTip>

    private lateinit var mBinding: FragmentQueryBinding

    private lateinit var mMapController: MapController

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_query,
                container,
                false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mNaviViewModel = ViewModelProviders.of(this)
                .get(NaviViewModel::class.java)

        mInputTipViewModel = ViewModelProviders.of(this)
                .get(InputTipViewModel::class.java)

        @Suppress("CAST_NEVER_SUCCEEDS")
        val mapFragment = childFragmentManager
                .findFragmentById(R.id.map_view) as TextureSupportMapFragment

        mMapController = mapFragment.map

        mTipsAdapter = GenericQuickAdapter<InputTip>(R.layout.item_tip, BR.inputTip)
                .apply {
                    setOnItemClickListener { adapter,
                                             view,
                                             position ->
                        val point = with(mMapController.myLocation)
                        {
                            Point.form(longitude, latitude)
                        }
                        mNaviViewModel.query(data[position]!!, point)
                    }
                }

        mBinding.apply {
            lifecycleOwner = this@QueryFragment
            routePager.setPageTransformer(false, ScaleTransformer())
            routePager.offscreenPageLimit = 3
            query = mInputTipViewModel.queryText
            tipsAdapter = mTipsAdapter
        }

        val layoutStyle = MyLocationStyle()
                .apply {
                    interval(2000)

                }

        mMapController.apply {
            myLocationStyle = layoutStyle
            isMyLocationEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
        }

        mInputTipViewModel.inputTips.observe(this,
                Observer {
                    if (!it.isEmpty()) {
                        mBinding.isShowTips = true
                    }
                    mTipsAdapter.setNewData(it)
                })

        Transformations.map(mNaviViewModel.routes) {
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

            mBinding.apply {
                isShowRoutes = !isEmpty
                routeAdapter = wrapToAdapter(fragments)
            }
        })

        requireActivity().addOnBackPressedCallback(
                this,
                OnBackPressedCallback {
                    if (!isHidden) {
                        if (!mNaviViewModel.routes.value.isNullOrEmpty()) {
                            mNaviViewModel.routes.value = emptyMap()
                            return@OnBackPressedCallback true
                        }
                        if (!mInputTipViewModel.inputTips.value.isNullOrEmpty()) {
                            mInputTipViewModel.inputTips.value = emptyList()
                            return@OnBackPressedCallback true
                        }
                    }
                    return@OnBackPressedCallback false
                }
        )

        mNaviViewModel.onSuccess.mergeWith(mInputTipViewModel.onSuccess)
                .observeOn(AndroidSchedulers.mainThread())
                .filter {
                    lifecycle.currentState.isAtLeast(RESUMED)
                }.`as`(autoDisposable(from(this, ON_DESTROY)))
                .subscribe {
                    Toasty.success(requireContext(), it).show()
                }

        mNaviViewModel.onError.mergeWith(mInputTipViewModel.onError)
                .observeOn(AndroidSchedulers.mainThread())
                .filter {
                    lifecycle.currentState.isAtLeast(RESUMED)
                }.`as`(autoDisposable(from(this, ON_DESTROY)))
                .subscribe {
                    Toasty.error(requireContext(), it).show()
                }

        ProgressFragment.observeWith(mNaviViewModel.isLoading, this)

        mBinding.isShowTips = true

    }

    private fun wrapToAdapter(list: List<Fragment>): PagerAdapter {

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


