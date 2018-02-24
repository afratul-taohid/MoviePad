package com.nerdgeeks.cinetodo.browse.client;

import android.content.Context;
import android.support.annotation.NonNull;

import com.nerdgeeks.cinetodo.browse.config.Config;
import com.nerdgeeks.cinetodo.browse.model.BrowseModel;
import com.nerdgeeks.cinetodo.browse.model.Movie;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by IMRAN on 9/14/2017.
 */

public class ApiInterfaceImpl {
    private final Context context;
    private final ApiInterfaceListener listener;

    public interface ApiInterfaceListener {
        void moviesReady(List<Movie> movies, int page);
        void onFailureSnackBar(String message);
    }

    public ApiInterfaceImpl(Context context, ApiInterfaceListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void getMovies(final int page){
        ApiClient
            .getClient()
            .getResults(Config.ADDITIONAL_URL + "" + page + "&limit=50")
            .enqueue(new Callback<BrowseModel>() {
                @Override
                public void onResponse(@NonNull Call<BrowseModel> call, @NonNull Response<BrowseModel> response) {
                    BrowseModel result = response.body();

                    if (result != null) {
                        assert listener != null;
                        listener.moviesReady(result.getData().getMovies(), page);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BrowseModel> call, @NonNull Throwable t) {
                    assert listener != null;
                    listener.onFailureSnackBar(t.getMessage());
                }
            });
    }

    public void getMovies(final int page, String genre){
        ApiClient
            .getClient()
            .getResults(Config.ADDITIONAL_URL + "" + page + "&genre=" + genre + "&limit=50")
            .enqueue(new Callback<BrowseModel>() {
                @Override
                public void onResponse(@NonNull Call<BrowseModel> call, @NonNull Response<BrowseModel> response) {
                    BrowseModel result = response.body();

                    if (result != null) {
                        assert listener != null;
                        listener.moviesReady(result.getData().getMovies(), page);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BrowseModel> call, @NonNull Throwable t) {
                    String exception = t.getMessage().replaceAll("\"yts.ag\"","");
                    assert listener != null;
                    listener.onFailureSnackBar(exception);
                }
            });
    }

}
