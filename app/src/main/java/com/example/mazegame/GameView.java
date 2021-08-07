package com.example.mazegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class GameView extends View {

    // enum is a data type of self defined constants
    private enum Direction {
        UP, DOWN, RIGHT, LEFT
    }

    private Cell[][] cells;
    private Cell player, exit;
    private static final int COLS = 7, ROWS = 10;
    private float cellSize, hMargin, vMargin;
    private Paint wallPaint, playerPaint, exitPaint;
    private static final float WALL_THICKNESS = 4;
    private Random random;

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        random = new Random();

        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(WALL_THICKNESS);

        playerPaint = new Paint();
        playerPaint.setColor(Color.BLUE);

        exitPaint = new Paint();
        exitPaint.setColor(Color.RED);

        createMaze();
    }

    private Cell getNeighbour(Cell cell) {
        ArrayList<Cell> neighbours = new ArrayList<>();

        // check the left neighbour
        if (cell.col > 0) {
            if (!cells[cell.col - 1][cell.row].visited)
                neighbours.add(cells[cell.col - 1][cell.row]);
        }

        // check the right neighbour
        if (cell.col < COLS - 1) {
            if (!cells[cell.col + 1][cell.row].visited)
                neighbours.add(cells[cell.col + 1][cell.row]);
        }

        // check the top neighbour
        if (cell.row > 0) {
            if (!cells[cell.col][cell.row - 1].visited)
                neighbours.add(cells[cell.col][cell.row - 1]);
        }

        // check the bottom neighbour
        if (cell.row < ROWS - 1) {
            if (!cells[cell.col][cell.row + 1].visited)
                neighbours.add(cells[cell.col][cell.row + 1]);
        }

        if (neighbours.size() > 0) {
            // now we select a neighbour randomly out of all the neighbours
            int index = random.nextInt(neighbours.size());

            // return the neighbour with random index
            return neighbours.get(index);
        }

        // if no neighbours exists, return null
        return null;
    }

    private void removeWall(Cell current, Cell next) {

        // we have four possibilities, the current cell can be
        // to the {left, right, above, below} of the next cell.

        // current below next
        if (current.col == next.col && current.row == next.row + 1) {
            // remove the common wall
            current.topWall = false;
            next.bottomWall = false;
        }

        // current above next
        if (current.col == next.col && current.row == next.row - 1) {
            // remove the common wall
            current.bottomWall = false;
            next.topWall = false;
        }

        // current to the right of next
        if (current.col == next.col + 1 && current.row == next.row) {
            // remove the common wall
            current.leftWall = false;
            next.rightWall = false;
        }

        // current to the left next
        if (current.col == next.col - 1 && current.row == next.row) {
            // remove the common wall
            current.rightWall = false;
            next.leftWall = false;
        }


    }

    private void createMaze() {

        // creating different paths in the maze (using recursive backtrack algorithm)
        Stack<Cell> stack = new Stack<>();
        Cell current, next;

        // initializing the cell matrix
        cells = new Cell[COLS][ROWS];
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                cells[x][y] = new Cell(x, y);
            }
        }

        // setting the positions of player and exit
        player = cells[0][0];
        exit = cells[COLS - 1][ROWS - 1];

        // setting the current cell to the cell in the upper-left corner (cell[0][0])
        current = cells[0][0];

        // mark the current cell as visited
        current.visited = true;

        do {
            // choosing one random neighbour from the current cell
            next = getNeighbour(current);
            if (next != null) {
                // remove the wall between the current cell and the next cell
                removeWall(current, next);

                // put the current cell in the stack
                stack.push(current);

                // make the next cell as current cell
                current = next;

                // mark the new current cell as visited
                current.visited = true;
            } else {
                current = stack.pop();
            }
        }
        while (!stack.empty());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.GREEN);

        int width = getWidth();
        int height = getHeight();

//        if (width/height > COLS/ROWS) {
//            cellSize = width / (COLS + 1);
//        }
//        else {
//            cellSize = height / (ROWS + 1);
//        }

        if (width / COLS > height / ROWS){
            cellSize = height / (ROWS + 1);
        }
        else {
            cellSize = width / (COLS + 1);
        }


        hMargin = (width - COLS * cellSize) / 2;
        vMargin = (height - ROWS * cellSize ) / 2;

        // we translate out canvas to hMargin, vMargin so that
        // we do not need to add it in our code (below) every time
        canvas.translate(hMargin, vMargin);

        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                if (cells[x][y].topWall) {
                    canvas.drawLine(
                            x * cellSize,
                            y * cellSize,
                            (x + 1) * cellSize,
                            y * cellSize,
                            wallPaint
                            );
                }
                if (cells[x][y].leftWall) {
                    canvas.drawLine(
                            x * cellSize,
                            y * cellSize,
                            x * cellSize,
                            (y + 1) * cellSize,
                            wallPaint
                    );
                }
                if (cells[x][y].bottomWall) {
                    canvas.drawLine(
                            x * cellSize,
                            (y + 1) * cellSize,
                            (x + 1) * cellSize,
                            (y + 1) * cellSize,
                            wallPaint
                    );
                }
                if (cells[x][y].rightWall) {
                    canvas.drawLine(
                            (x + 1) * cellSize,
                            y * cellSize,
                            (x + 1) * cellSize,
                            (y + 1) * cellSize,
                            wallPaint
                    );
                }

            }
        }


        // adding margin around the player and exit cells
        float margin = cellSize / 10;

        // display the player
        canvas.drawRect(
                player.col * cellSize + margin,
                player.row * cellSize + margin,
                (player.col + 1) * cellSize - margin,
                (player.row + 1) * cellSize - margin,
                playerPaint);

        // display the exit
        canvas.drawRect(
                exit.col * cellSize + margin,
                exit.row * cellSize + margin,
                (exit.col + 1) * cellSize - margin,
                (exit.row + 1) * cellSize - margin,
                exitPaint);

    }

    // a method to move the player
    private void movePlayer(Direction direction) {
        // distinguish for cases
        switch (direction) {
            case UP:
                // when we move up, we decrease the row by 1
                // but we can only go up if there isn't a topWall
                if (!player.topWall)
                    player = cells[player.col][player.row - 1];
                break;

            case DOWN:
                // when we move down, we increase the row by 1
                // but we can only go up if there isn't a bottomWall
                if (!player.bottomWall)
                    player = cells[player.col][player.row + 1];
                break;

            case LEFT:
                // when we move left, we decrease the col by 1
                // but we can only go up if there isn't a leftWall
                if (!player.leftWall)
                    player = cells[player.col - 1][player.row];
                break;

            case RIGHT:
                // when we move right, we increase the col by 1
                // but we can only go up if there isn't a rightWall
                if (!player.rightWall)
                    player = cells[player.col + 1][player.row];
        }

        // to update the position of player on the canvas we need to call the onDraw() method
        // but we cannot call it directly so instead we call invalidate() method which calls the
        // onDraw() methods ASAP!

        // we also need to check if player reached the exit
        checkExit();
        invalidate();
    }

    // a method to check if the player reached the exit (endpoint)
    private void checkExit() {
        if (player == exit) {
            Toast.makeText(getContext().getApplicationContext(), "You've reached your destination!", Toast.LENGTH_SHORT).show();
            createMaze();
        }
    }

    // moving the player on touch events
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // ACTION_MOVE event is a part of ACTION_DOWN event, so we must return true for
        // ACTION_DOWN event only then our ACTION_MOVE event works
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            return true;

        // when touched and moved (basically dragged)
        if (event.getAction() == MotionEvent.ACTION_MOVE) {

            // position of touched place (i guess that what it is)
            float x = event.getX();
            float y = event.getY();

            // position of the center of the player
            float playerCenterX = hMargin + (player.col + 0.5f) * cellSize;
            float playerCenterY = vMargin + (player.row + 0.5f) * cellSize;

            // now we decide in which direction to move by calculating dx and dy
            float dx = x - playerCenterX;
            float dy = y - playerCenterY;

            // since the above dx and dy can be positive or negative, we calculate the absolute values
            float absDx = Math.abs(dx);
            float absDy = Math.abs(dy);

            // Now note the fact that, we only want to move the player in any direction WHEN THE DIFFERENCE BETWEEN
            // THEIR POSITION IS GREATER THAN THE CELL SIZE
            if (absDx > cellSize || absDy > cellSize) {

                // now to find the direction, we'll see which of the absDx and absDy is bigger and we'll move in that direction
                if (absDx > absDy) {
                    // move in X direction
                    // now we could either go to right or to the left in x direction
                    if (dx > 0) {
                        // move to the right
                        movePlayer(Direction.RIGHT);
                    }
                    else {
                        // move to the left
                        movePlayer(Direction.LEFT);
                    }
                }
                else {
                    // move in Y direction
                    // now we could either go in upward direction or in the downward direction
                    if (dy > 0) {
                        // move downwards
                        movePlayer(Direction.DOWN);
                    }
                    else {
                        // move upwards
                        movePlayer(Direction.UP);
                    }
                }
            }
        }
        // and we finally return true as the final output of this method
        return true;
    }

    private class Cell {
        boolean
            topWall = true,
            leftWall = true,
            bottomWall = true,
            rightWall = true,
            visited = false;

        int col, row;

        public Cell(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }
}
