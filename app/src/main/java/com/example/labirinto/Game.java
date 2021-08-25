package com.example.labirinto;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Game extends AppCompatActivity {
    private static byte[] selfieByteArray;
    Button restartButton;
    static int buttonHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        selfieByteArray = extras.getByteArray("selfieByteArray");

        setContentView(R.layout.game);
        restartButton = findViewById(R.id.button3);

        ViewTreeObserver viewTreeObserver = restartButton.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    buttonHeight = restartButton.getHeight();
                }
            });
        }
    }

    public static byte[] getByteArray() {
        return selfieByteArray;
    }

    public static int getButtonHeight() {
        return buttonHeight;
    }

    public void restartGame(View v) {
        this.recreate();
    }
}
