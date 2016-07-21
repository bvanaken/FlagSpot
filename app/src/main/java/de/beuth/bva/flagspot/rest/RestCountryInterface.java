package de.beuth.bva.flagspot.rest;

import java.util.List;

import de.beuth.bva.flagspot.model.Country;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Betty van Aken on 13/07/16.
 */
public interface RestCountryInterface {

    @GET("name/{country}")
    Call<List<Country>> getCountry(@Path("country") String countryName);

}
