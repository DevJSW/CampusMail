package com.campusmail.campusmail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class ChooseCountry extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //String [] COUNTRY={"KENYA", "UGANDA", "TANZANIA", "BURUNDI", "RWANDA", "SOUTH AFRICA", "GHANA", "NIGERIA", "EGYPT"};

    private TextView next;

    Spinner spinner_country;
    ArrayAdapter country_adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_country);

        country_adapter = ArrayAdapter.createFromResource(this, R.array.choose_country, android.R.layout.simple_dropdown_item_1line);
        spinner_country = (Spinner) findViewById(R.id.spinner_country);
        spinner_country.setAdapter(country_adapter);
        spinner_country.setOnItemSelectedListener( ChooseCountry.this);

        next = (TextView) findViewById(R.id.nextBtn);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent cardonClick = new Intent(ChooseCountry.this, EditActivity.class);
                cardonClick.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(cardonClick);
            }
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
