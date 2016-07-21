package de.beuth.bva.flagspot.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.beuth.bva.flagspot.R;
import de.beuth.bva.flagspot.model.Country;

/**
 * Created by Betty van Aken on 19/07/16.
 */
public class ChooserView extends RelativeLayout {

    private static final String TAG = "ChooserView";

    Context context;

    ChooserViewListener listener;

    TextView leftText;
    TextView rightText;
    ImageView leftImg;
    ImageView rightImg;
    ImageView close;

    Country countryLeft;
    Country countryRight;

    public ChooserView(Context context, ChooserViewListener listener, Country countryLeft, Country countryRight) {
        super(context);
        this.context = context;
        this.listener = listener;
        this.countryLeft = countryLeft;
        this.countryRight = countryRight;

        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.view_chooser, this);

        leftText = (TextView) findViewById(R.id.text_left);
        rightText = (TextView) findViewById(R.id.text_right);
        leftImg = (ImageView) findViewById(R.id.flag_left);
        rightImg = (ImageView) findViewById(R.id.flag_right);
        close = (ImageView) findViewById(R.id.close_icon);

        setUpImageViews();

        leftText.setText(countryLeft.getName());
        rightText.setText(countryRight.getName());

        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClosePressed();
                }
            }
        });

        OnClickListener onFlagClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listener != null) {

                    switch (v.getId()) {
                        case R.id.text_left:
                        case R.id.flag_left:
                            listener.onChosenCountry(countryLeft);
                            break;
                        case R.id.text_right:
                        case R.id.flag_right:
                            listener.onChosenCountry(countryRight);
                            break;
                    }
                }

            }
        };

        leftText.setOnClickListener(onFlagClickListener);
        leftImg.setOnClickListener(onFlagClickListener);
        rightText.setOnClickListener(onFlagClickListener);
        rightImg.setOnClickListener(onFlagClickListener);
    }

    private void setUpImageViews(){

        if(countryLeft.getAltSpellings() != null) {
            String countryCode = countryLeft.getAltSpellings().get(0);
            if(countryCode != null){
                Picasso.with(context)
                        .load(context.getString(R.string.flag_base_url) + countryCode.toUpperCase() + ".png")
                        .placeholder(R.drawable.flag_outline)
                        .into(leftImg);
            }
        }

        if(countryRight.getAltSpellings() != null) {
            String countryCode = countryRight.getAltSpellings().get(0);
            if(countryCode != null){
                Picasso.with(context)
                        .load(context.getString(R.string.flag_base_url) + countryCode.toUpperCase() + ".png")
                        .placeholder(R.drawable.flag_outline)
                        .into(rightImg);
            }
        }
    }

    public interface ChooserViewListener {
        void onChosenCountry(Country chosenCountry);

        void onClosePressed();
    }

}
