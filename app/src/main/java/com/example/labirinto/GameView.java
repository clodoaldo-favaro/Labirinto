package com.example.labirinto;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class GameView extends View {
    private Cell[][] cells;
    private static final int COLS = 7, ROWS = 10;

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
    }

    private class Cell {
        boolean topWall = true, leftWall = true, bottomWall = true, rightWall = true;

        int col , row;

        public Cell(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }
}
