package com.ichi2.apisample.ui;

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
                isMoving = true;
                layoutParams.x = (int) (motionEvent.getRawX() + dX);
                layoutParams.y = (int) (motionEvent.getRawY() + dY);
                windowManager.updateViewLayout(movableView, layoutParams);
                break;
            default:
                return false;
        }
        return true;
    }
}
