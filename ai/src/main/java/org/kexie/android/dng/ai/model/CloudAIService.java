package org.kexie.android.dng.ai.model;

import org.kexie.android.dng.ai.model.entity.CloudAIRequest;
import org.kexie.android.dng.ai.model.entity.CloudAIResponse;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author Luke
 */
public interface CloudAIService
{
    @POST("/openapi/api/v2")
    Observable<CloudAIResponse> post(@Body CloudAIRequest request);
}
