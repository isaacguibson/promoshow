package com.myappcompany.isaac.dealday.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.myappcompany.isaac.dealday.Constantes.ConstantesPlataformas;
import com.myappcompany.isaac.dealday.Enums.Platform;
import com.myappcompany.isaac.dealday.Model.PlatformModel;
import com.myappcompany.isaac.dealday.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by isaac on 15/04/18.
 */

public class ConfigPlatformAdapter extends BaseAdapter {

    private List<PlatformModel> plataformas;
    private Activity activity;
    private SharedPreferences sharedPreferences;
    private View viewConfig;
    private Switch switch_platform;
    private ImageButton image_platform;
    private TextView platform_name;

    public ConfigPlatformAdapter(Activity activity, SharedPreferences sharedPreferences) {

        this.sharedPreferences = sharedPreferences;
        plataformas = new ArrayList<PlatformModel>();

        for(Platform platform : Platform.values()){

            PlatformModel platformModel = new PlatformModel(formatNames(platform.getNome()), platform.getNome(),sharedPreferences.getBoolean(platform.getNome(), true));

            plataformas.add(platformModel);
        }

        this.activity = activity;
    }

    private String formatNames(String name){
        switch (name){
            case "pelando":
                return "Pelando";
            case "promobit":
                return "Promobit";
            case "hardmob":
                return "Hardmob";
            case "adrenaline":
                return "Adrenaline";
            case "promoforum":
                return "Promoforum";
            case "melhores_destinos":
                return "Melhores Destinos";
            case "passagens_imperdiveis":
                return "Passagens Imperdíveis";
            default:
                return "Sem Nome";
        }
    }

    @Override
    public int getCount() {
        return plataformas.size();
    }

    @Override
    public Object getItem(int i) {
        return plataformas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {

        viewConfig = view;

        if(viewConfig == null){
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


            viewConfig = inflater.inflate(R.layout.config_platform_layout, null);
        }

        final PlatformModel plataforma = plataformas.get(i);

        platform_name = (TextView) viewConfig.findViewById(R.id.textViewConfig);
        image_platform = (ImageButton) viewConfig.findViewById(R.id.imageLogoConfig);
        switch_platform = (Switch) viewConfig.findViewById(R.id.switchConfig);


        platform_name.setText(plataforma.getName());
        fillImageLogo(image_platform, plataforma.getConfName());

        switch_platform.setChecked(plataforma.isChecked());
        switch_platform.setTag(plataformas.get(i));

        switch_platform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch aSwitch = (Switch) v;
                PlatformModel platformModel = (PlatformModel) v.getTag();
                platformModel.setChecked(((Switch) v).isChecked());

                if(aSwitch.isChecked()){

                    platformModel.setChecked(true);
                    plataformas.set(i, platformModel);
                } else {

                    platformModel.setChecked(false);
                    plataformas.set(i, platformModel);
                }
            }
        });
        return viewConfig;
    }

    public void fillImageLogo(ImageButton imageButton, String platform){
        switch (platform){
            case ConstantesPlataformas.PELANDO:
                imageButton.setImageDrawable(activity.getBaseContext().getResources().getDrawable(R.drawable.pelando_logo));
                break;
            case ConstantesPlataformas.HARDMOB:
                imageButton.setImageDrawable(activity.getBaseContext().getResources().getDrawable(R.drawable.hardmob_logo));
                break;
            case ConstantesPlataformas.PROMOBIT:
                imageButton.setImageDrawable(activity.getBaseContext().getResources().getDrawable(R.drawable.promobit_logo));
                break;
            case ConstantesPlataformas.PROMOFORUM:
                imageButton.setImageDrawable(activity.getBaseContext().getResources().getDrawable(R.drawable.promoforum_logo));
                break;
            case ConstantesPlataformas.ADRENALINE:
                imageButton.setImageDrawable(activity.getBaseContext().getResources().getDrawable(R.drawable.adrenaline_logo));
                break;
            case ConstantesPlataformas.MELHORES_DESTINOS:
                imageButton.setImageDrawable(activity.getBaseContext().getResources().getDrawable(R.drawable.melhores_destinos_logo));
                break;
            case ConstantesPlataformas.PASSAGENS_IMPERDIVEIS:
                imageButton.setImageDrawable(activity.getBaseContext().getResources().getDrawable(R.drawable.passagens_imperdiveis_logo));
                break;
        }
    }

    public List<PlatformModel> getPlataformas() {
        return plataformas;
    }

    public void setPlataformas(List<PlatformModel> plataformas) {
        this.plataformas = plataformas;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

}
