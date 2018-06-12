package com.myappcompany.isaac.dealday;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.myappcompany.isaac.dealday.Adapter.ConfigPlatformAdapter;
import com.myappcompany.isaac.dealday.Model.PlatformModel;

import java.util.List;

/**
 * Created by isaac on 15/04/18.
 */

public class ConfigActivity extends AppCompatActivity {

    private ListView listView;
    private Button button_save;
    private Button back_button;
    private SharedPreferences sharedPref;
    private ConfigPlatformAdapter configPlatformAdapter;
    private Spinner spinnerCategoria;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_layout);

        sharedPref = this.getSharedPreferences("promoshow_shared_prefs", Context.MODE_PRIVATE);

        listView = (ListView) findViewById(R.id.configListView);
        spinnerCategoria = (Spinner) findViewById(R.id.spinnerCategories);

        spinnerCategoria.setSelection(sharedPref.getInt("category" , 0));

        configPlatformAdapter = new ConfigPlatformAdapter(this, sharedPref);

        listView.setAdapter(configPlatformAdapter);

        ViewGroup.LayoutParams params = listView.getLayoutParams();

        int totalItemsHeight = 0;
        for (int itemPos = 0; itemPos < configPlatformAdapter.getCount(); itemPos++) {
            View item = configPlatformAdapter.getView(itemPos, null, listView);
            item.measure(0, 0);
            totalItemsHeight += item.getMeasuredHeight();
        }

        int totalDividersHeight = listView.getDividerHeight() * (configPlatformAdapter.getCount() - 1);

        params.height = totalItemsHeight + totalDividersHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();

        button_save = (Button) findViewById(R.id.buttonSaveConfig);
        button_save.setOnClickListener(saveListener);

        back_button = (Button) findViewById(R.id.buttonBackConfig);
        back_button.setOnClickListener(backListener);

    }


    private View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean one_checked = false;
            List<PlatformModel> platforms = configPlatformAdapter.getPlataformas();
            SharedPreferences.Editor editor = sharedPref.edit();

            for(int i = 0; i < platforms.size(); i++){
                if(platforms.get(i).isChecked()){
                    one_checked = true;
                }
                editor.putBoolean(platforms.get(i).getConfName(), platforms.get(i).isChecked());
            }
            if(!one_checked){
                Toast.makeText(getApplicationContext(), "Pelo menos uma opção de plataforma deve ser selecionada", Toast.LENGTH_SHORT).show();
                return;
            }

            String category = spinnerCategoria.getSelectedItem().toString();
            editor.putInt("category", spinnerCategoria.getSelectedItemPosition());

            editor.commit();

            configPlatformAdapter.setSharedPreferences(sharedPref);

            Toast.makeText(getApplicationContext(), "Ok! As mudanças serão aplicadas quando recarregar o feed", Toast.LENGTH_SHORT).show();

            ConfigActivity.this.finish();
        }
    };

    private View.OnClickListener backListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
}
