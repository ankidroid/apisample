package com.ichi2.apisample.ui;

import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class MoveViewOnTouchListener implements View.OnTouchListener {
    private final WindowManager windowManager;
    private final View movableView;

    private float dX;
    private float dY;

    private boolean isMoving;

    public MoveViewOnTouchListener(WindowManager windowManager, View movableView) {
        this.windowManager = windowManager;
        this.movableView = movableView;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) movableView.getLayoutParams();
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dX = layoutParams.x - motionEvent.getRawX();
                dY = layoutParams.y - motionEvent.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                if (!isMoving) {
                    view.performClick();
                } else {
                    isMoving = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                DisplayMetrics displayMetrics = new DisplayMetrics();
                windowManager.getDefaultDisplay().getMetrics(displayMetrics);

                int x = (int) (motionEvent.getRawX() + dX);
                int left = -displayMetrics.widthPixels / 2 + movableView.getWidth() / 2;
                int right = displayMetrics.widthPixels / 2 - movableView.getWidth() / 2;
                if (x < left) {
                    x = left;
                } else if (x > right) {
                    x = right;
                }

                int y = (int) (motionEvent.getRawY() + dY);
                int top = -displayMetrics.heightPixels / 2 + movableView.getHeight() / 2;
                int bot = displayMetrics.heightPixels / 2 - movableView.getHeight() / 2;
                if (y < top) {
                    y = top;
                } else if (y > bot) {
                    y = bot;
                }

                if (x != layoutParams.x || y != layoutParams.y) {
                    isMoving = true;
                    layoutParams.x = x;
                    layoutParams.y = y;
                    windowManager.updateViewLayout(movableView, layoutParams);
                }
                break;
            default:
                return false;
        }
        return true;
    }
}
