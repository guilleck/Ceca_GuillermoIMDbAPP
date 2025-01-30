package es.riberadeltajo.ceca_guillermoimdbapp.ui.slideshow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import es.riberadeltajo.ceca_guillermoimdbapp.adapters.MovieAdapter;
import es.riberadeltajo.ceca_guillermoimdbapp.R;
import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesManager;
import es.riberadeltajo.ceca_guillermoimdbapp.models.Movie;

public class FavoriteFragment extends Fragment {
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1;
    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movie> movieList = new ArrayList<>();
    private FavoritesManager favoritesManager;

    private final ActivityResultLauncher<String[]> bluetoothPermissions =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (result.getOrDefault(Manifest.permission.BLUETOOTH_CONNECT, false) &&
                        result.getOrDefault(Manifest.permission.BLUETOOTH_SCAN, false) &&
                        result.getOrDefault(Manifest.permission.BLUETOOTH_ADMIN, false)) {
                    // Permisos concedidos, compartir las películas
                    shareFavorites();
                } else {
                    Toast.makeText(getContext(), "Permiso de Bluetooth denegado", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favorite, container, false);

        recyclerView = root.findViewById(R.id.recyclerViewSearch);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        favoritesManager = new FavoritesManager(getContext());
        adapter = new MovieAdapter(getContext(), movieList,favoritesManager);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemLongClickListener(movie -> {
            String userID = getGoogleUserId();
            favoritesManager.removeFavorite(movie,userID);
            movieList.remove(movie);
            adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Eliminada de favoritos: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
        });

        Button shareButton = root.findViewById(R.id.btnShareFavorites);
        shareButton.setOnClickListener(v -> requestBluetoothPermissionAndShare());

        loadFavorites();

        return root;
    }

    private void loadFavorites() {
        String userID = getGoogleUserId();
        if (userID == null) {
            Toast.makeText(getContext(), "Error: No se pudo obtener el usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Movie> favorites = favoritesManager.getFavorites(userID);
        if (favorites == null) {
            favorites = new ArrayList<>();
        }

        movieList.clear();
        movieList.addAll(favorites);
        adapter.notifyDataSetChanged();
    }


    private void shareFavorites() {
        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(movieList);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, jsonFavorites);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Compartir lista de favoritos"));
        } else {
            Toast.makeText(getContext(), "No hay aplicaciones disponibles para compartir.", Toast.LENGTH_SHORT).show();
        }
    }
    private void requestBluetoothPermissionAndShare() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADMIN
            }, BLUETOOTH_PERMISSION_REQUEST_CODE);
        } else {
            shareFavorites();
        }
    }

    public String getGoogleUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        } else {
            return null;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }


}
