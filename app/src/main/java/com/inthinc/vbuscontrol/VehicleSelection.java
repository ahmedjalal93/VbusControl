package com.inthinc.vbuscontrol;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.inthinc.vbuscontrol.MainActivity.TAG;

/**
 * Created by ajalal on 5/21/18.
 */

public class VehicleSelection extends Activity{

    private ArrayList<String> years = new ArrayList<String>();;
    private ArrayList<String> makes = new ArrayList<String>();
    private ArrayList<String> models = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicle_selection);

        Spinner mYearSelector = findViewById(R.id.spinner_year);
        Spinner mMakeSelector = findViewById(R.id.spinner_make);
        Spinner mModelSelector = findViewById(R.id.spinner_model);

        if(SupportedVehicles() == 0){
            Log.i(TAG,"No supported vehicles found");
        };
        ArrayAdapter<String> mYearAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, years);
        ArrayAdapter<String> mMakesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, makes);
        ArrayAdapter<String> mModelsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, models);
        mYearSelector.setAdapter(mYearAdapter);
        mMakeSelector.setAdapter(mMakesAdapter);
        mModelSelector.setAdapter(mModelsAdapter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

    }


    public int SupportedVehicles() {
        try {
            final String[] Vehicles = getApplicationContext().getResources().getAssets().list("");
            if(Vehicles != null && Vehicles.length > 0){
                for (int i=0; i< Vehicles.length; i++){
                    if(Vehicles[i].matches("[\\d]+\\_.*$")) {
                        String[] mYearMakeModel = Vehicles[i].split("_");
                        if(!years.contains(mYearMakeModel[0])) {
                            years.add(mYearMakeModel[0]);
                        }
                        if(!makes.contains(mYearMakeModel[1])){
                            makes.add(mYearMakeModel[1]);
                        }
                        if(!models.contains(mYearMakeModel[2])){
                            models.add(mYearMakeModel[2]);
                        }

                    }else{
                        //Log.i(TAG, "No vehicles found!");
                    }
                }
                return 1;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
