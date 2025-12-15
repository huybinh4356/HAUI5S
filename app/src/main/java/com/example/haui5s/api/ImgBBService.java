package com.example.haui5s.api;

import com.google.gson.annotations.SerializedName;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ImgBBService {

    class ImgBBResponse {
        @SerializedName("data")
        public ImgBBData data;
        public boolean success;
    }

    class ImgBBData {
        @SerializedName("url")
        public String url;
    }

    @Multipart
    @POST("upload")
    Call<ImgBBResponse> uploadImage(
            @Query("key") String apiKey,
            @Part MultipartBody.Part image
    );
}