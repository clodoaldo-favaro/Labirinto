package com.example.labirinto;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class GameView extends View implements MediaPlayer.OnCompletionListener, SensorEventListener {

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private Vibrator vibrator;
    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private Sensor accelerometerSensor;

    private final float alpha = 0.8f;

    private static final int MAX_LEVELS = 3;
    private int currentLevel = 1;
    private String nextAction;

    private Cell[][] cells;
    private Cell player, exit;
    private static final int COLS = 7, ROWS = 10;
    private static final String
        ACTION_CREATE_MAZE = "ACTION_CREATE_MAZE",
        ACTION_END_GAME = "ACTION_END_GAME";


    private static final float WALL_THICKNESS = 4;
    private float cellSize, hMargin, vMargin;
    private Paint wallPaint, playerPaint, exitPaint;
    private Random random;
    private byte[] selfieByteArray;

    MediaPlayer mp;

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        selfieByteArray = Game.getByteArray();

        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(WALL_THICKNESS);

        playerPaint = new Paint();
        playerPaint.setColor(Color.RED);

        exitPaint = new Paint();
        exitPaint.setColor(Color.BLUE);

        random = new Random();

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        createMaze();
    }

    private void createMaze() {
        Stack<Cell> stack = new Stack<>();
        Cell current, next;

        cells = new Cell[COLS][ROWS];

        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                cells[x][y] = new Cell(x, y);
            }
        }

        player = cells[0][0];
        exit = cells[COLS - 1][ROWS - 1];

        current = cells[0][0];
        current.visited = true;

        do {
            next = getNeighbour(current);
            if (next != null) {
                removeWall(current, next);
                stack.push(current);
                current = next;
                current.visited = true;
            } else {
                current = stack.pop();
            }
        } while (!stack.empty());

        nextAction = "NOTHING";
        playSound("START");

    }

    private Cell getNeighbour(Cell cell) {
        ArrayList<Cell> neighbours = new ArrayList<>();

        //left neighbour
        if (cell.col > 0 && !cells[cell.col - 1][cell.row].visited) {
            neighbours.add(cells[cell.col - 1][cell.row]);
        }

        //right neighbour
        if (cell.col < (COLS - 1)  && !cells[cell.col + 1][cell.row].visited) {
            neighbours.add(cells[cell.col + 1][cell.row]);
        }

        //top neighbour
        if (cell.row > 0 && !cells[cell.col][cell.row - 1].visited) {
            neighbours.add(cells[cell.col][cell.row - 1]);
        }

        //bottom neighbour
        if (cell.row < (ROWS - 1) && !cells[cell.col][cell.row + 1].visited) {
            neighbours.add(cells[cell.col][cell.row + 1]);
        }

        if (neighbours.size() > 0 ) {
            int index = random.nextInt(neighbours.size());
            return neighbours.get(index);
        }

        return null;
    }

    private void removeWall(Cell current, Cell next) {
        //current under next
        if (current.col == next.col && current.row == next.row + 1) {
            current.topWall = false;
            next.bottomWall = false;
        }

        //current above next
        if (current.col == next.col && current.row == next.row - 1) {
            current.bottomWall = false;
            next.topWall = false;
        }

        //current to the right of the next
        if (current.col == next.col + 1 && current.row == next.row) {
            current.leftWall = false;
            next.rightWall = false;
        }

        //current to the left of the next
        if (current.col == next.col - 1 && current.row == next.row) {
            current.rightWall = false;
            next.leftWall = false;
        }
    }

    public void drawCurrentLevelText(Canvas canvas) {
        Paint textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(48f);

        int xPos = (canvas.getWidth() / 2);

        canvas.drawText("Level " + currentLevel, xPos, 56, textPaint);
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int buttonHeightPx = Game.getButtonHeight();

        canvas.drawColor(Color.WHITE);

        drawCurrentLevelText(canvas);

        int width = getWidth();
        int height = getHeight() - buttonHeightPx;

        if (width/height < COLS/ROWS) {
            cellSize = width/(COLS + 1) - 8;
        } else {
            cellSize = height/(ROWS + 1);
        }

        hMargin = (width - COLS*cellSize)/2;
        vMargin = (height - ROWS*cellSize)/2;

        canvas.translate(hMargin, vMargin + 48);

        if (currentLevel == 2) {
            wallPaint.setColor(Color.BLUE);
        } else if (currentLevel == 3) {
            wallPaint.setColor(Color.RED);
        }

        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                if (cells[x][y].topWall) {
                    canvas.drawLine(x*cellSize, y*cellSize, (x + 1)*cellSize, y*cellSize, wallPaint);
                }

                if (cells[x][y].leftWall && !(x == 0 && y == 0)) {
                    canvas.drawLine(x*cellSize, y*cellSize, x*cellSize, (y+1)*cellSize, wallPaint);
                }

                if (cells[x][y].rightWall && !(x == COLS - 1 && y == ROWS - 1)) {
                    canvas.drawLine((x+1)*cellSize, y*cellSize, (x + 1)*cellSize, (y+1)*cellSize, wallPaint);
                }

                if (cells[x][y].bottomWall) {
                    canvas.drawLine(x*cellSize, (y+1)*cellSize, (x+1)*cellSize, (y+1)*cellSize, wallPaint);
                }
            }
        }

        float margin = cellSize/10;

        RectF playerDestRect = new RectF(
                player.col*cellSize+margin,
                player.row*cellSize+margin,
                (player.col + 1)*cellSize-margin,
                (player.row + 1)*cellSize-margin
                );

        Bitmap bmp = BitmapFactory.decodeByteArray(selfieByteArray, 0, selfieByteArray.length);

        canvas.drawBitmap(bmp, null, playerDestRect, null);

        /*canvas.drawRect(
                player.col*cellSize+margin,
                player.row*cellSize+margin,
                (player.col + 1)*cellSize-margin,
                (player.row + 1)*cellSize-margin,
                playerPaint
        );*/

    }

    private void movePlayer(Direction direction) {
        switch (direction) {
            case UP:
                if (!player.topWall) {
                    player = cells[player.col][player.row - 1];
                } else {
                    collisionVibrate(vibrator);
                }
                break;
            case DOWN:
                if (!player.bottomWall) {
                    player = cells[player.col][player.row + 1];
                } else {
                    collisionVibrate(vibrator);
                }
                break;
            case LEFT:
                if (!(player.leftWall || player.col == 0)) {
                    player = cells[player.col - 1][player.row];
                } else {
                    collisionVibrate(vibrator);
                }
                break;
            case RIGHT:
                if (!player.rightWall) {
                    player = cells[player.col + 1][player.row];
                } else {
                    collisionVibrate(vibrator);
                }
                break;
        }

        checkExit();
        invalidate();
    }

    private void collisionVibrate(Vibrator vibrator) {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(200);
        }
    }

    private void playSound(String type) {
        int resId;

        switch (type) {
            case "START":
                resId = R.raw.start;
                break;
            case "ERROR":
                resId = R.raw.erro;
                break;
            case "EXIT":
                resId = R.raw.miseravel_genio;
                break;
            default:
                resId = 0;
                break;
        }

        mp = MediaPlayer.create(getContext(), resId);
        mp.setOnCompletionListener(this);

        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mp.release();

        switch (nextAction) {
            case ACTION_CREATE_MAZE:
                currentLevel++;
                createMaze();
                sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
                invalidate();
                break;
            case ACTION_END_GAME:
                getContext().startActivity(new Intent(getContext(),com.example.labirinto.GameOver.class));
                invalidate();
                break;
            default:
                System.out.println("Finished");
                break;
        }
    }

    private void checkExit() {
        if (player == exit) {
            if (currentLevel < MAX_LEVELS) {
                nextAction = ACTION_CREATE_MAZE;
            } else {
                nextAction = ACTION_END_GAME;
            }
            playSound("EXIT");
        } else {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        float[] gravity = {0, 0, 0};
        float[] linear_acceleration = {0, 0, 0};
        int x, y, z;
        Direction direction;


        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            /*

            Log.i("Event values",
                    String.format("X=%.2f, Y=%.2f, Z=%.2f",
                            event.values[0],
                            event.values[1],
                            event.values[2]));


            Log.i("Float Gravity",
                    String.format("X=%.2f, Y=%.2f, Z=%.2f",
                            gravity[0],
                            gravity[1],
                            gravity[2]));
            */

            // Remove the gravity contribution with the high-pass filter.
            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

            /*
            Log.i("Float acc",
                    String.format("X=%.2f, Y=%.2f, Z=%.2f",
                            linear_acceleration[0],
                            linear_acceleration[1],
                            linear_acceleration[2]));
            */


            x = (int) linear_acceleration[0];
            y = (int) linear_acceleration[1];
            z = (int) linear_acceleration[2];


            /*
            Log.i("INT acc",
                    String.format("X=%d, Y=%d, Z=%d",
                            x,
                            y,
                            z));

             */

            /*
            if (Math.abs(x) > 0) {
                sensorManager.unregisterListener(this);
                Log.i("ACCELERATOR", "Aceleração no eixo X = " + x);
                if (x > 0) {
                    movePlayer(Direction.RIGHT);
                } else {
                    movePlayer(Direction.LEFT);
                }

            } else if (Math.abs(y) > 0) {
                sensorManager.unregisterListener(this);
                if (y > 0) {
                    movePlayer(Direction.UP);
                } else {
                    movePlayer(Direction.DOWN);
                }
                Log.i("ACCELERATOR", "Aceleração no eixo Y = " + y);
            }
            */

        } else if (sensorType == Sensor.TYPE_GYROSCOPE) {

            if (event.values[0] > 0.05 || event.values[1] > 0.05) {
                Log.i("GYROSCOPE Event values",
                        String.format("X=%.2f, Y=%.2f, Z=%.2f",
                                event.values[0],
                                event.values[1],
                                event.values[2]));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float y = event.getY();

            float playerCenterX = hMargin + (player.col + 0.5f)*cellSize;
            float playerCenterY = vMargin + (player.row + 0.5f)*cellSize;

            float dx = x - playerCenterX;
            float dy = y - playerCenterY;

            float absDx = Math.abs(dx);
            float absDy = Math.abs(dy);

            if (absDx > cellSize || absDy > cellSize) {
                if (absDx > absDy) {
                    //move in x-direction
                    if (dx > 0) {
                        //move to the right
                        movePlayer(Direction.RIGHT);
                    } else {
                        //move to the left
                        movePlayer(Direction.LEFT);
                    }
                } else {
                    //move in y-direction
                    if (dy > 0) {
                        //move down
                        movePlayer(Direction.DOWN);
                    } else {
                        //move up
                        movePlayer(Direction.UP);
                    }
                }
            }
            return true;
        }

        return super.onTouchEvent(event);
    }

    private class Cell {
        boolean topWall = true, leftWall = true, bottomWall = true, rightWall = true, visited = false;

        int col , row;

        public Cell(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }
}
