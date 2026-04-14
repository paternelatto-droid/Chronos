package com.latto.chronos.api;

import static com.latto.chronos.Constante.BASE_URL;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.latto.chronos.Constante;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;


public class ApiClient {
    private static Retrofit retrofit = null;

    private static Retrofit getClient(final Context context) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                            String token = sp.getString("token", null);

                            Request.Builder requestBuilder = original.newBuilder()
                                    .header("Accept", "application/json")
                                    .method(original.method(), original.body());

                            if (token != null) {
                                requestBuilder.header("Authorization", "Bearer " + token);
                            }
                            return chain.proceed(requestBuilder.build());
                        }
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constante.BASE_URL) // conserve ton BASE_URL
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getService(Context context) {
        return getClient(context).create(ApiService.class);
    }
}
