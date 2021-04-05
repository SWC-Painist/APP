package com.app.painist.Utils;

import android.view.MotionEvent;
import android.view.View;

public class ViewScroller {
    private View attachedView;

    public boolean fixedX = false;
    public boolean fixedY = true;

    public int leftMargin = 500;
    public int rightMargin = 100;

    private int maxLeft;
    private int maxRight;
    private int maxTop;
    private int maxBottom;

    public void addViewScrolling(View view, int bitmapWidth, int bitmapHeight) {
        attachedView = view;

        // set maximum scroll amount (based on center of image)
        int innerWidth = bitmapWidth / 2;
        int innerHeight = bitmapHeight / 2;

        // set scroll limits
        maxLeft = -innerWidth - leftMargin;
        maxRight = innerWidth + rightMargin;
        maxTop = -innerHeight;
        maxBottom = innerHeight;
    }

    private float downX, downY;
    private int totalX, totalY;

    public void updateTouchEvent(MotionEvent event) {

        float currentX, currentY;
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                currentX = event.getX();
                currentY = event.getY();
                int scrollByX = (int) (downX - currentX);
                int scrollByY = (int) (downY - currentY);

                // scrolling to left side of image (pic moving to the right)
                if (currentX > downX) {
                    if (totalX == maxLeft) scrollByX = 0;
                    if (totalX > maxLeft) totalX = totalX + scrollByX;
                    if (totalX < maxLeft) {
                        scrollByX = maxLeft - (totalX - scrollByX);
                        totalX = maxLeft;
                    }
                }

                // scrolling to right side of image (pic moving to the left)
                if (currentX < downX) {
                    if (totalX == maxRight) scrollByX = 0;
                    if (totalX < maxRight) totalX = totalX + scrollByX;
                    if (totalX > maxRight) {
                        scrollByX = maxRight - (totalX - scrollByX);
                        totalX = maxRight;
                    }
                }

                // scrolling to top of image (pic moving to the bottom)
                if (currentY > downY) {
                    if (totalY == maxTop) scrollByY = 0;
                    if (totalY > maxTop) totalY = totalY + scrollByY;
                    if (totalY < maxTop) {
                        scrollByY = maxTop - (totalY - scrollByY);
                        totalY = maxTop;
                    }
                }

                // scrolling to bottom of image (pic moving to the top)
                if (currentY < downY) {
                    if (totalY == maxBottom) scrollByY = 0;
                    if (totalY < maxBottom) totalY = totalY + scrollByY;
                    if (totalY > maxBottom) {
                        scrollByY = maxBottom - (totalY - scrollByY);
                        totalY = maxBottom;
                    }
                }

                if (fixedX) scrollByX = 0;
                if (fixedY) scrollByY = 0;
                attachedView.scrollBy(scrollByX, scrollByY);
                downX = currentX;
                downY = currentY;
                break;
        }
    }

    public void scrollToLeft() {
        attachedView.scrollBy(maxLeft - totalX, 0);
        totalX = maxLeft;
    }

    public void scrollToRight() {
        attachedView.scrollBy(maxRight - totalX, 0);
        totalX = maxRight;
    }
}
