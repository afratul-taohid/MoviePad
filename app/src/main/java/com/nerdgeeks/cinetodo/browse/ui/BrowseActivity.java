package com.nerdgeeks.cinetodo.browse.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.clockbyte.admobadapter.expressads.AdmobExpressRecyclerAdapterWrapper;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.nerdgeeks.cinetodo.R;
import com.nerdgeeks.cinetodo.browse.adapter.BrowseAdapter;
import com.nerdgeeks.cinetodo.browse.client.ApiInterfaceImpl;
import com.nerdgeeks.cinetodo.browse.client.SearchApiInterface;
import com.nerdgeeks.cinetodo.browse.model.Movie;
import com.nerdgeeks.cinetodo.browse.utils.EndlessRecyclerViewScrollListener;
import com.nerdgeeks.cinetodo.todolist.ui.MainActivity;

import android.support.design.widget.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BrowseActivity extends AppCompatActivity implements
        ApiInterfaceImpl.ApiInterfaceListener, SearchView.OnQueryTextListener, SearchApiInterface.SearchResultListener {

    private RecyclerView browseListView;
    private CoordinatorLayout parentView;
    private BrowseAdapter adapter;
    private ApiInterfaceImpl apiInterface;
    private SearchApiInterface searchApiInterface;
    private List<Movie> allMovies;
    private ProgressBar mLoader;
    private int FLAG = 0;
    private String genre;
    private SearchView searchView;
    AdmobExpressRecyclerAdapterWrapper adapterWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Browse Movies");
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        browseListView = (RecyclerView) findViewById(R.id.browseList);
        parentView = (CoordinatorLayout) findViewById(R.id.browseActivity);
        mLoader = (ProgressBar) findViewById(R.id.load_progress);

        mLoader.setVisibility(View.VISIBLE);

        apiInterface = new ApiInterfaceImpl(this, this);
        apiInterface.getMovies(1);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        browseListView.setLayoutManager(layoutManager);

        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                mLoader.setVisibility(View.VISIBLE);
                int next_page = page + 1;
                if (FLAG==0){
                    apiInterface.getMovies(next_page);
                } else {
                    apiInterface.getMovies(next_page, genre);
                }

                view.post(new Runnable() {
                    @Override
                    public void run() {
                        adapterWrapper.notifyItemRangeChanged(adapterWrapper.getItemCount(),allMovies.size()-1);
                    }
                });
            }
        };
        // Adds the scroll listener to RecyclerView
        browseListView.addOnScrollListener(scrollListener);
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        finish();
        return true;
    }

    @Override
    public void getResult(List<Movie> movies) {
        if (allMovies != null){
            allMovies.clear();
            this.allMovies = movies;
        }
        adapter = new BrowseAdapter(this, allMovies);
        createAdapterWithNativeAds(adapter);
    }

    @Override
    public void onFailureSnackBar(String message) {
        int color = Color.RED;
        int TIME_OUT = Snackbar.LENGTH_INDEFINITE;

        Snackbar snackbar = Snackbar
                .make(parentView, message, TIME_OUT);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.setAction("CLOSE", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        snackbar.show();
        mLoader.setVisibility(View.INVISIBLE);
    }

    @Override
    public void moviesReady(List<Movie> movies, int page) {
        if (page>1){
            this.allMovies.addAll(movies);
            adapter.notifyDataSetChanged();
        } else {
            if (allMovies != null){
                allMovies.clear();
            }
            this.allMovies = movies;
            adapter = new BrowseAdapter(this, allMovies);
            createAdapterWithNativeAds(adapter);
        }
        mLoader.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()){
            searchView.onActionViewCollapsed();
        } else {
            super.onBackPressed();
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browse_menu, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action:
                FLAG =1;
                genre = "action";
                setListByGenre(genre);
                return true;

            case R.id.adventure:
                FLAG =1;
                genre = "adventure";
                setListByGenre(genre);
                return true;

            case R.id.animation:
                FLAG =1;
                genre = "animation";
                setListByGenre(genre);
                return true;

            case R.id.comedy:
                FLAG =1;
                genre = "comedy";
                setListByGenre(genre);
                return true;

            case R.id.drama:
                FLAG =1;
                genre = "drama";
                setListByGenre(genre);
                return true;

            case R.id.doc:
                FLAG =1;
                genre = "horror";
                setListByGenre(genre);
                return true;

            case R.id.mystery:
                FLAG =1;
                genre = "mystery";
                setListByGenre(genre);
                return true;

            case R.id.romance:
                FLAG =1;
                genre = "romance";
                setListByGenre(genre);
                return true;

            case R.id.crime:
                FLAG =1;
                genre = "sci-fi";
                setListByGenre(genre);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setListByGenre(String genre) {
        mLoader.setVisibility(View.VISIBLE);
        apiInterface = new ApiInterfaceImpl(this, this);
        apiInterface.getMovies(1,genre);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchApiInterface = new SearchApiInterface(this,this);
        searchApiInterface.getSearchResult(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void createAdapterWithNativeAds(BrowseAdapter adapter) {
        //your test devices' ids
        //when you'll be ready for release please use another ctor with admobReleaseUnitId instead.
        String testDevicesIds=getResources().getString(R.string.native_ad);
        adapterWrapper = new AdmobExpressRecyclerAdapterWrapper(this, testDevicesIds,new AdSize(AdSize.FULL_WIDTH, 100)){
            @NonNull
            @Override
            protected ViewGroup getAdViewWrapper(ViewGroup parent) {
                return (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.native_express_ad_container,
                        parent, false);
            }
            @Override
            protected void recycleAdViewWrapper(@NonNull ViewGroup wrapper, @NotNull NativeExpressAdView ad) {
                //get the view which directly will contain ad
                ViewGroup container = (ViewGroup) wrapper.findViewById(R.id.ad_container);
                /**iterating through all children of the container view and remove the first occured {@link NativeExpressAdView}. It could be different with {@param ad}!!!*/
                for (int i = 0; i < container.getChildCount(); i++) {
                    View v = container.getChildAt(i);
                    if (v instanceof NativeExpressAdView) {
                        container.removeViewAt(i);
                        break;
                    }
                }
            }
            @Override
            protected void addAdViewToWrapper(@NonNull ViewGroup wrapper, @NotNull NativeExpressAdView ad) {
                //get the view which directly will contain ad
                ViewGroup container = (ViewGroup) wrapper.findViewById(R.id.ad_container);
                /**add the {@param ad} directly to the end of container*/
                container.addView(ad);
            }
        };
        adapterWrapper.setAdapter(adapter); //wrapping your adapter with a AdmobExpressRecyclerAdapterWrapper.
        adapterWrapper.setLimitOfAds(500);
        adapterWrapper.setNoOfDataBetweenAds(15);
        adapterWrapper.setFirstAdIndex(5);
        browseListView.setAdapter(adapterWrapper);
        adapterWrapper.notifyDataSetChanged();
    }


}
