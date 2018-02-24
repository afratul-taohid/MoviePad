package com.nerdgeeks.cinetodo.browse.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nerdgeeks.cinetodo.R;
import com.nerdgeeks.cinetodo.browse.model.Movie;
import com.nerdgeeks.cinetodo.browse.ui.YoutubePlayerFragment;
import com.nerdgeeks.cinetodo.todolist.database.DbHelper;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static android.support.v4.app.ActivityCompat.requestPermissions;
import static android.support.v4.app.ActivityCompat.shouldShowRequestPermissionRationale;

/**
 * Created by IMRAN on 9/14/2017.
 */

public class BrowseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private List<Movie> models;
    private ProgressBar pb;
    private int downloadedSize = 0;
    private int totalSize = 0;
    private TextView cur_val;
    private float per;
    private String filename;

    public BrowseAdapter(Activity activity, List<Movie> models) {
        this.activity = activity;
        this.models = models;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_row,parent,false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        ViewHolder myHolder = (ViewHolder) holder;

        Picasso.with(activity)
                .load(models.get(position).getMediumCoverImage())
                .into(myHolder.thumbnail);



        final int id = models.get(position).getId();

        final String movieName = models.get(position).getTitle();
        myHolder.name.setText(movieName);

        final String year = String.valueOf(models.get(position).getYear());
        myHolder.year.setText(year);

        int item = 0;
        StringBuilder movieGenre = new StringBuilder();
        for (String genre : models.get(position).getGenres()){
            if (item < models.get(position).getGenres().size()-1){
                movieGenre.append(genre + ", ");
                item++;
            } else {
                movieGenre.append(genre);
                item = 0;
            }
        }
        myHolder.genre.setText(movieGenre);

        myHolder.rating.setText(String.valueOf(models.get(position).getRating()));

        myHolder.addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DbHelper dbHelper = new DbHelper(activity);
                if (!dbHelper.isDataExists(id)){
                    dbHelper.insertNewMovie(id, movieName, "false", getEncodedImage(holder));
                    Toast.makeText(activity, "Added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "Already Added", Toast.LENGTH_SHORT).show();
                }
            }
        });

        myHolder.playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String videoId = models.get(holder.getAdapterPosition()).getYtTrailerCode();
                if (!videoId.isEmpty()){
                    Intent intent = new Intent(activity, YoutubePlayerFragment.class);
                    intent.putExtra("VIDEO_ID", videoId);
                    activity.startActivity(intent);
                }
            }
        });

        myHolder.downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isStoragePermissionGranted()){
                    final String url = models.get(holder.getAdapterPosition()).getTorrents().get(0).getUrl();
                    showProgress(url, movieName);
                    new Thread(new Runnable() {
                        public void run() {
                            downloadFile(url, movieName, year);
                        }
                    }).start();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //This is called if user has denied the permission before
                        //In this case I am just asking the permission again
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                    } else {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                    }
                }

            }
        });
    }



    private void downloadFile(String path, String movieName, String year){
        try {
            URL url = new URL(path);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);

            //connect
            urlConnection.connect();

            //set the path where we want to save the file
            File SDCardRoot = Environment.getExternalStorageDirectory();

            //create a new file, to save the downloaded file
            filename = movieName+"(" + year + ")[YTS.AG]" + ".torrent";
            File file = new File(SDCardRoot, filename);

            FileOutputStream fileOutput = new FileOutputStream(file);

            //Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            //this is the total size of the file which we are downloading
            totalSize = urlConnection.getContentLength();

            activity.runOnUiThread(new Runnable() {
                public void run() {
                    pb.setMax(totalSize);
                }
            });

            //create a buffer...
            byte[] buffer = new byte[1024];
            int bufferLength = 0;

            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                // update the progressbar //
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        pb.setProgress(downloadedSize);
                        per = ((float)downloadedSize/totalSize) * 100;
                        cur_val.setText("Downloaded " + downloadedSize + "KB / " + totalSize + "KB (" + per + "%)" );
                    }
                });
            }
            //close the output stream when complete //
            fileOutput.close();
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    per = 0;
                    downloadedSize = 0;
                    totalSize = 0;
                }
            });

        } catch (final MalformedURLException e) {
            showError("Error : MalformedURLException " + e);
            e.printStackTrace();
        } catch (final IOException e) {
            showError("Error : IOException " + e);
            e.printStackTrace();
        }
        catch (final Exception e) {
            showError("Error : Please check your internet connection " + e);
        }
    }

    private void showError(final String s) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showProgress(String url, String movieName) {
        MaterialDialog.Builder builder  =new MaterialDialog.Builder(activity)
                .title(movieName)
                .customView(R.layout.myprogressdialog, true)
                .backgroundColor(activity.getResources().getColor(R.color.colorPrimary))
                .titleColorRes(android.R.color.white)
                .positiveText("OPEN")
                .icon(activity.getResources().getDrawable(R.mipmap.ic_launcher))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        openDownloadedFileFromStorage();
                    }
                });

        MaterialDialog dialog = builder.build();
        TextView text = (TextView) dialog.findViewById(R.id.tv1);
        text.setText("Downloading file from ... " + url);
        cur_val = (TextView) dialog.findViewById(R.id.cur_pg_tv);
        cur_val.setText("Starting download...");
        dialog.show();

        pb = (ProgressBar) dialog.findViewById(R.id.progress_bar);
        pb.setProgress(0);
        pb.setProgressDrawable(activity.getResources().getDrawable(R.drawable.green_progress));
    }

    private void openDownloadedFileFromStorage() {
        if (filename!=null){
            File file = new File(Environment.getExternalStorageDirectory(),filename);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(activity, "com.nerdgeeks.cinetodo", file);
                intent.setDataAndType(contentUri, "application/x-bittorrent");
            } else {
                intent.setDataAndType(Uri.fromFile(file), "application/x-bittorrent");
            }

            try {
                activity.startActivity(intent);
            } catch (ActivityNotFoundException e){
                Toast.makeText(activity,"Sorry! please download a torrent client app", Toast.LENGTH_SHORT).show();
            }
        }
        Toast.makeText(activity,"Sorry! Torrent file not downloaded", Toast.LENGTH_SHORT).show();
    }

    private  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if(activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }


    private String getEncodedImage(RecyclerView.ViewHolder holder){
        ViewHolder myHolder = (ViewHolder) holder;
        BitmapDrawable drawable = (BitmapDrawable) myHolder.thumbnail.getDrawable();

        if (drawable != null){
            Bitmap bitmap = drawable.getBitmap();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.WEBP, 60, baos); //bm is the bitmap object
            byte[] b = baos.toByteArray();

            return Base64.encodeToString(b, Base64.DEFAULT);
        } else
            return "";
    }

    @Override
    public int getItemCount() {
        if (models != null){
            return models.size();
        } else
            return 0;
    }

    // Add a new item to the RecyclerView on a predefined position
    public void addNewItems(int position, Movie data) {
        models.add(position, data);
        notifyItemInserted(position);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView thumbnail, addTask, playBtn, downloadBtn;
        private TextView name, genre, rating, year;
        ViewHolder(View itemView) {
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            addTask = (ImageView) itemView.findViewById(R.id.add_task);
            playBtn = (ImageView) itemView.findViewById(R.id.play_tube);
            downloadBtn = (ImageView) itemView.findViewById(R.id.download);
            name = (TextView) itemView.findViewById(R.id.movie_name);
            genre = (TextView) itemView.findViewById(R.id.genre);
            rating = (TextView) itemView.findViewById(R.id.rating);
            year = (TextView) itemView.findViewById(R.id.year);
        }
    }
}
