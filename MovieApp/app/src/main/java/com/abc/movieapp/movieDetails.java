package com.abc.movieapp;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;

public class movieDetails implements Parcelable {
    String movieName = "";
    String movieDescription = "";
    String movieGenre = "";
    int ratings = 0;
    String imdbLink = "";
    double year = 0;

    public movieDetails(String name, String des, String genre, String imdb, int ratings, double year){
        this.movieName = name;
        this.movieDescription = des;
        this.movieGenre = genre;
        this.imdbLink = imdb;
        this.ratings = ratings;
        this.year = year;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(movieName);
        dest.writeString(movieDescription);
        dest.writeString(movieGenre);
        dest.writeString(imdbLink);
        dest.writeInt(ratings);
        dest.writeDouble(year);

    }

    public static final Parcelable.Creator<movieDetails> CREATOR
            = new Parcelable.Creator<movieDetails>() {
        public movieDetails createFromParcel(Parcel in) {
            return new movieDetails(in);
        }

        public movieDetails[] newArray(int size) {
            return new movieDetails[size];
        }
    };

    private movieDetails(Parcel in){
        this.movieName = in.readString();
        this.movieDescription = in.readString();
        this.movieGenre = in.readString();
        this.imdbLink = in.readString();
        this.ratings = in.readInt();
        this.year = in.readDouble();

    }



    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getMovieDescription() {
        return movieDescription;
    }

    public void setMovieDescription(String movieDescription) {
        this.movieDescription = movieDescription;
    }

    public String getMovieGenre() {
        return movieGenre;
    }

    public void setMovieGenre(String movieGenre) {
        this.movieGenre = movieGenre;
    }

    public int getRatings() {
        return ratings;
    }

    public void setRatings(int ratings) {
        this.ratings = ratings;
    }

    public String getImdbLink() {
        return imdbLink;
    }

    public void setImdbLink(String imdbLink) {
        this.imdbLink = imdbLink;
    }

    public double getYear() {
        return year;
    }

    public void setYear(double year) {
        this.year = year;
    }
}
