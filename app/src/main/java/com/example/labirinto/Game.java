package com.example.labirinto;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class Game extends AppCompatActivity {
    private static byte[] selfieByteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        selfieByteArray = extras.getByteArray("selfieByteArray");

        setContentView(R.layout.game);
    }

    public static byte[] getByteArray() {
        return selfieByteArray;
    }

    public void restartGame(View v) {
        this.recreate();
    }
}
