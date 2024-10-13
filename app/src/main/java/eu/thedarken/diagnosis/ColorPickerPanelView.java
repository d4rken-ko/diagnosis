/*
 * Copyright (C) 2010 Daniel Nilsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.thedarken.diagnosis;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * 이 클래스는 설정할 수 있는 색상으로 채워질 패널을 그립니다.
 * 현재 선택된 색상을 표시하는 데 사용할 수 있습니다.
 * {@link ColorPickerView}
 *
 * @author Daniel Nilsson
 */
public class ColorPickerPanelView extends View {

    /**
     * 테두리의 픽셀 단위 너비
     * 색상 패널을 둘러싸고 있습니다.
     */
    private final static float BORDER_WIDTH_PX = 1;

    private float mDensity = 1f;

    private int mBorderColor = 0xff6E6E6E;
    private int mColor = 0xff000000;

    private Paint mBorderPaint;
    private Paint mColorPaint;

    private RectF mDrawingRect;
    private RectF mColorRect;

    private AlphaPatternDrawable mAlphaPattern;


    public ColorPickerPanelView(Context context) {
        this(context, null);
    }

    public ColorPickerPanelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerPanelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mBorderPaint = new Paint();
        mColorPaint = new Paint();
        mDensity = getContext().getResources().getDisplayMetrics().density;
    }


    @Override
    protected void onDraw(Canvas canvas) {

        final RectF rect = mColorRect;

        if (BORDER_WIDTH_PX > 0) {
            mBorderPaint.setColor(mBorderColor);
            canvas.drawRect(mDrawingRect, mBorderPaint);
        }

        if (mAlphaPattern != null) {
            mAlphaPattern.draw(canvas);
        }

        mColorPaint.setColor(mColor);

        canvas.drawRect(rect, mColorPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mDrawingRect = new RectF();
        mDrawingRect.left = getPaddingLeft();
        mDrawingRect.right = w - getPaddingRight();
        mDrawingRect.top = getPaddingTop();
        mDrawingRect.bottom = h - getPaddingBottom();

        setUpColorRect();

    }

    private void setUpColorRect() {
        final RectF dRect = mDrawingRect;

        float left = dRect.left + BORDER_WIDTH_PX;
        float top = dRect.top + BORDER_WIDTH_PX;
        float bottom = dRect.bottom - BORDER_WIDTH_PX;
        float right = dRect.right - BORDER_WIDTH_PX;

        mColorRect = new RectF(left, top, right, bottom);

        mAlphaPattern = new AlphaPatternDrawable((int) (5 * mDensity));

        mAlphaPattern.setBounds(
                Math.round(mColorRect.left),
                Math.round(mColorRect.top),
                Math.round(mColorRect.right),
                Math.round(mColorRect.bottom)
        );

    }

    /**
     * 현재 이 보기에서 표시되는 색상을 가져옵니다.
     *
     * @return
     */
    public int getColor() {
        return mColor;
    }

    /**
     * 이 보기에 표시되어야 하는 색상을 설정합니다.
     *
     * @param color
     */
    public void setColor(int color) {
        mColor = color;
        invalidate();
    }

    /**
     * 패널을 둘러싼 테두리의 색상을 가져옵니다.
     */
    public int getBorderColor() {
        return mBorderColor;
    }

    /**
     * 패널을 둘러싼 테두리의 색상을 설정합니다.
     *
     * @param color
     */
    public void setBorderColor(int color) {
        mBorderColor = color;
        invalidate();
    }

}
