package cn.jackwhliu.rvadapter.lib.decoration;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GridItemDecoration extends RecyclerView.ItemDecoration {

    private int mSpace;
    private int mColor = -1;
    private Drawable mDivider;
    private Paint mPaint;
    private int mType;

    public GridItemDecoration(int space) {
        this.mSpace = space;
    }

    public GridItemDecoration(int space, int color) {
        this.mSpace = space;
        this.mColor = color;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(space * 2);
    }

    public GridItemDecoration(int space, int color, int type) {
        this.mSpace = space;
        this.mColor = color;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(space * 2);
        this.mType = type;
    }

    public GridItemDecoration(int space, Drawable mDivider) {
        this.mSpace = space;
        this.mDivider = mDivider;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        if (parent.getLayoutManager() != null) {
            if (parent.getLayoutManager() instanceof LinearLayoutManager && !(parent.getLayoutManager() instanceof GridLayoutManager)) {
                if (((LinearLayoutManager) parent.getLayoutManager()).getOrientation() == LinearLayoutManager.HORIZONTAL) {
                    outRect.set(mSpace, 0, mSpace, 0);
                } else {
                    outRect.set(0, mSpace, 0, mSpace);
                }
            } else {
                outRect.set(mSpace, mSpace, mSpace, mSpace);
            }
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (parent.getLayoutManager() != null) {
            if (parent.getLayoutManager() instanceof LinearLayoutManager && !(parent.getLayoutManager() instanceof GridLayoutManager)) {
                if (((LinearLayoutManager) parent.getLayoutManager()).getOrientation() == LinearLayoutManager.HORIZONTAL) {
                    drawHorizontal(c, parent);
                } else {
                    drawVertical(c, parent);
                }
            } else {
                if (mType == 0) {
                    drawGridDivideLine(c, parent);
                } else {
                    drawGridFullDivideLine(c, parent);
                }
            }
        }
    }

    /**
     * 绘制纵向 item 分割线。
     *
     * @param canvas 画布。
     * @param parent RV。
     */
    private void drawVertical(Canvas canvas, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + layoutParams.rightMargin;
            final int right = left + mSpace;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }

    /**
     * 绘制横向item分割线。
     *
     * @param canvas 画布。
     * @param parent RV。
     */
    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        int left = parent.getPaddingLeft();
        int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getBottom() + layoutParams.bottomMargin;
            int bottom = top + mSpace;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }

    /**
     * 绘制item分割线，不是填充满的。
     *
     * @param canvas 画布。
     * @param parent RV。
     */
    private void drawGridDivideLine(Canvas canvas, RecyclerView parent) {
        GridLayoutManager linearLayoutManager = (GridLayoutManager) parent.getLayoutManager();
        int childSize = parent.getChildCount();
        int other = parent.getChildCount() / linearLayoutManager.getSpanCount() - 1;
        if (other < 1) {
            other = 1;
        }
        other = other * linearLayoutManager.getSpanCount();
        if (parent.getChildCount() < linearLayoutManager.getSpanCount()) {
            other = parent.getChildCount();
        }
        int top, bottom, left, right, spancount;
        spancount = linearLayoutManager.getSpanCount() - 1;
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            if (i < other) {
                top = child.getBottom() + layoutParams.bottomMargin;
                bottom = top + mSpace;
                left = (layoutParams.leftMargin + mSpace) * (i + 1);
                right = child.getMeasuredWidth() * (i + 1) + left + mSpace * i;
                if (mDivider != null) {
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(canvas);
                }
                if (mPaint != null) {
                    canvas.drawRect(left, top, right, bottom, mPaint);
                }
            }
            if (i != spancount) {
                top = (layoutParams.topMargin + mSpace) * (i / linearLayoutManager.getSpanCount() + 1);
                bottom = (child.getMeasuredHeight() + mSpace) * (i / linearLayoutManager.getSpanCount() + 1) + mSpace;
                left = child.getRight() + layoutParams.rightMargin;
                right = left + mSpace;
                if (mDivider != null) {
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(canvas);
                }
                if (mPaint != null) {
                    canvas.drawRect(left, top, right, bottom, mPaint);
                }
            } else {
                spancount += 4;
            }
        }
    }

    private void drawGridFullDivideLine(Canvas canvas, RecyclerView parent) {
        GridLayoutManager linearLayoutManager = (GridLayoutManager) parent.getLayoutManager();
        int childSize = parent.getChildCount();
        int top, bottom, left, right,spancount;
        spancount=linearLayoutManager.getSpanCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            // 画横线
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            top = child.getBottom() + layoutParams.bottomMargin;
            bottom = top + mSpace;
            left = layoutParams.leftMargin+child.getPaddingLeft()+mSpace;
            right = child.getMeasuredWidth() * (i + 1) + left + mSpace * i;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
            // 画竖线
            top = (layoutParams.topMargin + mSpace) * (i / linearLayoutManager.getSpanCount() + 1);
            bottom = (child.getMeasuredHeight() + mSpace) * (i / linearLayoutManager.getSpanCount() + 1) + mSpace;
            left = child.getRight() + layoutParams.rightMargin;
            right = left + mSpace;
            if (mDivider != null) {
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
            if (mPaint != null) {
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
            // 画上缺失的线框
            if(i<spancount){
                top = child.getTop() + layoutParams.topMargin;
                bottom = top + mSpace;
                left = (layoutParams.leftMargin + mSpace) * (i + 1);
                right = child.getMeasuredWidth() * (i + 1) + left + mSpace * i;
                if (mDivider != null) {
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(canvas);
                }
                if (mPaint != null) {
                    canvas.drawRect(left, top, right, bottom, mPaint);
                }
            }
            if (i%spancount==0) {
                top = (layoutParams.topMargin + mSpace) * (i / linearLayoutManager.getSpanCount() + 1);
                bottom = (child.getMeasuredHeight() + mSpace) * (i / linearLayoutManager.getSpanCount() + 1) + mSpace;
                left = child.getLeft() + layoutParams.leftMargin;
                right = left + mSpace;
                if (mDivider != null) {
                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(canvas);
                }
                if (mPaint != null) {
                    canvas.drawRect(left, top, right, bottom, mPaint);
                }
            }
        }
    }
}