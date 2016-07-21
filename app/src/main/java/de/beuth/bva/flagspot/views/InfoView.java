package de.beuth.bva.flagspot.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.beuth.bva.flagspot.R;
import de.beuth.bva.flagspot.model.Country;

/**
 * Created by Betty van Aken on 20/07/16.
 */
public class InfoView extends RelativeLayout {

    private static final String TAG = "InfoView";

    Context context;

    TextView name;
    TextView capital;
    TextView population;
    TextView region;
    TextView moreInfo;
    ImageView flag;
    ImageView close;

    String countryName;

    Bitmap bitmap;

    public InfoView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.view_infos, this);

        name = (TextView) findViewById(R.id.name);
        capital = (TextView) findViewById(R.id.capital);
        population = (TextView) findViewById(R.id.population);
        region = (TextView) findViewById(R.id.region);
        moreInfo = (TextView) findViewById(R.id.more_info);
        flag = (ImageView) findViewById(R.id.flag);
        close = (ImageView) findViewById(R.id.close_icon);

        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibility(GONE);
            }
        });

        moreInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = context.getString(R.string.wikipedia_base_url) + countryName;

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));

                context.startActivity(i);
            }
        });
    }

    public void resetCountry(Country country) {

        countryName = country.getName();

        name.setText(countryName);
        capital.setText("Capital: " + country.getCapital());
        population.setText(String.format("%,d", country.getPopulation()));
        region.setText(country.getSubregion());

        if(country.getAltSpellings() != null) {
            String countryCode = country.getAltSpellings().get(0);
            if(countryCode != null){
                Picasso.with(context)
                        .load(context.getString(R.string.flag_base_url) + countryCode.toUpperCase() + ".png")
                        .placeholder(new BitmapDrawable(getResources(), bitmap))
                        .into(flag);
            }
        }
    }

    public void setFlag(Bitmap bitmap) {
        this.bitmap = bitmap;
        flag.setImageBitmap(bitmap);
    }

}
