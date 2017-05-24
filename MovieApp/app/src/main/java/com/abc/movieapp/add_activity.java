package com.abc.movieapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class add_activity extends AppCompatActivity {
    EditText nameEditText, imdbEditText, descriptionEditText, yearEditText;
    SeekBar ratingSeekBar;
    int seekBarProgress = 0;
    Spinner genreSpinner;
    String genre = "";

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_activity);
        Button click;
        nameEditText = (EditText) findViewById(R.id.editTextName);
        InputFilter[] filtersName = new InputFilter[1];
        filtersName[0] = new InputFilter.LengthFilter(50);
        nameEditText.setFilters(filtersName);

        imdbEditText = (EditText) findViewById(R.id.editTextIMDB);

        genreSpinner = (Spinner)findViewById(R.id.spinnerGenre);
        descriptionEditText = (EditText) findViewById(R.id.editTextDes);
        InputFilter[] filterDes = new InputFilter[1];
        filterDes[0] = new InputFilter.LengthFilter(1000);
        descriptionEditText.setFilters(filterDes);

        yearEditText = (EditText)findViewById(R.id.editTextYear);
        ratingSeekBar = (SeekBar) findViewById(R.id.seekBarRating);



        click = (Button) findViewById(R.id.buttonAddMovie);
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {


                    String movieName = nameEditText.getText().toString();
                    String movieDes = descriptionEditText.getText().toString();
                    int year = Integer.parseInt(yearEditText.getText().toString());
                    if(year <= 1900 || year >= 2018){
                        throw new NumberFormatException();
                    }
                    String imdb = imdbEditText.getText().toString();
                    movieDetails newMovie = new movieDetails(movieName, movieDes, genre, imdb, seekBarProgress, year);

                    Intent i = new Intent();
                    i.putExtra(MainActivity.RESULT_KEY, newMovie);
                    setResult(RESULT_OK, i);
                    finish();
                }
                catch (NumberFormatException e){
                    Toast.makeText(getApplicationContext(), "Invalid input.", Toast.LENGTH_LONG).show();
                }
                catch (NullPointerException e){
                    Toast.makeText(getApplicationContext(),"Null pointer",Toast.LENGTH_LONG).show();
                }
            }
        });


        ratingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarProgress = progress;
                ((TextView)findViewById(R.id.textViewSeekRating)).setText(String.valueOf(progress));
                Log.d("demo", "progress"+seekBarProgress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("demo", "onStartTrackingTouch"+seekBarProgress);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("demo", "onStopTrackingTouch"+seekBarProgress);
            }
        });

        ArrayAdapter<CharSequence> genreAdapter = ArrayAdapter.createFromResource(this, R.array.genre_labels, R.layout.support_simple_spinner_dropdown_item);
        genreAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        genreSpinner.setAdapter(genreAdapter);
        genreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                genre = parent.getItemAtPosition(position).toString();
                Log.d("demo", "genre"+genre);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    protected void onStart(Bundle savedInstanceState) {
        super.onStart();

    }

}
