package fastttrack.android.project.themovie2.UI;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import fastttrack.android.project.themovie2.Adapter.ReviewAdapter;
import fastttrack.android.project.themovie2.Adapter.TrailerAdapter;
import fastttrack.android.project.themovie2.BuildConfig;
import fastttrack.android.project.themovie2.MainActivity;
import fastttrack.android.project.themovie2.Model_Movie.Result;
import fastttrack.android.project.themovie2.Model_Reviews.Reviews;
import fastttrack.android.project.themovie2.Model_Trailer.Trailer;
import fastttrack.android.project.themovie2.R;
import fastttrack.android.project.themovie2.Utils.NetworkUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static fastttrack.android.project.themovie2.DB.Database.FavEntry.COLUMN_DATERELEASE;
import static fastttrack.android.project.themovie2.DB.Database.FavEntry.COLUMN_ISFAVORITE;
import static fastttrack.android.project.themovie2.DB.Database.FavEntry.COLUMN_POSTER;
import static fastttrack.android.project.themovie2.DB.Database.FavEntry.COLUMN_RATING;
import static fastttrack.android.project.themovie2.DB.Database.FavEntry.COLUMN_SYNOPSIS;
import static fastttrack.android.project.themovie2.DB.Database.FavEntry.COLUMN_TITLE;
import static fastttrack.android.project.themovie2.DB.Database.FavEntry.CONTENT_URI;
import static fastttrack.android.project.themovie2.DB.Database.FavEntry.COUMN_ID;

/**
 * Created by Fauziyyah Faturahma on 7/30/2017.
 */

//commit now

public class DetailMovie extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    //@BindView(R.id.reviewList)
    RecyclerView ReviewList;
    Result data;
    Button favorite;
    private ReviewAdapter reviewAdapter;
    private TrailerAdapter trailerAdapter;

    //listadapter review movie
    private List<fastttrack.android.project.themovie2.Model_Reviews.Result> reviewList = new ArrayList<>();

    //list adapter trailer moview
    private List<fastttrack.android.project.themovie2.Model_Trailer.Result> trailerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_movie);


        data = new GsonBuilder().create().fromJson(this.getIntent().getStringExtra("movie"), Result.class);

        TextView titleMovie = (TextView) findViewById(R.id.textTitleMovie);
        ImageView detailImage = (ImageView) findViewById(R.id.detailImage);
        TextView dateReleaseMovie = (TextView) findViewById(R.id.textDateRelease);
        TextView ratingMovie = (TextView) findViewById(R.id.textRatingMovie);
        TextView synopsisMovie = (TextView) findViewById(R.id.textDetailSynopsis);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.reviewList);
        RecyclerView recyclerViewTrailer = (RecyclerView)findViewById(R.id.trailerList) ;
        TextView textReview = (TextView) findViewById(R.id.textReview);
        favorite = (Button) findViewById(R.id.jadiFavorite);

        cekFavorite();

        titleMovie.setText(data.getTitle());
        dateReleaseMovie.setText(data.getReleaseDate());
        ratingMovie.setText(""+data.getVoteAverage());
        synopsisMovie.setText(data.getOverview());


        Glide.with(this)
                .load(RetrofitInterface.BASE_IMAGE + data.getPosterPath())
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .centerCrop()
                .into(detailImage);

        reviewAdapter = new ReviewAdapter(reviewList);  //review movie
        trailerAdapter = new TrailerAdapter(trailerList); //trailer moview

        recyclerViewTrailer.setAdapter(trailerAdapter); //trailer moview
        recyclerView.setAdapter(reviewAdapter); //review movie

        recyclerViewTrailer.setLayoutManager(new LinearLayoutManager(this)); //trailer moview
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); //review movie

        moviewReview(data.getId()); //review movie
        TrailerMovie(data.getId()); //trailer movie
    }


    private void cekFavorite(){

        String selection = COUMN_ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(data.getId())};
        Cursor cursor = getContentResolver().query(CONTENT_URI, null, selection, selectionArgs, null, null);
        if (cursor != null) {

            if(cursor.moveToFirst()){
                //Log.d("Detail Movie", "Fav Movie: " +cursor.getString(cursor.getColumnIndex(COLUMN_ISFAVORITE)));
                if(cursor.getString(cursor.getColumnIndex(COLUMN_ISFAVORITE)).equals("0")) {
                    favorite.setText("Mark as favorite");
                }
                else {
                    favorite.setText("Mark as un-favorite");
                }
            }
            cursor.close();
        }

    }
    //TODO - cek dulu movie_id udah ada blm di database, kalo udah ada jangan didave datanya tapi di apus, kalo belum ada baru di insert/save

    //ini metode onClick favorite movie, kalo di mark as favorite semua data movie yg bersangkutan akan di push ke db
    public void FavoriteMovie (View view){

            ContentValues contentValues = new ContentValues();
            contentValues.put(COUMN_ID, data.getId());
            contentValues.put(COLUMN_TITLE, data.getTitle());
            contentValues.put(COLUMN_POSTER, data.getPosterPath());
            contentValues.put(COLUMN_SYNOPSIS, data.getOverview());
            contentValues.put(COLUMN_RATING, data.getVoteAverage());
            contentValues.put(COLUMN_DATERELEASE, data.getReleaseDate());
            contentValues.put(COLUMN_ISFAVORITE, 1);

            //getContentResolver().update(CONTENT_URI, contentValues, COUMN_ID + "=?", new String[]{String.valueOf(data.getId())});

            //Toast.makeText(this, "Favorite Success", Toast.LENGTH_LONG).show();

            String message = "";
            String selection = COUMN_ID + "=?";
            String[] selectionArgs = new String[]{String.valueOf(data.getId())};
            Cursor cursor = getContentResolver().query(CONTENT_URI, null, selection, selectionArgs, null, null);
            if (cursor != null) {

                if(cursor.moveToFirst()){
                    //Log.d("Detail Movie", "Fav Movie: " +cursor.getString(cursor.getColumnIndex(COLUMN_ISFAVORITE)));
                    if(cursor.getString(cursor.getColumnIndex(COLUMN_ISFAVORITE)).equals("0")) {
                        message = "Favorite";
                        contentValues.put(COLUMN_ISFAVORITE, 1);
                    }
                    else {
                        message = "Un-Favorite";
                        contentValues.put(COLUMN_ISFAVORITE, 0);
                    }
                }
                cursor.close();
            }

            int update = getContentResolver().update(CONTENT_URI, contentValues, selection, selectionArgs);

            if (update!=0){
                Toast.makeText(this, message + "Success", Toast.LENGTH_LONG).show();
            }
             else{
                Toast.makeText(this, message + "Failed", Toast.LENGTH_LONG).show();
            }

        cekFavorite();

    }


    //showing moview reviews
    private void moviewReview(final int id) {
        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... params) {
                RetrofitInterface retrofitInterface = NetworkUtils.getRetrofit().create(RetrofitInterface.class);
                Call<Reviews> review = retrofitInterface.getMovieReview(String.valueOf(id), BuildConfig.MOVIE_API_KEY);
                review.enqueue(new Callback<Reviews>() {
                    @Override
                    public void onResponse(Call<Reviews> call, Response<Reviews> response) {
                        Log.d(MainActivity.class.getSimpleName(), "onResponse: ");
                        reviewList.clear();
                        reviewList.addAll(response.body().getResults());
                        reviewAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<Reviews> call, Throwable t) {
                        Log.e(MainActivity.class.getSimpleName(), "onFailure: ");
                    }
                });
                return "";
            }
        }.execute();
    }


    //showing trailer movie in detail activity

    private void TrailerMovie(final int id){

        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... params) {
                RetrofitInterface retrofitInterface = NetworkUtils.getRetrofit().create(RetrofitInterface.class);
                final Call<Trailer> trailer = retrofitInterface.getMovieTrailer(String.valueOf(id), BuildConfig.MOVIE_API_KEY);
                trailer.enqueue(new Callback<Trailer>() {
                    @Override
                    public void onResponse(Call<Trailer> call, final Response<Trailer> response) {
                        Log.d(MainActivity.class.getSimpleName(), "onResponse: ");

                        trailerList.clear();
                        trailerList.addAll(response.body().getResults());
                        trailerAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<Trailer> call, Throwable t) {
                        Log.e(MainActivity.class.getSimpleName(), "onFailure: ");
                    }
                });
                return "";
            }
        }.execute();
    }



    //back button to home
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {



        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
