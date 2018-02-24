package com.nerdgeeks.cinetodo.todolist.ui;

import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.nerdgeeks.cinetodo.BuildConfig;
import com.nerdgeeks.cinetodo.browse.ui.BrowseActivity;
import com.nerdgeeks.cinetodo.todolist.database.DbHelper;
import com.nerdgeeks.cinetodo.todolist.adapter.MovieAdapter;
import com.nerdgeeks.cinetodo.R;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements
        ListView.OnItemClickListener, AddMovieFragment.AddNewMovie, AbsListView.MultiChoiceModeListener {

    private DbHelper dbHelper;
    private ArrayAdapter<String> adapter;
    private ListView movieList;
    private boolean isChecked = false;
    private ArrayList<String> toDelete;
    private VideoController videoController;
    private InterstitialAd interstitialAd;
    boolean exitApp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);


        final CardView cardView = (CardView) findViewById(R.id.card);
        final Animation slide_down = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_down);

        final NativeExpressAdView mAdView= new NativeExpressAdView(this);
        AdSize adSize = new AdSize(AdSize.FULL_WIDTH, 80);
        mAdView.setAdSize(adSize);
        mAdView.setAdUnitId(getString(R.string.native_ad));
        cardView.addView(mAdView);

        mAdView.setVideoOptions(new VideoOptions.Builder().setStartMuted(true).build());

        videoController= mAdView.getVideoController();
        videoController.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
            @Override
            public void onVideoEnd() {
                super.onVideoEnd();
            }
        });

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                cardView.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded() {
                cardView.setVisibility(View.VISIBLE);
                cardView.setAnimation(slide_down);
                movieList.setAnimation(slide_down);
            }
        });
        mAdView.loadAd(new AdRequest.Builder().build());


        dbHelper = new DbHelper(this);
        movieList = (ListView) findViewById(R.id.listMovie);

        movieList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        movieList.setMultiChoiceModeListener(this);
        movieList.setOnItemClickListener(this);

        toDelete = new ArrayList<>();
        loadMovieList();
    }

    private void loadMovieList() {
        ArrayList<String> arrayList = dbHelper.getMovieList();
        if (adapter==null){
            adapter = new MovieAdapter(this, arrayList);
            movieList.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.addAll(arrayList);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        Drawable icon = menu.getItem(0).getIcon();
        icon.mutate();
        icon.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_movie:
                DialogFragment dialog = AddMovieFragment.newInstance();
                dialog.show(MainActivity.this.getFragmentManager(), "AddMovieFragment");
                return true;
            case R.id.action_browse_movie:
                launchInter();
                loadInterstitial();
                startActivity(new Intent(this, BrowseActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                finish();
                return true;
            case R.id.action_rate:
                rateMyApp();
                return true;
            case R.id.action_about:
                aboutMyApp();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void setState(View view) {
        View rootView = (View) view.getParent();
        TextView item = (TextView) rootView.findViewById(R.id.movie_title);
        String title = item.getText().toString();
        if (!isChecked){
            dbHelper.updateCheckedRow(title, "false");
            isChecked = true;
            loadMovieList();
        }else {
            dbHelper.updateCheckedRow(title, "true");
            isChecked = false;
            loadMovieList();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String title = parent.getItemAtPosition(position).toString();
        if (!isChecked){
            dbHelper.updateCheckedRow(title, "false");
            isChecked = true;
            loadMovieList();
        }else {
            dbHelper.updateCheckedRow(title, "true");
            isChecked = false;
            loadMovieList();
        }
    }

    @Override
    public void AddNewTask(String title) {
        Random random = new Random();
        int id = random.nextInt(100000 - 20000) + 20000;
        dbHelper.insertNewMovie(id, title, "false", "");
        loadMovieList();
    }

    private void deleteMovieData(String item){
        dbHelper.deleteByEncodedTitle(item);
        loadMovieList();
    }

    @Override
    public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
        if (checked) {
            toDelete.add(adapter.getItem(position));
        } else {
            toDelete.remove(adapter.getItem(position));
        }
    }

    @Override
    public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.row_selection, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                for (String title : toDelete) {
                    deleteMovieData(title);
                }
                // Action picked, so close the CAB
                mode.finish();
                return true;
            default:
                return false;
        }
    }

    private void aboutMyApp() {

        MaterialDialog.Builder bulder = new MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .customView(R.layout.about, true)
                .backgroundColor(getResources().getColor(R.color.colorPrimary))
                .titleColorRes(android.R.color.white)
                .positiveText("MORE APPS")
                .icon(getResources().getDrawable(R.mipmap.ic_launcher))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Uri uri = Uri.parse("market://search?q=pub:" + "NerdGeeks");
                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        try {
                            startActivity(goToMarket);
                        } catch (ActivityNotFoundException e) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/search?q=pub:" + "NerdGeeks")));
                        }
                    }
                });

        MaterialDialog materialDialog = bulder.build();

        TextView versionCode = (TextView) materialDialog.findViewById(R.id.version_code);
        TextView versionName = (TextView) materialDialog.findViewById(R.id.version_name);
        versionCode.setText(String.valueOf("vCode : " +BuildConfig.VERSION_CODE));
        versionName.setText("vName : " + BuildConfig.VERSION_NAME);

        materialDialog.show();
    }

    private void rateMyApp() {
        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
        }
    }

    @Override
    public void onDestroyActionMode(android.view.ActionMode mode) {
        toDelete.clear();
    }

    public void launchInter(){
        interstitialAd =new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.Interstitial));

        //Set the adListener
        interstitialAd.setAdListener(new AdListener() {


            public void onAdLoaded() {
                showAdInter();
            }

            public void onAdFailedToLoad(int errorCode) {
                String message = String.format("onAdFailedToLoad(%s)", getErrorReason(errorCode));

            }

            @Override
            public void onAdClosed() {
                if (exitApp)
                    finish();
            }
        });

    }

    private void showAdInter(){

        if(interstitialAd.isLoaded()){
            interstitialAd.show();
        }
        else{
            Log.d("", "ad was not ready to shown");
        }

    }

    public void loadInterstitial(){

        AdRequest adRequest= new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("INSERT_YOUR_HASH_DEVICE_ID")
                .build();
        //Load this Interstitial ad
        interstitialAd.loadAd(adRequest);
    }

    //Get a string error
    private String getErrorReason(int errorCode){

        String errorReason="";
        switch(errorCode){
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                errorReason="Internal Error";
                break;
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                errorReason="Invalid Request";
                break;
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                errorReason="Network Error";
                break;
            case AdRequest.ERROR_CODE_NO_FILL:
                errorReason="No Fill";
                break;
        }
        return errorReason;
    }


}
