package com.example.fanchaozhou.moview;

import android.support.annotation.LayoutRes;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Fanchao Zhou on 3/12/2016.
 */
public class MovieItemAdapter extends ArrayAdapter<MovieItem> {

    private Context mContext;
    private ArrayList<MovieItem>movieList;
    private @LayoutRes int layoutID;

    public MovieItemAdapter(Context mContext, @LayoutRes int layoutID, ArrayList<MovieItem>movieList){
        super(mContext, layoutID, movieList);

        this.movieList = movieList;
        this.mContext = mContext;
        this.layoutID = layoutID;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieItem mvItem = movieList.get(position);   //Get the current item in the source data set

        if(convertView == null){ //If this is a view with no layout before, then inflate the layout
            convertView = LayoutInflater.from(mContext).inflate(layoutID, parent, false);
        }

        //Set the poster of the movie
        ImageView iconView = (ImageView)convertView.findViewById(R.id.gridView_imageView);
        Picasso.with(mContext).load(mvItem.getPosterURL()).into(iconView);

        //Set the average count of the movie
        TextView ratingView = (TextView) convertView.findViewById(R.id.gridView_textView);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        if(sharedPref.getString(mContext.getString(R.string.pref_sort_key), mContext.getString(R.string.pref_sort_default)).
                equals(mContext.getString(R.string.pref_sort_most_pop))){
            String temp = "Popularity: "+mvItem.getPopularity()+"/100";
            ratingView.setText(temp);
        } else if(sharedPref.getString(mContext.getString(R.string.pref_sort_key), mContext.getString(R.string.pref_sort_default)).
                equals(mContext.getString(R.string.pref_sort_top_rated))){
            String temp = "Ratings: "+mvItem.getRating()+"/100";
            ratingView.setText(temp);
        }

        return convertView;
    }
}