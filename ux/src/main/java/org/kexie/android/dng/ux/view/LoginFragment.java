package org.kexie.android.dng.ux.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;

import org.kexie.android.dng.common.contract.Module;
import org.kexie.android.dng.ux.R;
import org.kexie.android.dng.ux.databinding.FragmentLoginBinding;
import org.kexie.android.dng.ux.viewmodel.LoginViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;

@Route(path = Module.Ux.login)
public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_login,
                container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        viewModel.qrcode.observe(this,
                drawable -> binding.qrCode.setImageDrawable(drawable));
        binding.qrCode.setOnClickListener(v -> viewModel.requestQrcode());
        viewModel.result.observe(this,
                result -> {
                    Toast toast = result
                            ? Toasty.success(requireContext(), "登录成功")
                            : Toasty.error(requireContext(), "二维码请求失败");
                    toast.show();
                });
        viewModel.text.observe(this, s -> binding.message.setText(s));
    }
}
