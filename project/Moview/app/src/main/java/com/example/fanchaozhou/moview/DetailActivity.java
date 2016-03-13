package com.example.fanchaozhou.moview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String mvInfoStr;
        if(intent!=null && intent.hasExtra(MainActivityFragment.MOVIEINFO)) {
            mvInfoStr = intent.getStringExtra(MainActivityFragment.MOVIEINFO);
            Gson gson = new GsonBuilder().create();
            MovieItem mvItem = gson.fromJson(mvInfoStr, MovieItem.class);

            TextView titleTV = (TextView)findViewById(R.id.detail_scrollv_title);
            TextView rateTV = (TextView)findViewById(R.id.detail_scrollv_rate);
            TextView dateTV = (TextView)findViewById(R.id.detail_scrollv_release_date);
            TextView plotTV = (TextView)findViewById(R.id.detail_scrollv_plot);
            ImageView posterIV = (ImageView)findViewById(R.id.detail_scrollv_poster);

            titleTV.setText(mvItem.getTitle());
            rateTV.setText("Ratings: "+mvItem.getRating()+"/100");
            dateTV.setText("Release Date: "+mvItem.getReleaseDate());
            plotTV.setText(mvItem.getPlot());
            Picasso.with(this).load(mvItem.getPosterURL()).into(posterIV);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_movies){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
