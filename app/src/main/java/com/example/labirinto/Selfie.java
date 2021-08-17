package com.example.labirinto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class Selfie extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selfie);
    }

    public void game(View v) {
        startActivity(new Intent(this, com.example.labirinto.Game.class));
    }
}
