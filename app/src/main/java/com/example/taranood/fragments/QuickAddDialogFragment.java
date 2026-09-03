package com.example.taranood.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.taranood.R;
import com.example.taranood.adapters.SearchAdapter;
import com.example.taranood.api.TMDbModels;
import com.example.taranood.api.TMDbService;
import com.example.taranood.models.LogEntry;
import com.example.taranood.models.WatchItem;
import com.example.taranood.utils.StorageHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class QuickAddDialogFragment extends BottomSheetDialogFragment
        implements SearchAdapter.OnResultClickListener {

    private TextInputEditText inputSearch;
    private ProgressBar searchProgress;
    private RecyclerView resultsRecycler;
    private SearchAdapter adapter;
    private View selectedContainer;
    private ImageView selectedPoster;
    private TextView selectedTitle, selectedInfo, selectedDescription;
    private Spinner spinnerType;

    private OkHttpClient httpClient;
    private final Gson gson = new GsonBuilder().setLenient().create();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private okhttp3.Call activeSearchCall;
    private okhttp3.Call activeDetailCall;

    private WatchItem pendingItem;
    private String posterUrl;

    public static QuickAddDialogFragment newInstance() {
        return new QuickAddDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)  // longer timeout for big responses like popular movies
                .writeTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quick_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inputSearch         = view.findViewById(R.id.input_search);
        searchProgress      = view.findViewById(R.id.search_progress);
        resultsRecycler     = view.findViewById(R.id.search_results_recycler);
        selectedContainer   = view.findViewById(R.id.selected_item_container);
        selectedPoster      = view.findViewById(R.id.selected_poster);
        selectedTitle       = view.findViewById(R.id.selected_title);
        selectedInfo        = view.findViewById(R.id.selected_info);
        selectedDescription = view.findViewById(R.id.selected_description);
        spinnerType         = view.findViewById(R.id.spinner_type);

        resultsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchAdapter(new ArrayList<>(), this);
        resultsRecycler.setAdapter(adapter);

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                if (activeSearchCall != null) activeSearchCall.cancel();
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    searchRunnable = () -> performSearch(query);
                    searchHandler.postDelayed(searchRunnable, 700);
                } else {
                    searchProgress.setVisibility(View.GONE);
                    adapter.updateResults(new ArrayList<>());
                    selectedContainer.setVisibility(View.GONE);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        view.findViewById(R.id.btn_save_quick).setOnClickListener(v -> saveItem());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (activeSearchCall != null) activeSearchCall.cancel();
        if (activeDetailCall != null) activeDetailCall.cancel();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
    }

    // ─── Core HTTP executor ──────────────────────────────────────────────────

    interface RawCallback {
        void onResponse(int code, String body);
    }

    /**
     * Builds a TMDb URL safely. Returns null (and shows a toast) if the base URL
     * or path segment is somehow malformed.
     */
    private HttpUrl buildUrl(String path, String... queryPairs) {
        String fullPath = TMDbService.BASE_URL + path;
        HttpUrl base = HttpUrl.parse(fullPath);
        if (base == null) return null;
        HttpUrl.Builder b = base.newBuilder()
                .addQueryParameter("api_key", TMDbService.API_KEY);
        for (int i = 0; i + 1 < queryPairs.length; i += 2) {
            b.addQueryParameter(queryPairs[i], queryPairs[i + 1]);
        }
        return b.build();
    }

    private okhttp3.Call executeAsync(HttpUrl url, RawCallback cb) {
        if (url == null) {
            cb.onResponse(-1, "Failed to build request URL");
            // return a dummy no-op Call so callers don't NPE
            return httpClient.newCall(new Request.Builder()
                    .url("https://api.themoviedb.org/3/").build());
        }
        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .header("User-Agent", "Taranood/1.0")
                .build();
        okhttp3.Call call = httpClient.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call c, @NonNull IOException e) {
                if (c.isCanceled()) return;
                runOnMain(() -> cb.onResponse(-1, "Network error: " + e.getMessage()));
            }
            @Override
            public void onResponse(@NonNull okhttp3.Call c,
                                   @NonNull okhttp3.Response response) {
                if (c.isCanceled()) { response.close(); return; }
                int code = response.code();
                String body = "";
                try {
                    if (response.body() != null) body = response.body().string();
                } catch (IOException e) {
                    final String errMsg = "Failed to read response: " + e.getMessage();
                    runOnMain(() -> cb.onResponse(-1, errMsg));
                    return;
                } finally {
                    response.close();
                }
                final int fCode = code;
                final String fBody = body;
                runOnMain(() -> cb.onResponse(fCode, fBody));
            }
        });
        return call;
    }

    private void runOnMain(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }

    private String httpErrorMessage(int code, String body) {
        if (code == -1) return body;
        switch (code) {
            case 401: return "Invalid TMDb API key. Get a free one at themoviedb.org → Settings → API";
            case 404: return "Item not found on TMDb (HTTP 404)";
            case 429: return "Too many requests — wait a moment and try again";
            default:
                try {
                    org.json.JSONObject j = new org.json.JSONObject(body);
                    if (j.has("status_message")) return j.getString("status_message");
                } catch (Exception ignored) {}
                return "Request failed (HTTP " + code + ")";
        }
    }

    // ─── Search ──────────────────────────────────────────────────────────────

    private void performSearch(String query) {
        if (getContext() == null) return;
        searchProgress.setVisibility(View.VISIBLE);
        selectedContainer.setVisibility(View.GONE);
        resultsRecycler.setVisibility(View.VISIBLE);

        HttpUrl url = buildUrl("search/multi",
                "query", query,
                "include_adult", "false");

        activeSearchCall = executeAsync(url, (code, body) -> {
            if (getContext() == null) return;
            searchProgress.setVisibility(View.GONE);
            if (code == 200) {
                try {
                    TMDbModels.SearchResponse parsed =
                            gson.fromJson(body, TMDbModels.SearchResponse.class);
                    List<TMDbModels.SearchResult> filtered = new ArrayList<>();
                    if (parsed != null && parsed.results != null) {
                        for (TMDbModels.SearchResult r : parsed.results) {
                            if ("movie".equals(r.mediaType) || "tv".equals(r.mediaType)) {
                                filtered.add(r);
                            }
                        }
                    }
                    adapter.updateResults(filtered);
                    resultsRecycler.setVisibility(View.VISIBLE);
                    if (filtered.isEmpty()) {
                        Toast.makeText(getContext(),
                                "No results found for \"" + query + "\"",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    String hint = body.trim().startsWith("<")
                            ? "TMDb returned an error page — check your API key"
                            : "Parse error: " + e.getMessage();
                    Toast.makeText(getContext(), hint, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), httpErrorMessage(code, body),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // ─── Result click ────────────────────────────────────────────────────────

    @Override
    public void onResultClick(TMDbModels.SearchResult result) {
        inputSearch.clearFocus();
        resultsRecycler.setVisibility(View.GONE);
        selectedContainer.setVisibility(View.GONE);
        if (activeDetailCall != null) activeDetailCall.cancel();
        searchProgress.setVisibility(View.VISIBLE);

        if ("movie".equals(result.mediaType)) {
            fetchMovieDetails(result.id, result.title);
        } else {
            fetchTVDetails(result.id, result.name);
        }
    }

    private void fetchMovieDetails(int id, String fallbackTitle) {
        // Request only the fields we need — append_to_response is NOT used so the
        // response stays small and fast even for blockbusters like Barbie (2023)
        HttpUrl url = buildUrl("movie/" + id);
        if (url == null) {
            searchProgress.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Failed to build movie URL", Toast.LENGTH_SHORT).show();
            return;
        }

        activeDetailCall = executeAsync(url, (code, body) -> {
            if (getContext() == null) return;
            searchProgress.setVisibility(View.GONE);
            if (code == 200) {
                try {
                    TMDbModels.MovieDetails d =
                            gson.fromJson(body, TMDbModels.MovieDetails.class);
                    if (d == null) throw new Exception("Empty response");

                    pendingItem = new WatchItem();
                    // Use fallback title from search result if detail title is missing
                    String title = (d.title != null && !d.title.isEmpty())
                            ? d.title : (fallbackTitle != null ? fallbackTitle : "Unknown");
                    pendingItem.setTitle(title);
                    pendingItem.setType("Movie");
                    pendingItem.setStatus("Planned");
                    pendingItem.setTotalRuntime(d.runtime);
                    pendingItem.setNotes(d.overview != null ? d.overview : "");
                    pendingItem.setWatchLink(generateWatchLink(title));
                    posterUrl = d.posterPath != null
                            ? TMDbService.IMAGE_BASE_URL + d.posterPath : null;
                    showSelectedItem(pendingItem);
                } catch (Exception e) {
                    Toast.makeText(getContext(),
                            "Could not parse movie details: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), httpErrorMessage(code, body),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTVDetails(int id, String fallbackTitle) {
        HttpUrl url = buildUrl("tv/" + id);
        if (url == null) {
            searchProgress.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Failed to build TV URL", Toast.LENGTH_SHORT).show();
            return;
        }

        activeDetailCall = executeAsync(url, (code, body) -> {
            if (getContext() == null) return;
            searchProgress.setVisibility(View.GONE);
            if (code == 200) {
                try {
                    TMDbModels.TVDetails d =
                            gson.fromJson(body, TMDbModels.TVDetails.class);
                    if (d == null) throw new Exception("Empty response");

                    pendingItem = new WatchItem();
                    String title = (d.name != null && !d.name.isEmpty())
                            ? d.name : (fallbackTitle != null ? fallbackTitle : "Unknown");
                    pendingItem.setTitle(title);
                    pendingItem.setType("Series");
                    pendingItem.setStatus("Planned");
                    pendingItem.setTotalEpisodes(d.numberOfEpisodes);
                    pendingItem.setNotes(d.overview != null ? d.overview : "");
                    pendingItem.setWatchLink(generateWatchLink(title));
                    posterUrl = d.posterPath != null
                            ? TMDbService.IMAGE_BASE_URL + d.posterPath : null;
                    showSelectedItem(pendingItem);
                } catch (Exception e) {
                    Toast.makeText(getContext(),
                            "Could not parse series details: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), httpErrorMessage(code, body),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── UI ──────────────────────────────────────────────────────────────────

    private void showSelectedItem(WatchItem item) {
        selectedContainer.setVisibility(View.VISIBLE);
        selectedTitle.setText(item.getTitle());

        String[] typeOptions = "Movie".equals(item.getType())
                ? new String[]{"Movie"}
                : new String[]{"Series", "Anime", "Other"};

        ArrayAdapter<String> sa = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, typeOptions);
        sa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(sa);
        spinnerType.setSelection(0);
        spinnerType.setVisibility(View.VISIBLE);

        spinnerType.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> p,
                                               View v, int pos, long id) {
                        item.setType(typeOptions[pos]);
                        updateInfoText(item);
                    }
                    @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
                });

        updateInfoText(item);
        selectedDescription.setText(item.getNotes());

        if (posterUrl != null) {
            Glide.with(this).load(posterUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(selectedPoster);
        } else {
            selectedPoster.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private void updateInfoText(WatchItem item) {
        String info;
        if ("Movie".equals(item.getType())) {
            int rt = item.getTotalRuntime();
            info = "Movie | " + (rt > 0 ? rt + " min" : "Runtime unknown");
        } else {
            int eps = item.getTotalEpisodes();
            info = item.getType() + " | " + (eps > 0 ? eps + " episodes" : "Episodes unknown");
        }
        selectedInfo.setText(info);
    }

    private String generateWatchLink(String title) {
        if (title == null) return "";
        try {
            return "https://kisskh.do/Search?q=" +
                    java.net.URLEncoder.encode(title, "UTF-8");
        } catch (Exception e) {
            return "https://kisskh.do/Search?q=" + title.replace(" ", "+");
        }
    }

    // ─── Save ────────────────────────────────────────────────────────────────

    private void saveItem() {
        if (pendingItem == null) {
            Toast.makeText(getContext(), "Please select a title first",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (spinnerType != null && spinnerType.getSelectedItem() != null) {
            pendingItem.setType(spinnerType.getSelectedItem().toString());
        }

        List<WatchItem> allItems = StorageHelper.loadWatchItems(getContext());
        for (WatchItem existing : allItems) {
            if (existing.getTitle() != null
                    && existing.getTitle().equalsIgnoreCase(pendingItem.getTitle())
                    && existing.getType() != null
                    && existing.getType().equalsIgnoreCase(pendingItem.getType())) {
                Toast.makeText(getContext(),
                        "\"" + pendingItem.getTitle() + "\" is already in your watchlist",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        pendingItem.setImageUri(posterUrl);
        allItems.add(pendingItem);
        StorageHelper.saveWatchItems(getContext(), allItems);
        StorageHelper.saveLogEntry(getContext(), new LogEntry(
                "Quick Added \"" + pendingItem.getTitle() + "\" to watchlist",
                pendingItem.getTitle(),
                "Type: " + pendingItem.getType()));

        Toast.makeText(getContext(),
                "\"" + pendingItem.getTitle() + "\" added!", Toast.LENGTH_SHORT).show();

        if (getParentFragment() instanceof HomeFragment) {
            ((HomeFragment) getParentFragment()).refreshData();
        }
        dismiss();
    }
}