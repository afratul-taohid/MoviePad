package com.nerdgeeks.cinetodo.todolist.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nerdgeeks.cinetodo.R;
import com.nerdgeeks.cinetodo.todolist.database.DbHelper;

import java.util.ArrayList;

/**
 * Created by IMRAN on 9/14/2017.
 */

public class MovieAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final ArrayList<String> values;
    private DbHelper dbHelper;

    private static class ViewHolder {
        TextView item_title;
        ImageView item_checked;
    }

    public MovieAdapter(@NonNull Activity context, ArrayList<String> values) {
        super(context, R.layout.item_row, values);
        this.context = context;
        this.values = values;
        dbHelper = new DbHelper(context);
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rootView = convertView;

        if (rootView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            rootView = inflater.inflate(R.layout.item_row, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.item_title = (TextView) rootView.findViewById(R.id.movie_title);
            viewHolder.item_checked = (ImageView) rootView.findViewById(R.id.check_box);
            rootView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rootView.getTag();
        String title = values.get(position);
        holder.item_title.setText(title);

        ArrayList<String> checkList = dbHelper.getCheckList();
        ArrayList<String> imgList = dbHelper.getImageList();

        if (checkList.get(position).equals("false")){
            if (imgList.get(position).isEmpty()){
                holder.item_checked.setBackgroundResource(R.drawable.ic_movie_dark);
                holder.item_checked.setImageBitmap(null);
            } else {
                byte[] imageAsBytes = Base64.decode(imgList.get(position).getBytes(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
                holder.item_checked.setImageBitmap(bitmap);
                holder.item_checked.setBackgroundResource(android.R.color.transparent);
            }
        } else {
            holder.item_checked.setBackgroundResource(R.drawable.ic_check);
            holder.item_checked.setImageBitmap(null);
        }
        return rootView;
    }
}
