package edu.uncc.weatherapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements GetLocationDetailsAsyncTask.IGetActivity, GetCurrentCityWeatherAsycTask.IGetActivity {
    Boolean isCurrentCitySet = false;
    CurrentLocationWeatherModel currentLocation = null;
    String cityName, countryName;
    LinearLayout layoutWithCurrentCity, layoutNoCurrentCity;
    Button btnSetCurrentCity, btnSearchCity;
    AlertDialog alertDialogChangeCity = null;
    SharedPreferences mSharedPreference;
    public static final String Current_City_Code = "current_city";
    public static final String Current_Country_Code = "current_country";
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    RecyclerView savedCitiesRecyclerView;
    ArrayList<CityDetails> savedCitiesList = new ArrayList<>();
    TextView txtNoSavedCities;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreference = getSharedPreferences(PreferencesActivity.PREFS_NAME, Context.MODE_PRIVATE);
        layoutWithCurrentCity = (LinearLayout) findViewById(R.id.layoutWithCurrentCity);
        layoutNoCurrentCity = (LinearLayout) findViewById(R.id.layoutNoCurrentCity);
        btnSetCurrentCity = (Button) findViewById(R.id.btnSetCurrentCity);
        btnSearchCity = (Button) findViewById(R.id.btnSearchCity);
        txtNoSavedCities=(TextView)findViewById(R.id.txtNoSavedCities);
        if (CheckIfCurrentCitySet()) {
            cityName = (mSharedPreference.getString(PreferencesActivity.Current_City_Code, null));
            countryName = (mSharedPreference.getString(PreferencesActivity.Current_Country_Code, null));
            new GetLocationDetailsAsyncTask(MainActivity.this).execute(cityName, countryName);
        } else {
            layoutNoCurrentCity.setVisibility(View.VISIBLE);
            layoutWithCurrentCity.setVisibility(View.GONE);
        }
        savedCitiesList = new ArrayList<>();
        GetSavedCitiesFromDb();
        btnSetCurrentCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(MainActivity.this);
                View changeCityPromptView = li.inflate(R.layout.change_city_prompt, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setView(changeCityPromptView);
                final EditText userCity = (EditText) changeCityPromptView.findViewById(R.id.txtChangeCity);
                final EditText userCountry = (EditText) changeCityPromptView.findViewById(R.id.txtChangeCountry);
                final Button btnSetCity = (Button) changeCityPromptView.findViewById(R.id.btnSetCity);
                final Button btnCancelCity = (Button) changeCityPromptView.findViewById(R.id.btnCancelCity);
                // set dialog message
                alertDialogBuilder.setCancelable(false);
                alertDialogChangeCity = alertDialogBuilder.create();
                btnSetCity.setText("Set");
                alertDialogChangeCity.setTitle("Enter city details");
                alertDialogChangeCity.show();
                btnCancelCity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialogChangeCity.dismiss();
                    }
                });
                btnSetCity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cityName = userCity.getText().toString();
                        countryName = userCountry.getText().toString();
                        //code to add the city to Shared Preferences
                        SharedPreferences.Editor editor = mSharedPreference.edit();
                        editor.putString(Current_City_Code, cityName);
                        editor.putString(Current_Country_Code, countryName);
                        editor.commit();
                        alertDialogChangeCity.dismiss();
                        new GetLocationDetailsAsyncTask(MainActivity.this).execute(cityName, countryName);
                    }
                });
            }
        });
        btnSearchCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CityWeatherActivity.class);
                TextView txtSearchedCityName = (TextView) findViewById(R.id.txtSearchedCityName);
                TextView txtSearchedCountry = (TextView) findViewById(R.id.txtSearchedCountry);
                String city = txtSearchedCityName.getText().toString();
                String country = txtSearchedCountry.getText().toString();
                if (city != "" && country != "") {
                    intent.putExtra("CityName", txtSearchedCityName.getText());
                    intent.putExtra("Country", txtSearchedCountry.getText());
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Enter City and Country to search", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void GetSavedCitiesFromDb() {
        //Write Code to Read from FireBase database to see if any saved cities found.
        //Create an ArrayList and pass it to the method to bind to recycler view adapter.
        //savedCitiesList
        final boolean[] returnValue = {false};
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        savedCitiesRecyclerView = (RecyclerView) findViewById(R.id.savedCitiesRecyclerView);
// Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                savedCitiesList.clear();
                savedCitiesRecyclerView.removeAllViews();

                HashMap mapCities = (HashMap) dataSnapshot.getValue();
                HashMap citiesMap = new HashMap();
                if (mapCities != null) {
                    citiesMap = (HashMap) mapCities.get("citydetails");

                    if (citiesMap != null) {
                        for (Object key : citiesMap.keySet()) {
                            HashMap cityMap = (HashMap) citiesMap.get((String) key);

                            CityDetails city = new CityDetails();
                            city.setCityName((String) cityMap.get("cityName"));
                            if (cityMap != null && cityMap.get("favourite") != null && cityMap.get("favourite").equals("Yes")){
                                city.setFavourite(true);
                            }
                            else {
                                city.setFavourite(false);
                            }
                            city.setCountry((String) cityMap.get("country"));
                            city.setCityKey((String) cityMap.get("cityKey"));
//                             city.setLastUpdated((String) cityMap.get("lastUpdated"));
//                    HashMap<String, String> metricTemperatureMap = (HashMap<String, String>) cityMap.get("metricTemperature");
//
//                    Integer metricTemperatureValue = Integer.parseInt(String.valueOf(metricTemperatureMap.get("value")));
//                    String metricTemperatureUnit = metricTemperatureMap.get("unit");
//                    Integer metricTemperatureUnitType = Integer.parseInt(String.valueOf(metricTemperatureMap.get("unitType")));
//
//                    MetricTemperature metricTemperature = new MetricTemperature(metricTemperatureValue, metricTemperatureUnit, metricTemperatureUnitType);
//                    city.setMetricTemperature(metricTemperature);
                            savedCitiesList.add(city);
                        }
                    }
                }
                if (savedCitiesList.size() != 0) {
                    createRecyclerViewDisplay();
//                    cityList.setVisibility(View.INVISIBLE);
//                    noCitiesToDisplay.setVisibility(View.VISIBLE);
                }

              //  mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    private Boolean CheckIfCurrentCitySet() {
        String city = (mSharedPreference.getString(PreferencesActivity.Current_City_Code, null));
        String country = (mSharedPreference.getString(PreferencesActivity.Current_Country_Code, null));
        if (city != null && country != null && city != "" && country != "") {
            return true;
        } else return false;
    }

    private boolean isConnectedOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intentCart = new Intent(MainActivity.this, PreferencesActivity.class);
                startActivity(intentCart);
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void SetUpLocationData(CurrentLocationWeatherModel currentLocationWeatherModel) {
        if (currentLocationWeatherModel != null) {
            new GetCurrentCityWeatherAsycTask(MainActivity.this).execute(currentLocationWeatherModel);
            Toast.makeText(MainActivity.this, "Current city details saved.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, "City not found.", Toast.LENGTH_LONG).show();
            layoutWithCurrentCity.setVisibility(View.GONE);
            layoutNoCurrentCity.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void SetUpCurrentCityWeatherData(CurrentLocationWeatherModel currentLocationWeatherModel) {
        if (currentLocationWeatherModel != null) {
            TextView txtCityCountry = (TextView) findViewById(R.id.txtCityCountry);
            txtCityCountry.setText(cityName + ", " + countryName);
            TextView txtWeatherText = (TextView) findViewById(R.id.txtWeatherText);
            txtWeatherText.setText(currentLocationWeatherModel.getWeatherText());
            ImageView imgWeatherIcon = (ImageView) findViewById(R.id.imgWeatherIcon);
            Picasso.with(MainActivity.this)
                    .load(currentLocationWeatherModel.getWeatherIconUrl())
                    .resize(300, 300)
                    .into(imgWeatherIcon);
            TextView txtTemperature = (TextView) findViewById(R.id.txtTemperature);
            txtTemperature.setText(currentLocationWeatherModel.getCelsiusTemperature());
            TextView txtTempUpdated = (TextView) findViewById(R.id.txtTempUpdated);
            txtTempUpdated.setText(currentLocationWeatherModel.getLocalDateTime());
            layoutNoCurrentCity.setVisibility(View.GONE);
            layoutWithCurrentCity.setVisibility(View.VISIBLE);
            //hide progress bar here.
        }

    }

    public void createRecyclerViewDisplay() {
        //List view style
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        savedCitiesRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SavedCitiesRecyclerViewAdapter(savedCitiesList, MainActivity.this);
        savedCitiesRecyclerView.setAdapter(mAdapter);
        savedCitiesRecyclerView.setVisibility(View.VISIBLE);
        txtNoSavedCities.setVisibility(View.GONE);
        mAdapter.notifyDataSetChanged();
    }
}
