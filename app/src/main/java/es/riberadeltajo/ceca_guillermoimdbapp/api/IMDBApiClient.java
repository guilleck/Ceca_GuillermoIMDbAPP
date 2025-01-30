package es.riberadeltajo.ceca_guillermoimdbapp.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class IMDBApiClient {
    private static final String BASE_URL = "https://imdb-com.p.rapidapi.com/";
    private static IMDBApiService apiService;
    private static RapidApiKeyManager apiKeyManager = new RapidApiKeyManager();

    public static IMDBApiService getApiService() {
        if (apiService == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        String apiKey = apiKeyManager.getCurrentKey();
                        Request modifiedRequest = chain.request().newBuilder()
                                .addHeader("X-RapidAPI-Key", apiKey)
                                .addHeader("X-RapidAPI-Host", "imdb-com.p.rapidapi.com")
                                .build();
                        return chain.proceed(modifiedRequest);
                    })
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            // Configuración del cliente Retrofit
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(IMDBApiService.class);
        }
        return apiService;
    }

    public static String getApiKey() {
        return apiKeyManager.getCurrentKey();
    }

    public static void switchApiKey() {
        apiKeyManager.switchToNextKey();
    }
}
