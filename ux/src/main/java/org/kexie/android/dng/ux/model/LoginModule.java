package org.kexie.android.dng.ux.model;

import org.kexie.android.dng.ux.model.entity.JsonPollingResult;
import org.kexie.android.dng.ux.model.entity.JsonQrcode;

import retrofit2.Call;
import retrofit2.http.GET;

public interface LoginModule
{

    @GET("/adasd")
    Call<JsonPollingResult> polling();

    @GET("/asadasd")
    Call<JsonQrcode> getQrcode();

}
