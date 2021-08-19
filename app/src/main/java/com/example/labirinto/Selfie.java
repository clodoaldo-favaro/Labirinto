package com.example.labirinto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Selfie extends AppCompatActivity {

    ImageView imageViewSelfie;
    Button takePictureBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selfie);

        imageViewSelfie = findViewById(R.id.imageViewSelfie);
        takePictureBtn = findViewById(R.id.buttonTakeSelfie);

        takePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public void game(View v) {
        startActivity(new Intent(this, com.example.labirinto.Game.class));
    }



}
