package com.nepapp.journeyjournal.api;

import com.nepapp.journeyjournal.models.LoginResponse;
import com.nepapp.journeyjournal.models.JournalResponse;
import com.nepapp.journeyjournal.models.SuccessResponse;
import com.nepapp.journeyjournal.models.UserResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiInterface {

    @FormUrlEncoded
    @POST("auth/register")
    Call<LoginResponse> registerUser(@Field("email") String email,
                                    @Field("fullname") String fullname,
                                    @Field("mobile") String mobile,
                                    @Field("password") String password);

    @FormUrlEncoded
    @POST("auth/login")
    Call<LoginResponse> loginUser(@Field("email") String email,
                                  @Field("password") String password);

    @GET("journals")
    Call<JournalResponse> getJournalEntries(@Header("Authorization") String authHeader);

    @Multipart
    @POST("journals")
    Call<SuccessResponse> addJournalEntry(@Header("Authorization") String authHeader,
                                          @Part("title") RequestBody title,
                                          @Part("description") RequestBody description,
                                          @Part MultipartBody.Part image);


    @Multipart
    @PUT("journals/{id}")
    Call<SuccessResponse> editJournalEntry(@Header("Authorization") String authHeader,
                                           @Part("title") RequestBody title,
                                           @Part("description") RequestBody description,
                                           @Part MultipartBody.Part image,
                                           @Path("id") String id);

    @DELETE("journals/{id}")
    Call<SuccessResponse> deleteJournalEntry(@Header("Authorization") String authHeader,
                                             @Path("id") String id);


    @FormUrlEncoded
    @PUT("users/updateAccount")
    Call<LoginResponse> updateAccountInfo(@Header("Authorization") String authHeader,
                                          @Field("fullname") String fullname,
                                          @Field("mobile") String mobile,
                                          @Field("email") String email);

    @FormUrlEncoded
    @PUT("users/updatePassword")
    Call<LoginResponse> updateAccountPassword(@Header("Authorization") String authHeader,
                                              @Field("currentPassword") String currentPassword,
                                              @Field("newPassword") String newPassword);
}
