package de.beuth.bva.flagspot.rest;

import android.content.Context;
import android.util.Log;

import de.beuth.bva.flagspot.R;
import de.beuth.bva.flagspot.model.Country;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Betty van Aken on 13/07/16.
 */
public class RestCountryProvider {

    private static final String TAG = "RestCountryProvider";

    Retrofit retrofit;

    public RestCountryProvider(Context context) {

        // Setting up retrofit with base url
        retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.rest_countries_base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public void getCountryByName(String name) {

        RestCountryInterface apiService = retrofit.create(RestCountryInterface.class);

        Call<Country> call = apiService.getCountry(name);
        call.enqueue(new Callback<Country>() {

            @Override
            public void onResponse(Call<Country> call, Response<Country> response) {

                int statusCode = response.code();
                Country countryObject = response.body();

                Log.d(TAG, "onResponse: " + countryObject.toString());
            }

            @Override
            public void onFailure(Call<Country> call, Throwable t) {

                Log.d(TAG, "onFailure: Country informations could not be retrieved.");
            }

        });

    }

}
