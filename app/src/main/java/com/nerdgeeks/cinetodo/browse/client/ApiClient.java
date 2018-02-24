package com.nerdgeeks.cinetodo.browse.client;

import com.nerdgeeks.cinetodo.browse.config.Config;
import com.nerdgeeks.cinetodo.browse.model.BrowseModel;
import com.nerdgeeks.cinetodo.browse.model.Data;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by hp on 5/7/2017.
 */

public class ApiClient {
    private static final String BASE_URL = "https://yts.ag";
    private static Retrofit retrofit = null;

    public interface MoviesAPI {
        @GET
        Call<BrowseModel> getResults(@Url String URL);
    }

    public static MoviesAPI getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(MoviesAPI.class);
    }
}
