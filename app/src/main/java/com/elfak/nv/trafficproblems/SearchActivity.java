package com.elfak.nv.trafficproblems;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class SearchActivity extends AppCompatActivity {
    ImageButton search_by_attributes,search_by_radius,searching,searching2;
    EditText enter_problem_name,enter_problem_date,enter_radius,enter_priority;
    TextView searching2_text_for_button,searching_text_for_button;
    private boolean byRadius = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        search_by_attributes = findViewById(R.id.search_by_attributes);
        search_by_radius = findViewById(R.id.search_by_radius);
        enter_problem_name = findViewById(R.id.enter_problem_name);
        enter_problem_date = findViewById(R.id.enter_problem_date);
        enter_priority = findViewById(R.id.enter_priority);
        enter_radius = findViewById(R.id.enter_radius);
        searching = findViewById(R.id.searching);
        searching2 = findViewById(R.id.searching2);
        searching_text_for_button = findViewById(R.id.searching_text_for_button);
        searching2_text_for_button = findViewById(R.id.searching2_text_for_button);
        searching2.setVisibility(View.INVISIBLE);
        enter_radius.setVisibility(View.INVISIBLE);
        searching2_text_for_button.setVisibility(View.INVISIBLE);

        search_by_attributes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byRadius = false;
                enter_problem_name.setVisibility(View.VISIBLE);
                enter_problem_date.setVisibility(View.VISIBLE);
                enter_priority.setVisibility(View.VISIBLE);
                searching.setVisibility(View.VISIBLE);
                searching_text_for_button.setVisibility(View.VISIBLE);
                searching2.setVisibility(View.INVISIBLE);
                enter_radius.setVisibility(View.INVISIBLE);
                searching2_text_for_button.setVisibility(View.INVISIBLE);
            }
        });
        search_by_radius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byRadius = true;
                enter_problem_name.setVisibility(View.INVISIBLE);
                enter_problem_date.setVisibility(View.INVISIBLE);
                enter_priority.setVisibility(View.INVISIBLE);
                searching.setVisibility(View.INVISIBLE);
                searching2.setVisibility(View.VISIBLE);
                enter_radius.setVisibility(View.VISIBLE);
                searching_text_for_button.setVisibility(View.INVISIBLE);
                searching2_text_for_button.setVisibility(View.VISIBLE);
            }
        });
        searching.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String priority = enter_priority.getText().toString();
                String name = enter_problem_name.getText().toString();
                String date = enter_problem_date.getText().toString();
                Bundle idBundle = new Bundle();
                idBundle.putInt("case", 2);
                idBundle.putString("priority",priority);
                idBundle.putString("name",name);
                idBundle.putString("date",date);
                Intent profile = new Intent(SearchActivity.this,SearchMap.class);
                profile.putExtras(idBundle);
                startActivity(profile);
            }
        });
        searching2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String radius = enter_radius.getText().toString();
                Bundle idBundle = new Bundle();
                idBundle.putInt("case", 1);
                idBundle.putString("radius",radius);
                Intent profile = new Intent(SearchActivity.this,SearchMap.class);
                profile.putExtras(idBundle);
                startActivity(profile);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
