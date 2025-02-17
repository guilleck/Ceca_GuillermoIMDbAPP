
package es.riberadeltajo.ceca_guillermoimdbapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.bumptech.glide.Glide;
import es.riberadeltajo.ceca_guillermoimdbapp.api.IMDBApiService;
import es.riberadeltajo.ceca_guillermoimdbapp.models.Movie;
import es.riberadeltajo.ceca_guillermoimdbapp.models.MovieOverviewResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class MovieDetailsActivity extends AppCompatActivity {

    private Movie pelicula;
    private TextView textViewTitulo,textViewDescripcion,textViewDate;
    private IMDBApiService ApiService;
    private ImageView imagen;
    private static final int REQUEST_CODE_PERMISSIONS = 100;
    private static final int PICK_CONTACT_REQUEST = 1;
    private String numeroTelefono;
    private String movieRating;

    private final ActivityResultLauncher<Intent> pickContact =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri contactUri = result.getData().getData();
                    String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

                    Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        numeroTelefono = cursor.getString(columnIndex);
                        cursor.close();
                        openSmsApp();
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent i = getIntent();
        pelicula = i.getParcelableExtra("pelicula");

        textViewTitulo = findViewById(R.id.TextViewTitle);
        textViewDescripcion = findViewById(R.id.TextViewDescription);
        textViewDate = findViewById(R.id.TextViewDate);
        textViewTitulo.setText(pelicula.getTitle());
        imagen = findViewById(R.id.ImageViewPortada);


        Glide.with(this)
                .load(pelicula.getPosterPath())
                .into(imagen);

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

        Call<MovieOverviewResponse> call = ApiService.obtenerDatos(pelicula.getId());
        call.enqueue(new Callback<MovieOverviewResponse>() {
            @Override
            public void onResponse(Call<MovieOverviewResponse> call, Response<MovieOverviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String descripcion = response.body().getData().getTitle().getPlot().getPlotText().getPlainText();
                    textViewDescripcion.setText(descripcion);

                    MovieOverviewResponse.ReleaseDate releaseDate = response.body().getData().getTitle().getReleaseDate();
                    if (releaseDate != null) {
                        String formattedDate = String.format("%d-%02d-%02d", releaseDate.getYear(), releaseDate.getMonth(), releaseDate.getDay());
                        textViewDate.setText("Release Date: " + formattedDate);
                    }

                    MovieOverviewResponse.RatingsSummary ratingsSummary = response.body().getData().getTitle().getRatingsSummary();
                    if (ratingsSummary != null) {
                        movieRating = String.format("%.1f", ratingsSummary.getAggregateRating());
                        Log.d("Rating", "Rating obtenido: " + movieRating);
                        TextView ratingView = findViewById(R.id.TextViewRating);
                        ratingView.setText("Rating: " + movieRating);
                    } else {
                        Log.d("Rating", "No se encontro el rating");  // Log si el rating es null
                    }

                } else {
                    Log.d("API Response", "Error en la respuesta");
                }
            }

            @Override
            public void onFailure(Call<MovieOverviewResponse> call, Throwable t) {
                Log.e("HomeFragment", "No se puede conectar a la API: " + t.getMessage());
            }
        });

        checkPermissions();

        Button btnSendSms = findViewById(R.id.btnSendSms);
        btnSendSms.setOnClickListener(view -> {
            if (numeroTelefono == null) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                pickContact.launch(intent);
            } else {
                openSmsApp();
            }
        });

    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS},
                    REQUEST_CODE_PERMISSIONS);
        }
    }


    private void openSmsApp() {
        String detallesPelicula = "Esta película te gustará: " + pelicula.getTitle() + "\nRating: " + movieRating;

        if (numeroTelefono != null) {
            Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + numeroTelefono));
            i.putExtra("sms_body", detallesPelicula);
            startActivity(i);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

            Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                numeroTelefono = cursor.getString(columnIndex);
                cursor.close();
                openSmsApp();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "Permisos concedidos");
            } else {
                Log.d("Permissions", "Permisos no concedidos");
            }
        }
    }
}
