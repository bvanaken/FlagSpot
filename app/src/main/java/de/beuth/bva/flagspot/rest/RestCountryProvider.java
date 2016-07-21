package de.beuth.bva.flagspot.rest;

import android.content.Context;
import android.util.Log;

import java.util.List;

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
    RestCountryListener listener;

    public RestCountryProvider(Context context, RestCountryListener lst) {

        listener = lst;

        // Setting up retrofit with base url
        retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.rest_countries_base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public void getCountryByName(String name) {

        RestCountryInterface apiService = retrofit.create(RestCountryInterface.class);

        Call<List<Country>> call = apiService.getCountry(name);
        call.enqueue(new Callback<List<Country>>() {

            @Override
            public void onResponse(Call<List<Country>> call, Response<List<Country>> response) {

                int statusCode = response.code();
                List<Country> countryList = response.body();

                if (countryList != null) {
                    for (Country country : countryList) {
                        Log.d(TAG, "onResponse: " + country.toString());
                    }

                    if (listener != null && !countryList.isEmpty()) {
                        listener.onCountryResponse(countryList.get(0));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Country>> call, Throwable t) {
                t.printStackTrace();
                if (listener != null) {
                    listener.onFailure();
                }
            }

        });
    }

    public interface RestCountryListener {
        void onCountryResponse(Country country);

        void onFailure();
    }

}
