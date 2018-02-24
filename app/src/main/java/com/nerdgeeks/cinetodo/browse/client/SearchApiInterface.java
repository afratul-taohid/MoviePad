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
 * Created by IMRAN on 9/17/2017.
 */

public class SearchApiInterface {

    private final Context context;
    private final SearchResultListener searchListener;

    public SearchApiInterface(Context context, SearchResultListener searchListener) {
        this.context = context;
        this.searchListener = searchListener;
    }

    public interface SearchResultListener {
        void getResult(List<Movie> movies);
        void onFailureSnackBar(String message);
    }

    public void getSearchResult(final String name){
        ApiClient
                .getClient()
                .getResults(Config.QUERY_URL + "" + name)
                .enqueue(new Callback<BrowseModel>() {
                    @Override
                    public void onResponse(@NonNull Call<BrowseModel> call, @NonNull Response<BrowseModel> response) {
                        BrowseModel result = response.body();

                        assert result != null;
                        if (result.getData().getMovieCount()>0) {
                            assert searchListener != null;
                            searchListener.getResult(result.getData().getMovies());
                        } else {
                            String message = "Sorry! "+ name +" not available";
                            searchListener.onFailureSnackBar(message);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BrowseModel> call, @NonNull Throwable t) {
                        String exception = t.getMessage().replaceAll("\"yts.ag\"","");
                        assert searchListener != null;
                        searchListener.onFailureSnackBar(exception);
                    }
                });
    }
}
