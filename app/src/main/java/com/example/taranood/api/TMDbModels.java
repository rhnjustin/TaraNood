package com.example.taranood.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TMDbModels {

    public static class SearchResponse {
        public List<SearchResult> results;
    }

    public static class SearchResult {
        public int id;
        @SerializedName("media_type")
        public String mediaType; // movie or tv
        public String title; // for movies
        @SerializedName("name")
        public String name; // for series
        @SerializedName("poster_path")
        public String posterPath;
        @SerializedName("release_date")
        public String releaseDate;
        @SerializedName("first_air_date")
        public String firstAirDate;
        public String overview;
        @SerializedName("vote_average")
        public double voteAverage;
    }

    public static class MovieDetails {
        public int id;
        public String title;
        @SerializedName("poster_path")
        public String posterPath;
        public int runtime;
        public String overview;
        @SerializedName("release_date")
        public String releaseDate;
    }

    public static class TVDetails {
        public int id;
        public String name;
        @SerializedName("poster_path")
        public String posterPath;
        @SerializedName("number_of_episodes")
        public int numberOfEpisodes;
        public String overview;
        @SerializedName("first_air_date")
        public String firstAirDate;
    }
}
