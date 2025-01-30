package es.riberadeltajo.ceca_guillermoimdbapp.api;


import es.riberadeltajo.ceca_guillermoimdbapp.models.MovieOverviewResponse;
import es.riberadeltajo.ceca_guillermoimdbapp.models.PopularMoviesResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IMDBApiService {

    @GET("title/get-top-meter")
    Call<PopularMoviesResponse> obtenerTop10(@Query("region") String region);

    @GET("title/get-overview")
    Call<MovieOverviewResponse> obtenerDatos(@Query("id") String id);

}