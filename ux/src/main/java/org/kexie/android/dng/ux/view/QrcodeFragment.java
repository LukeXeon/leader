package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentQrcodeBinding;
import org.kexie.android.dng.ux.viewmodel.LoginViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;
import mapper.Mapping;

import static com.uber.autodispose.AutoDispose.autoDisposable;
import static com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider.from;

@Mapping("dng/ux/login")
public class QrcodeFragment extends Fragment
{
    private FragmentQrcodeBinding binding;
    private LoginViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_qrcode,
                container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        //liveData
        viewModel.getQrcode().observe(this,binding::setQrCode);
        viewModel.requestQrcode();
        //rx
        viewModel.getOnErrorMessage()
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(s -> Toasty.error(getContext(), s).show());
        viewModel.getOnSuccessMessage()
                .as(autoDisposable(from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(s -> Toasty.success(getContext(), s).show());
    }
}
