package org.kexie.android.dng.asr.model;

import org.kexie.android.dng.asr.model.entity.AIRequest;
import org.kexie.android.dng.asr.model.entity.AIResponse;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author Luke
 */
public interface AIService
{
    @POST("/openapi/api/v2")
    Observable<AIResponse> post(@Body AIRequest request);
}
