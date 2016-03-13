package com.example.fanchaozhou.moview;

import java.io.Serializable;

/**
 * Created by Fanchao Zhou on 3/12/2016.
 */
public class MovieItem implements Serializable{
    private String title;
    private String posterURL;
    private String plot;
    private String releaseDate;
    private int rating;
    private int popularity;

    public MovieItem(int popularity, int rating, String title, String posterURL, String plot, String releaseDate){
        this.popularity = popularity;
        this.rating = rating;
        this.title = title;
        this.posterURL = posterURL;
        this.plot = plot;
        this.releaseDate = releaseDate;
    }

    public int getPopularity(){ return popularity; }

    public int getRating(){ return rating; }

    public String getTitle(){ return title; }

    public String getPosterURL(){ return posterURL; }

    public String getPlot(){ return plot; }

    public String getReleaseDate(){ return releaseDate; }
}
