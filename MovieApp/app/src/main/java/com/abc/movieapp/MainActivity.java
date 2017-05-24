package com.abc.movieapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnClickListener{
    Button addMovieButton = null, editButton = null, deleteButton = null, showYearButton = null, showRatingButton = null;
    ArrayList<movieDetails> m_List = new ArrayList<movieDetails>();
    final static int REQ_CODE_ADD = 100, REQ_CODE_EDIT = 200;
    final static String RESULT_KEY = "result";
    String movieSelected = "";
    final static String MOVIE_DETAIL = "Movie_Selected";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( requestCode == REQ_CODE_ADD){
            if(resultCode == RESULT_OK ){
                Toast.makeText(this,data.getExtras().getString(RESULT_KEY), Toast.LENGTH_LONG);
                movieDetails movie = (movieDetails)data.getExtras().getParcelable(RESULT_KEY);
                m_List.add(movie);
            }
        }
        else if (requestCode == REQ_CODE_EDIT){
            if(resultCode == RESULT_OK ){
                Toast.makeText(this,data.getExtras().getString(RESULT_KEY), Toast.LENGTH_LONG);
                movieDetails movie = (movieDetails)data.getExtras().getParcelable(RESULT_KEY);
                int index = 0;
                for (int i = 0; i < m_List.size(); i++){
                    if(m_List.get(i).getMovieName() == movie.getMovieName()){
                        index = i;
                    }
                }
                m_List.set(index, movie);
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addMovieButton = (Button) findViewById(R.id.buttonAddMovie);
        addMovieButton.setOnClickListener(this);
        editButton = (Button) findViewById(R.id.buttonEdit);
        editButton.setOnClickListener(this);
        deleteButton = (Button) findViewById(R.id.buttonDeleteMovie);
        deleteButton.setOnClickListener(this);
        showYearButton = (Button) findViewById(R.id.buttonShowListByYear);
        showYearButton.setOnClickListener(this);
        showRatingButton = (Button) findViewById(R.id.buttonShowListByRating);
        showRatingButton.setOnClickListener(this);
    }

    public void onClick(View view) {
        if(view.getId() == addMovieButton.getId()){
            Intent addI = new Intent(getBaseContext(),add_activity.class);
            startActivityForResult(addI, REQ_CODE_ADD);
        }
        else if(view.getId() == editButton.getId()){
            if(m_List.size() > 0) {
                ArrayList<String> movieNames = new ArrayList<>();
                Log.d("demo", "123 " + movieSelected);
                for (movieDetails detail : m_List) {
                    if (!m_List.isEmpty()) {
                        movieNames.add(detail.getMovieName());
                    }
                }
                final CharSequence[] items = movieNames.toArray(new CharSequence[movieNames.size()]);
                Log.d("demo", "456 " + movieSelected);
                AlertDialog.Builder movieBuilder = new AlertDialog.Builder(this);
                movieBuilder.setTitle(R.string.pack_label);
                movieBuilder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        movieSelected = items[which].toString();
                        Intent editI = new Intent(getBaseContext(), Edit_Activity.class);
                        for (movieDetails detail : m_List) {
                            if(detail.getMovieName() == movieSelected){
                                editI.putExtra(MOVIE_DETAIL, detail);
                            }
                        }
                        startActivityForResult(editI, REQ_CODE_EDIT);
                    }
                });
                movieBuilder.show();



            }
            else {
                Toast.makeText(getApplicationContext(), "No Movies added.", Toast.LENGTH_LONG).show();
            }
        }
        else if(view.getId() == deleteButton.getId()){
            if(m_List.size() > 0) {
                ArrayList<String> movieNames = new ArrayList<>();
                for (movieDetails detail : m_List) {
                    if (!m_List.isEmpty()) {
                        movieNames.add(detail.getMovieName());
                    }
                }
                final CharSequence[] items = movieNames.toArray(new CharSequence[movieNames.size()]);
                AlertDialog.Builder movieBuilder = new AlertDialog.Builder(this);
                movieBuilder.setTitle(R.string.pack_label);
                movieBuilder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        movieSelected = items[which].toString();
                        int index = 0;
                        for (int i = 0; i < m_List.size(); i++){
                            if(m_List.get(i).getMovieName() == movieSelected){
                                index = i;
                            }
                        }
                        m_List.remove(index);
                    }
                });
                movieBuilder.show();
            }
            else {
                Toast.makeText(getApplicationContext(), "No Movies added.", Toast.LENGTH_LONG).show();
            }
        }
        else if(view.getId() == showYearButton.getId()){

        }
        else if(view.getId() == showRatingButton.getId()){

        }

    }

}
