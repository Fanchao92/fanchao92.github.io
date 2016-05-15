package com.example.fanchaozhou.moview;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public static final String MOVIEINFO = "MOVIE INFORMATION";

    private MovieItemAdapter movieAdapter = null;
    private ArrayList<MovieItem> movieList = null;
    private static final int posterWidth = 342;
    private static final String apiKey = /*The API Key should be acquired on the website of movieDB*/;
    private static final String resUrl = "http://api.themoviedb.org/3/discover/movie";
    private static final String posterUrl = "http://image.tmdb.org/t/p/w"+posterWidth;
    private static final String para_sort = "sort_by";
    private static final String para_api_key = "api_key";
    private static final String popUrl = "popularity.desc";
    private static final String rateUrl = "vote_average.desc";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null){
            movieList = new ArrayList<MovieItem>(0);
            movieAdapter = new MovieItemAdapter(getActivity(), R.layout.single_item, movieList);
            setHasOptionsMenu(true);   //Enable the menu inflation
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);

        if(savedInstanceState == null){
            //Get the GridView and Bind the adapter to it.
            GridView gridView = (GridView)rootView.findViewById(R.id.gridView);
            gridView.setAdapter(movieAdapter);

            //Initialize the handler for clicking
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Gson gson = new GsonBuilder().create();
                    String movieInfoStr;
                    movieInfoStr = gson.toJson(movieList.get(position), MovieItem.class);  //Serialize the obj. in JSON format

                    //// Start a new activity and Send the JSON string to it ////
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.putExtra(MOVIEINFO, movieInfoStr);
                    startActivity(intent);
                }
            });
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sorting = sharedPref.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));
        if(sorting.equals(getString(R.string.pref_sort_most_pop))){//Reading the Settings: Sort by most popular
            new GetMovieList().execute(popUrl);   //Get the movie list sorted by popularity
        } else if(sorting.equals(getString(R.string.pref_sort_top_rated))) {//Reading the Settings: Sort by ratings
            new GetMovieList().execute(rateUrl);  //Get the movie list sorted by ratings
        }
    }

    private class GetMovieList extends AsyncTask<String, Void, ArrayList<MovieItem>>{

        @Override
        protected ArrayList<MovieItem> doInBackground(String... params) {
            URL url;
            HttpURLConnection urlConnection;
            Uri buildUri = Uri.parse(resUrl).buildUpon().
                    appendQueryParameter(para_sort, params[ 0 ]).
                    appendQueryParameter(para_api_key, apiKey).
                    build();                    //Build the query URL
            String movieJsonStr = null;
            BufferedReader reader;
            try{
                // Set up the HTTP connection to movieDB
                url = new URL(buildUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line+'\n');
                }

                if (buffer.length() == 0) {
                    return null;
                }

                movieJsonStr = buffer.toString(); //Get the JSON string from movieDB
            } catch (Exception e){
                System.out.println(e);
            }

            ArrayList<MovieItem> mvItems = parseMovieJSON(movieJsonStr);  //Parse the JSON string

            return mvItems;
        }

        @Override
        protected void onPostExecute(ArrayList<MovieItem> mvItems) {
            int cnt;
            if(movieList != null){
                movieList.clear();
                for(cnt = 0; cnt < mvItems.size(); cnt++){
                    movieList.add(cnt, mvItems.get(cnt));   //Refresh the movie list
                }
            }

            movieAdapter.notifyDataSetChanged();   //Refresh the UI with all the new movie info.

            super.onPostExecute(mvItems);
        }

        private ArrayList<MovieItem> parseMovieJSON(String movieJSONStr){
            ArrayList<MovieItem> mvItems = new ArrayList<>(0);

            try{
                int cnt;
                int numMovies;
                JSONObject movieJSONObj = new JSONObject(movieJSONStr);
                JSONArray movieList = movieJSONObj.getJSONArray("results");
                numMovies = movieList.length();
                for(cnt = 0; cnt < numMovies; cnt++){
                    JSONObject record = movieList.getJSONObject(cnt);
                    int pop = (int) Math.round(record.getDouble("popularity"));
                    int rate = (int) Math.round(record.getDouble("vote_average")*10);
                    String title = record.getString("title");
                    String url = posterUrl+record.getString("poster_path");
                    String plot = record.getString("overview");
                    String releaseDate = record.getString("release_date");
                    mvItems.add(new MovieItem(pop, rate, title, url, plot, releaseDate));
                }

                return mvItems;
            } catch(Exception e) {
                System.out.println(e);
            }

            return null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {  //Switch to the settings page
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.addToBackStack(null);    //Pushing to the stack for BACK button to trace back to previous fragments
            transaction.replace(R.id.container,  new SettingsFragment());  //Start a settings fragment
            transaction.commit();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}