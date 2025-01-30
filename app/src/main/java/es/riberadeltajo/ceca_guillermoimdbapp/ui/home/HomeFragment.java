
package es.riberadeltajo.ceca_guillermoimdbapp.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import es.riberadeltajo.ceca_guillermoimdbapp.adapters.MovieAdapter;
import es.riberadeltajo.ceca_guillermoimdbapp.api.IMDBApiService;
import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesDatabaseHelper;
import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesManager;
import es.riberadeltajo.ceca_guillermoimdbapp.databinding.FragmentHomeBinding;
import es.riberadeltajo.ceca_guillermoimdbapp.models.Movie;
import es.riberadeltajo.ceca_guillermoimdbapp.models.PopularMoviesResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private IMDBApiService ApiService;
    private List<Movie> movieList = new ArrayList<>();
    private MovieAdapter adapter;
    private RecyclerView recyclerView;
    private FavoritesDatabaseHelper databaseHelper;
    private FavoritesManager favoritesManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recycler;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        favoritesManager = new FavoritesManager(getContext());
        adapter = new MovieAdapter(getContext(), movieList,favoritesManager);
        recyclerView.setAdapter(adapter);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request modifiedRequest = chain.request().newBuilder()
                            .addHeader("X-RapidAPI-Key", "9c478d1de5msh0376a3e3aa6209ep161637jsn5be10011fc93")
                            .addHeader("X-RapidAPI-Host", "imdb-com.p.rapidapi.com")
                            .build();
                    return chain.proceed(modifiedRequest);
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://imdb-com.p.rapidapi.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService = retrofit.create(IMDBApiService.class);

        Call<PopularMoviesResponse> call = ApiService.obtenerTop10("US");
        call.enqueue(new Callback<PopularMoviesResponse>() {
            @Override
            public void onResponse(Call<PopularMoviesResponse> call, Response<PopularMoviesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PopularMoviesResponse.Edge> edges = response.body().getData().getTopMeterTitles().getEdges();
                    if (edges != null && !edges.isEmpty()) {
                        movieList.clear();
                        for (int i = 0; i < Math.min(edges.size(), 10); i++) {
                            PopularMoviesResponse.Edge edge = edges.get(i);
                            PopularMoviesResponse.Node node = edge.getNode();
                            Movie movie = new Movie();
                            movie.setId(node.getId());
                            movie.setTitle(node.getTitleText().getText());
                            movie.setReleaseDate(node.getPrimaryImage().getUrl());
                            movie.setPosterPath(node.getPrimaryImage().getUrl());
                            movieList.add(movie);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<PopularMoviesResponse> call, Throwable t) {
                Log.e("HomeFragment", "Error en la llamada API: " + t.getMessage());
            }
        });

        adapter.setOnItemLongClickListener(movie -> {
            favoritesManager.addFavorite(movie);
            movieList.add(movie);
            adapter.notifyItemInserted(movieList.size() - 1);
            Toast.makeText(getContext(), "Película añadida a favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
        });

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
