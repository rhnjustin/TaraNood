package com.example.taranood.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TMDbService {
    String BASE_URL = "https://api.themoviedb.org/3/";
    String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    // ⚠️ IMPORTANT: Replace this with your own valid TMDb API key.
    // The search will fail with a JSON parse error if this key is invalid or expired.
    //
    // How to get a free API key:
    //   1. Go to https://www.themoviedb.org/signup and create a free account
    //   2. Go to Settings → API → Request an API Key → Developer
    //   3. Copy the "API Key (v3 auth)" value and paste it below
    //
    String API_KEY = "7302a4ed77541fda75035aab9fcde816"; // <-- Replace this!

    @GET("search/multi")
    Call<TMDbModels.SearchResponse> searchMulti(
            @Query("api_key") String apiKey,
            @Query("query") String query
    );

    @GET("movie/{movie_id}")
    Call<TMDbModels.MovieDetails> getMovieDetails(
            @Path("movie_id") int movieId,
            @Query("api_key") String apiKey
    );

    @GET("tv/{tv_id}")
    Call<TMDbModels.TVDetails> getTVDetails(
            @Path("tv_id") int tvId,
            @Query("api_key") String apiKey
    );
}