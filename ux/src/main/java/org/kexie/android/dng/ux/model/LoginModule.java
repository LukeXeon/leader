package org.kexie.android.dng.ux.model;

import org.kexie.android.dng.ux.model.entity.JsonPollingResult;
import org.kexie.android.dng.ux.model.entity.JsonQrcode;

import retrofit2.Call;

public interface LoginModule
{

    Call<JsonPollingResult> polling();


    Call<JsonQrcode> getQrcode();

}
