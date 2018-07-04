package com.elfak.nv.trafficproblems;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class UserProblemsData {
    private ArrayList<Problem> problems;
    private HashMap<String, Integer> problemsKeyIndexMapping;
    private DatabaseReference database;
    private static final String FIREBASE_CHILD = "problems";

    private UserProblemsData(){
        problems=new ArrayList<>();
        problemsKeyIndexMapping = new HashMap<String, Integer>();
        database = FirebaseDatabase.getInstance().getReference();

    }
}
