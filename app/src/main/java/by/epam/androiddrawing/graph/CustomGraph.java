package by.epam.androiddrawing.graph;

/**
 * Created by Ivan_Dzinhala on 10/3/2017.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import by.epam.androiddrawing.R;

public class CustomGraph extends View{

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    public float dx,dy,startx=0f,starty=0f;

    public boolean gr_showTitle = false;
    public String gr_title = "";
    public int gr_textSize = 20;
    public int gr_graphColor = Color.rgb(0,0,0);
    public int gr_graphBackgroundColor = Color.rgb(255,255,255);
    public int gr_graphAxisColor = Color.rgb(0,0,0);
    public int gr_axisStyle = 0;//осей стиль
    public int gr_numberXmarks = 5;
    public int gr_numberYmarks = 5;
    public int gr_strokesTextSize=10;
    public boolean gr_drawGrid = false;

    public float[] input_xdata, input_ydata;
    private int[] scaled_xdata, scaled_ydata;//масштабирование данные для точек
    private int[] label_x, label_y; //координаты точек с подписями значений
    private float[] a_label_x, a_label_y;//цифры для подписи по осям

    private int graphHeight, graphWidth; //внутренний размер graphView без паддинга
    private int drawingWorkspaceHeight, drawingWorkspaceWidth; //размер области, в которой рисуем.
    private int fromWorkspaceToScalesHeight = 40, fromWorkspaceToScalesWidht = 40; //расстояние от области до осей
    private int x0, y0; // координаты перекрестия осей
    private int titleWidth;//ширина подписи графа в пикселях
    private int titleHeight;//высота подписи графа в пикселях
    private int pointingArrowIndents = 10;

    private int fromBoundsToWorkspaceTop = 40;//расстояние от края экрана до границ рисования сверху
    private int fromBoundsToWorkspaceBot = 40;//-||- снизу
    private int fromBoundsToWorkspaceLeft = 40;//-||- слева
    private int fromBoundsToWorkspaceRight = 40;//-||- справа

    private Paint axisPaint, graphPaint, labelAxisPaint, titlePaint, boundsPaint, backgroundPaint;
    private Path path;

    private float xMax;
    private float xMin;
    private float yMax;
    private float yMin;
    private int xSize;
    private int ySize;

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));


            //init();
            //onDataChanged();
            onSizeChanged(graphWidth+getPaddingLeft()+getPaddingRight(), graphHeight+getPaddingTop()+getPaddingBottom(),graphWidth, graphHeight);
            invalidate();
            return true;
        }
    }

    public CustomGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CustomGraph);
        try{
            gr_showTitle = ta.getBoolean(R.styleable.CustomGraph_showTitle, false);
            gr_title = ta.getString(R.styleable.CustomGraph_graphTitle);
            gr_textSize = ta.getDimensionPixelSize(R.styleable.CustomGraph_graphTextSize, 12);
            gr_graphColor = ta.getColor(R.styleable.CustomGraph_graphColor, Color.BLACK);
            gr_graphBackgroundColor = ta.getColor(R.styleable.CustomGraph_graphBackgroundColor, Color.WHITE);
            gr_graphAxisColor = ta.getColor(R.styleable.CustomGraph_graphAxisColor, Color.BLACK);
            gr_axisStyle = ta.getInt(R.styleable.CustomGraph_axisStyle, 0);
            gr_numberXmarks = ta.getInt(R.styleable.CustomGraph_numberXmarks, 5);
            gr_numberYmarks = ta.getInt(R.styleable.CustomGraph_numberYmarks, 5);
            gr_strokesTextSize = ta.getInt(R.styleable.CustomGraph_strokesTextSize, 15);
            gr_drawGrid = ta.getBoolean(R.styleable.CustomGraph_drawGrid, false);
            fromWorkspaceToScalesWidht = ta.getInt(R.styleable.CustomGraph_paddingSides, 40);
            fromWorkspaceToScalesHeight = ta.getInt(R.styleable.CustomGraph_paddingTopBot, 40);
            pointingArrowIndents = ta.getInt(R.styleable.CustomGraph_strokesSize, 5);

        }finally {
            ta.recycle();
        }
        init();
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        final float x,y;
        final int action = event.getAction();

        switch (action){

            case MotionEvent.ACTION_DOWN:

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startx = event.getX();
                starty = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                x = event.getX();
                y = event.getY();
                dx = x - startx;
                dy = y - starty;
                //init();
                //onDataChanged();
                onSizeChanged(graphWidth+getPaddingLeft()+getPaddingRight(), graphHeight+getPaddingTop()+getPaddingBottom(),graphWidth, graphHeight);
                invalidate();

        }
        return true;
    }


    public CustomGraph(Context context) {
        super(context);
        init();
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    private void init(){
        input_xdata = new float[]{-100,100};
        input_ydata = new float[]{-1, 1};
        graphPaint  =new Paint(Paint.ANTI_ALIAS_FLAG);
        graphPaint.setColor(gr_graphColor);
        graphPaint.setStrokeWidth(5);
        graphPaint.setStyle(Paint.Style.STROKE);
        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(gr_graphAxisColor);
        axisPaint.setStrokeWidth(5);
        axisPaint.setStyle(Paint.Style.STROKE);

        labelAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelAxisPaint.setColor(gr_graphAxisColor);
        labelAxisPaint.setStrokeWidth(1);
        labelAxisPaint.setStyle(Paint.Style.STROKE);
        labelAxisPaint.setTextSize(gr_strokesTextSize);

        boundsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boundsPaint.setColor(gr_graphAxisColor);
        boundsPaint.setStrokeWidth(5);
        boundsPaint.setStyle(Paint.Style.STROKE);

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(gr_graphAxisColor);
        titlePaint.setStrokeWidth(1);
        titlePaint.setLetterSpacing(1);
        titlePaint.setStyle(Paint.Style.STROKE);
        titlePaint.setTextSize(gr_textSize);
        Rect titleWidthRect = new Rect();
        titlePaint.getTextBounds(gr_title, 0, gr_title.length(), titleWidthRect);
        titleWidth = titleWidthRect.width();

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(gr_graphBackgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setStrokeWidth(5);

        gr_numberXmarks++;
        gr_numberYmarks++;

        path = new Path();
    }

    private void onDataChanged(){
        //пересчитывать точки которые введены в то что будет выводиться
        xMax=xMin=input_xdata[0];
        yMax=yMin=input_ydata[0];
        for(int i=0;i<input_xdata.length;i++){
            if(input_ydata[i]<yMin)yMin=input_ydata[i];
            if(input_ydata[i]>yMax)yMax=input_ydata[i];
            if(input_xdata[i]<xMin)xMin=input_xdata[i];
            if(input_xdata[i]>xMax)xMax=input_xdata[i];
        }

        if(Math.abs(xMin)>=Math.abs(xMax))xSize=(int)Math.ceil(Math.abs(xMin)*2);
        else xSize=(int)Math.ceil(Math.abs(xMax)*2);

        if(Math.abs(yMin)>=Math.abs(yMax))ySize=(int)Math.ceil(Math.abs(yMin)*2);
        else ySize=(int)Math.ceil(Math.abs(yMax)*2);

    }
    public void setData(float[] x, float[] y){
        input_xdata=new float[x.length];
        input_ydata = new float[y.length];
        input_xdata=x;
        input_ydata=y;
        onDataChanged();
        invalidate();
        //requestLayout();//вызывает метод размещающий дочерние компоненты на лайауте (если размеры или форма view поменялись динамически)
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w,h,oldw,oldh);

        graphWidth = w-getPaddingLeft() - getPaddingRight();
        graphHeight = h-getPaddingTop() - getPaddingBottom();

        drawingWorkspaceWidth = graphWidth - fromBoundsToWorkspaceLeft - fromBoundsToWorkspaceRight;
        drawingWorkspaceHeight = graphHeight - fromBoundsToWorkspaceTop - fromBoundsToWorkspaceBot;
        x0 = graphWidth/2;
        y0 = graphHeight/2;
//        if(gr_showTitle){
//            x0 = graphWidth/2;
//
//            int textSpace=gr_textSize*2;
//            fromBoundsToWorkspaceTop = fromBoundsToWorkspaceTop + textSpace;
//            drawingWorkspaceHeight-=textSpace;
//
//            y0=(drawingWorkspaceHeight/2) + fromBoundsToWorkspaceTop;
//        }else{
//            x0 = graphWidth/2;
//            y0 = graphHeight/2;
//        }
        onDataChanged();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.scale(mScaleFactor, mScaleFactor);
        canvas.translate(dx,dy);

        drawBounds(canvas);
        if(gr_showTitle){
            drawTitle(canvas);
        }
        drawAxes(canvas);
        drawStrokes(canvas);
        if(gr_drawGrid){
            drawGrid(canvas);
        }
        drawPoints(canvas);

        path.reset();
    }

    private void drawGrid(Canvas canvas) {
        drawXgrid(canvas);
        drawYgrid(canvas);
    }

    private void drawYgrid(Canvas canvas) {
        path.reset();
        float fromY = y0;
        float toY = y0 + drawingWorkspaceHeight / 2 - fromWorkspaceToScalesHeight;

        int xLeft = x0 - drawingWorkspaceWidth/2 + fromWorkspaceToScalesWidht;
        int xRight = x0 + drawingWorkspaceWidth/2 - fromWorkspaceToScalesWidht;

        if(gr_axisStyle==1 || gr_axisStyle==2){
            xLeft = x0;
            xRight = x0 + drawingWorkspaceWidth - fromWorkspaceToScalesWidht*2;
        }

        float step = 2 * (toY - fromY) / ySize;
        if (gr_axisStyle == 2) {
            step *= 2;
        }
        float maxYabs = (float) Math.ceil((Math.abs(yMax) > Math.abs(yMin) ? Math.abs(yMax) : Math.abs(yMin)));
        float strokeStep = maxYabs / gr_numberYmarks;

        for (float i = 0; i < maxYabs; i += strokeStep) {
            float drawValue = (float) (Math.rint(10.0 * i) / 10.0);
            if (gr_axisStyle != 2) {
                path.moveTo(xLeft, y0 + step * drawValue);
                path.lineTo(xRight, y0 + step * drawValue);
                canvas.drawPath(path, axisPaint);
                path.reset();
            }

            path.moveTo(xLeft, y0 - step * drawValue);
            path.lineTo(xRight, y0 - step * drawValue);
            canvas.drawPath(path, axisPaint);
            path.reset();

        }
    }

    private void drawXgrid(Canvas canvas) {
        path.reset();
        float fromX = x0;
        float toX = fromX + drawingWorkspaceWidth / 2 - fromWorkspaceToScalesWidht;

        int yTop = y0 - drawingWorkspaceHeight/2 + fromWorkspaceToScalesHeight;
        int yBot = y0 + drawingWorkspaceHeight/2 - fromWorkspaceToScalesHeight;

        float step = 2 * (toX - fromX) / xSize;
        if(gr_axisStyle==1 || gr_axisStyle==2){
            step*=2;
        }
        if(gr_axisStyle==2){
            yTop = y0 - drawingWorkspaceHeight + fromWorkspaceToScalesHeight*2;
            yBot = y0;
        }
        float maxXabs=(float)Math.ceil((Math.abs(xMax)>Math.abs(xMin)?Math.abs(xMax):Math.abs(xMin)));
        float strokeStep = maxXabs/gr_numberXmarks;
        for (float i = 0; i < maxXabs; i+=strokeStep) {
            float drawValue = (float)(Math.rint(10.0 * i) / 10.0);
            path.moveTo(fromX + step * drawValue, yTop);
            path.lineTo(fromX + step * drawValue, yBot);
            canvas.drawPath(path, axisPaint);
            path.reset();
            if(gr_axisStyle==0) {
                path.moveTo(fromX - step * drawValue, yTop);
                path.lineTo(fromX - step * drawValue, yBot);
                canvas.drawPath(path, axisPaint);
                path.reset();
            }

        }
    }

    private Float getIntersectionX(float x, float y, float prevX, float prevY){
        float a,c;
        a=(prevY-y);
        c=prevX*y-x*prevY;
        return -c/a;

    }
    private Float getIntersectionY(float x, float y, float prevX, float prevY){
        float b,c;
        b=(x-prevX);
        c=prevX*y-x*prevY;
        return -c/b;

    }

    private void drawPoints(Canvas canvas) {
        path.reset();
        float fromX=x0-drawingWorkspaceWidth/2 + fromWorkspaceToScalesWidht;
        float toX=x0+drawingWorkspaceWidth/2 - fromWorkspaceToScalesWidht;

        float fromY=y0-drawingWorkspaceHeight/2 + fromWorkspaceToScalesHeight;
        float toY=y0+drawingWorkspaceHeight/2 - fromWorkspaceToScalesHeight;

        float Xstep = (toX - fromX)/xSize;
        if(gr_axisStyle==1 || gr_axisStyle==2){
            Xstep*=2;
        }
        float Ystep = (toY - fromY)/ySize;
        if(gr_axisStyle==2){
            Ystep*=2;
        }

        for(int i = 0; i < input_xdata.length ; i++){
            float resX=x0+Xstep*input_xdata[i];
            float resY=y0-Ystep*input_ydata[i];
            if(i==0){
                path.moveTo(resX,resY);
                continue;
            }
            if(gr_axisStyle==2){
                float prevX=x0+Xstep*input_xdata[i-1];
                float prevY=y0-Ystep*input_ydata[i-1];
                if(input_xdata[i]>0 && input_ydata[i]<0){
                    if(input_xdata[i-1]>=0 && input_ydata[i-1]>=0){
                        canvas.drawPath(path,graphPaint);
                        path.reset();
                        path.moveTo(prevX, prevY);
                        path.lineTo((resX+prevX)/2,y0);
                    }
                    if(input_xdata[i-1]<0 && input_ydata[i-1]>0){
                        float intersectX = (resX+prevX)/2;
                        float intersectY = (resY+prevY)/2;
                        if(intersectX>0 && intersectY>0){
                            canvas.drawPath(path,graphPaint);
                            path.reset();
                            path.moveTo(x0,intersectY);
                            path.lineTo(intersectX, y0);
                        }

                    }
                }
                if(input_xdata[i]>=0 && input_ydata[i]>=0){
                    if(input_xdata[i-1]>0 && input_ydata[i-1]<0) {
                        float intersectX = (resX + prevX) / 2;
                        if (input_ydata[i] == 0) {
                            continue;
                        }
                        canvas.drawPath(path, graphPaint);
                        path.reset();
                        path.moveTo(intersectX, y0);
                        path.lineTo(resX, resY);

                    }
                    if(input_xdata[i-1]>=0 && input_ydata[i-1]>=0){
                        canvas.drawPath(path, graphPaint);
                        path.reset();
                        path.moveTo(prevX, prevY);
                        path.lineTo(resX, resY);
                    }
                    if(input_xdata[i-1]<0 && input_ydata[i-1]>0){
                        if(input_xdata[i]==0){
                            continue;
                        }
                        float intersectY = (resY+prevY)/2;
                        canvas.drawPath(path, graphPaint);
                        path.reset();
                        path.moveTo(x0, intersectY);
                        path.lineTo(resX, resY);
                    }
                    if(input_xdata[i-1]<=0 && input_ydata[i-1]<=0){
                        if(input_xdata[i]==0 || input_ydata[i]==0){
                            continue;
                        }
                        float intersectX = (resX+prevX)/2;
                        float intersectY = (resY+prevY)/2;
                        if(intersectX>0 && intersectY>0){
                            canvas.drawPath(path,graphPaint);
                            path.reset();
                            path.moveTo(intersectX,y0);
                            path.lineTo(resX, resY);
                        }

                    }
                }
                if(input_xdata[i]<0 && input_ydata[i]>0){
                    if(input_xdata[i-1]>0 && input_ydata[i-1]<0) {
                        float intersectX = (resX+prevX)/2;
                        float intersectY = (resY+prevY)/2;
                        if(intersectX>0 && intersectY>0){
                            canvas.drawPath(path,graphPaint);
                            path.reset();
                            path.moveTo(intersectX, y0);
                            path.lineTo(x0, intersectY);
                        }
                    }
                    if(input_xdata[i-1]>=0 && input_ydata[i-1]>=0){
                        if(input_xdata[i]==0){
                            continue;
                        }
                        float intersectY = (resY+prevY)/2;
                        canvas.drawPath(path,graphPaint);
                        path.reset();
                        path.moveTo(prevX, prevY);
                        path.lineTo(x0, intersectY);
                    }
                }
                if(input_xdata[i]<=0 && input_ydata[i]<=0) {
                    if (input_xdata[i - 1] >= 0 && input_ydata[i - 1] >= 0) {
                        float intersectX = getIntersectionX(resX,resY,prevX,prevY);
                        float intersectY = getIntersectionY(resX,resY,prevX,prevY);
                        if(intersectX>=0){
                            canvas.drawPath(path, graphPaint);
                            path.reset();
                            path.moveTo(prevX, prevY);
                            path.lineTo(intersectX,y0);
                            continue;
                        }else if (intersectY>=0){
                            canvas.drawPath(path, graphPaint);
                            path.reset();
                            path.moveTo(prevX, prevY);
                            path.lineTo(x0, intersectY);
                        }

                    }
                }
                continue;
            }
            path.lineTo(resX,resY);
        }
        canvas.drawPath(path,graphPaint);
        path.reset();
    }

    private void drawStrokes(Canvas canvas) {
        drawXstrokes(canvas);
        drawYstrokes(canvas);
    }

    private void drawXstrokes(Canvas canvas) {
        path.reset();
        float fromX = x0;
        float toX = x0 + drawingWorkspaceWidth / 2 - fromWorkspaceToScalesWidht;

        int yTop = y0 - pointingArrowIndents;
        int yBot = y0 + pointingArrowIndents;

        float step = 2 * (toX - fromX) / xSize;
        if(gr_axisStyle==1 || gr_axisStyle==2){
            step*=2;
        }
        float maxXabs=(float)Math.ceil((Math.abs(xMax)>Math.abs(xMin)?Math.abs(xMax):Math.abs(xMin)));
        float strokeStep = maxXabs/gr_numberXmarks;
        for (float i = 0; i < maxXabs; i+=strokeStep) {
            float drawValue = (float)(Math.rint(10.0 * i) / 10.0);
            path.moveTo(x0 + step * drawValue, yTop);
            path.lineTo(x0 + step * drawValue, yBot);
            canvas.drawPath(path, axisPaint);
            path.reset();
            if (i != 0) {
                Rect labelWidthRect = new Rect();
                labelAxisPaint.getTextBounds(String.valueOf(drawValue), 0, String.valueOf(drawValue).length(), labelWidthRect);
                int labelWidth = labelWidthRect.width();
                int labelHeight = labelWidthRect.height();

                float indent = labelWidth/2;
                if(gr_drawGrid){
                    if(gr_axisStyle !=2){
                        indent = 3*labelWidth/2;
                    }
                }
                float widhtFrom = x0 + step * drawValue - indent;
                int heightFrom = y0 + pointingArrowIndents + labelHeight + 4;
                path.moveTo(widhtFrom, heightFrom);
                path.lineTo(widhtFrom + labelWidth, heightFrom);
                canvas.drawTextOnPath(String.valueOf(drawValue), path, 0, 0, labelAxisPaint);
                path.reset();

                if(gr_axisStyle==0) {
                    labelAxisPaint.getTextBounds(String.valueOf(-drawValue), 0, String.valueOf(-drawValue).length(), labelWidthRect);
                    labelWidth = labelWidthRect.width();
                    widhtFrom = x0 - step * drawValue - (gr_drawGrid?(3*labelWidth/2):(labelWidth / 2));
                    path.moveTo(widhtFrom, heightFrom);
                    path.lineTo(widhtFrom + labelWidth, heightFrom);
                    canvas.drawTextOnPath(String.valueOf(-drawValue), path, 0, 0, labelAxisPaint);
                    path.reset();
                }
            } else {
                Rect labelWidthRect = new Rect();
                labelAxisPaint.getTextBounds(String.valueOf(0), 0, String.valueOf(0).length(), labelWidthRect);
                int labelWidth = labelWidthRect.width();
                int labelHeight = labelWidthRect.height();

                if(gr_axisStyle==0) {
                    float widhtFrom = x0 - labelWidth * 5 / 3;
                    int heightFrom = y0 + pointingArrowIndents + labelHeight + 3;
                    path.moveTo(widhtFrom, heightFrom);
                    path.lineTo(widhtFrom + labelWidth, heightFrom);
                    canvas.drawTextOnPath(String.valueOf(0), path, 0, 0, labelAxisPaint);
                    path.reset();
                }else if (gr_axisStyle==1 || gr_axisStyle==2){
                    float widhtFrom = x0 - pointingArrowIndents - labelWidth - pointingArrowIndents * 3 / 2;//
                    float heightFrom = y0 - step * drawValue + labelHeight / 2;
                    path.moveTo(widhtFrom, heightFrom);
                    path.lineTo(widhtFrom + labelWidth, heightFrom);
                    canvas.drawTextOnPath(String.valueOf(0), path, 0, 0, labelAxisPaint);
                    path.reset();
                }
            }
            if(gr_axisStyle==0) {
                path.moveTo(x0 - step * drawValue, yTop);
                path.lineTo(x0 - step * drawValue, yBot);
                canvas.drawPath(path, axisPaint);
                path.reset();
            }

        }
    }

    private void drawYstrokes(Canvas canvas) {
        path.reset();
        float fromY = y0;
        float toY = y0 + drawingWorkspaceHeight / 2 - fromWorkspaceToScalesHeight;

        int xLeft = x0 - pointingArrowIndents;
        int xRight = x0 + pointingArrowIndents;

        float step = 2 * (toY - fromY) / ySize;
        if (gr_axisStyle == 2) {
            step *= 2;
        }
        float maxYabs = (float) Math.ceil((Math.abs(yMax) > Math.abs(yMin) ? Math.abs(yMax) : Math.abs(yMin)));
        float strokeStep = maxYabs / gr_numberYmarks;

        for (float i = 0; i < maxYabs; i += strokeStep) {
            float drawValue = (float) (Math.rint(10.0 * i) / 10.0);
            if (gr_axisStyle != 2) {
                path.moveTo(xLeft, y0 + step * drawValue);
                path.lineTo(xRight, y0 + step * drawValue);
                canvas.drawPath(path, axisPaint);
                path.reset();
            }
            if (i != 0) {
                Rect labelWidthRect = new Rect();
                labelAxisPaint.getTextBounds(String.valueOf(drawValue), 0, String.valueOf(drawValue).length(), labelWidthRect);
                int labelWidth = labelWidthRect.width();
                int labelHeight = labelWidthRect.height();

                float widhtFrom = x0 - pointingArrowIndents - labelWidth - pointingArrowIndents * 3 / 2;//
                float indent  = - labelHeight/2;
                if(gr_drawGrid){
                    if(gr_axisStyle==0){
                        indent = labelHeight;
                    }
                }
                float heightFrom = y0 - step * drawValue - indent;
                path.moveTo(widhtFrom, heightFrom);
                path.lineTo(widhtFrom + labelWidth, heightFrom);
                canvas.drawTextOnPath(String.valueOf(drawValue), path, 0, 0, labelAxisPaint);
                path.reset();

                if (gr_axisStyle != 2) {
                    labelAxisPaint.getTextBounds(String.valueOf(-drawValue), 0, String.valueOf(-drawValue).length(), labelWidthRect);
                    labelWidth = labelWidthRect.width();
                    heightFrom = y0 + step * drawValue - indent;
                    path.moveTo(widhtFrom, heightFrom);
                    path.lineTo(widhtFrom + labelWidth, heightFrom);
                    canvas.drawTextOnPath(String.valueOf(-drawValue), path, 0, 0, labelAxisPaint);
                    path.reset();
                }
            }

            path.moveTo(xLeft, y0 - step * drawValue);
            path.lineTo(xRight, y0 - step * drawValue);
            canvas.drawPath(path, axisPaint);
            path.reset();

        }
    }

    private void drawTitle(Canvas canvas) {
        int widhtFrom=x0 - titleWidth / 2;
        int heightFrom = fromBoundsToWorkspaceTop - gr_textSize*7/10;
        path.reset();
        path.moveTo(widhtFrom,heightFrom);
        path.lineTo(widhtFrom + titleWidth,heightFrom);
        canvas.drawTextOnPath(gr_title,path,0,0,titlePaint);
        path.reset();
    }

    private void drawAxes(Canvas canvas) {
        if(gr_axisStyle==0) {
            canvas.drawCircle(x0, y0, 5, axisPaint);
            path.reset();
            path.moveTo(x0, y0 - drawingWorkspaceHeight / 2 + fromWorkspaceToScalesHeight);
            if(!gr_drawGrid) {
                path.moveTo(x0 - pointingArrowIndents, y0 - drawingWorkspaceHeight / 2 + fromWorkspaceToScalesHeight + pointingArrowIndents);
                path.quadTo(x0, y0 - drawingWorkspaceHeight / 2 + fromWorkspaceToScalesHeight - pointingArrowIndents, x0 + pointingArrowIndents, y0 - drawingWorkspaceHeight / 2 + fromWorkspaceToScalesHeight + pointingArrowIndents);
                path.moveTo(x0, y0 - drawingWorkspaceHeight / 2 + fromWorkspaceToScalesHeight);
            }

            path.lineTo(x0, y0 + drawingWorkspaceHeight / 2 - fromWorkspaceToScalesHeight);
            if(!gr_drawGrid) {
                path.moveTo(x0 - pointingArrowIndents, y0 + drawingWorkspaceHeight / 2 - fromWorkspaceToScalesHeight - pointingArrowIndents);
                path.quadTo(x0, y0 + drawingWorkspaceHeight / 2 - fromWorkspaceToScalesHeight + pointingArrowIndents, x0 + pointingArrowIndents, y0 + drawingWorkspaceHeight / 2 - fromWorkspaceToScalesHeight - pointingArrowIndents);
                path.moveTo(x0, y0 + drawingWorkspaceHeight / 2 - fromWorkspaceToScalesHeight);
            }


            path.moveTo(x0 - drawingWorkspaceWidth / 2 + fromWorkspaceToScalesWidht, y0);
            if(!gr_drawGrid) {
                path.moveTo(x0 - drawingWorkspaceWidth / 2 + fromWorkspaceToScalesWidht + pointingArrowIndents, y0 - pointingArrowIndents);
                path.quadTo(x0 - drawingWorkspaceWidth / 2 + fromWorkspaceToScalesWidht - pointingArrowIndents, y0, x0 - drawingWorkspaceWidth / 2 + fromWorkspaceToScalesWidht + pointingArrowIndents, y0 + pointingArrowIndents);
                path.moveTo(x0 - drawingWorkspaceWidth / 2 + fromWorkspaceToScalesWidht, y0);
            }

            path.lineTo(x0 + drawingWorkspaceWidth / 2 - fromWorkspaceToScalesWidht, y0);
            if(!gr_drawGrid) {
                path.moveTo(x0 + drawingWorkspaceWidth / 2 - fromWorkspaceToScalesWidht - pointingArrowIndents, y0 + pointingArrowIndents);
                path.quadTo(x0 + drawingWorkspaceWidth / 2 - fromWorkspaceToScalesWidht + pointingArrowIndents, y0, x0 + drawingWorkspaceWidth / 2 - fromWorkspaceToScalesWidht - pointingArrowIndents, y0 - pointingArrowIndents);
            }
            canvas.drawPath(path, axisPaint);

            path.reset();
        } else if(gr_axisStyle==1){
            x0=x0-drawingWorkspaceWidth/2 + fromWorkspaceToScalesWidht;
            canvas.drawCircle(x0, y0, 5, axisPaint);
            path.reset();
            path.moveTo(x0, y0 - drawingWorkspaceHeight / 2 + fromWorkspaceToScalesHeight);
            if(!gr_drawGrid) {
                path.moveTo(x0 - pointingArrowIndents, y0 - drawingWorkspaceHeight / 2 + fromWorkspaceToScalesHeight + pointingArrowIndents);
                path.quadTo(x0, y0 - drawingWorkspaceHeight / 2 + fromWorkspaceToScalesHeight - pointingArrowIndents, x0 + pointingArrowIndents, y0 - drawingWorkspaceHeight / 2 + fromWorkspaceToScalesHeight + pointingArrowIndents);
                path.moveTo(x0, y0 - drawingWorkspaceHeight / 2 + fromWorkspaceToScalesHeight);
            }

            path.lineTo(x0, y0 + drawingWorkspaceHeight / 2 - fromWorkspaceToScalesHeight);
            if(!gr_drawGrid) {
                path.moveTo(x0 - pointingArrowIndents, y0 + drawingWorkspaceHeight / 2 - fromWorkspaceToScalesHeight - pointingArrowIndents);
                path.quadTo(x0, y0 + drawingWorkspaceHeight / 2 - fromWorkspaceToScalesHeight + pointingArrowIndents, x0 + pointingArrowIndents, y0 + drawingWorkspaceHeight / 2 - fromWorkspaceToScalesHeight - pointingArrowIndents);
                path.moveTo(x0, y0 + drawingWorkspaceHeight / 2 - fromWorkspaceToScalesHeight);
            }


            path.moveTo(x0, y0);
            path.lineTo(x0 + drawingWorkspaceWidth  - fromWorkspaceToScalesWidht*2 , y0);
            if(!gr_drawGrid) {
                path.moveTo(x0 + drawingWorkspaceWidth - fromWorkspaceToScalesWidht * 2 - pointingArrowIndents, y0 - pointingArrowIndents);
                path.quadTo(x0 + drawingWorkspaceWidth - fromWorkspaceToScalesWidht * 2, y0, x0 + drawingWorkspaceWidth - fromWorkspaceToScalesWidht * 2 - pointingArrowIndents, y0 + pointingArrowIndents);
            }
            canvas.drawPath(path, axisPaint);

            path.reset();
        } else if(gr_axisStyle==2) {
            x0 = x0 - drawingWorkspaceWidth / 2 + fromWorkspaceToScalesWidht;
            y0 = y0 + drawingWorkspaceHeight / 2 - fromWorkspaceToScalesHeight;
            canvas.drawCircle(x0, y0, 5, axisPaint);
            path.reset();
            path.moveTo(x0, y0);
            path.lineTo(x0, y0 - drawingWorkspaceHeight + fromWorkspaceToScalesHeight * 2);
            if (!gr_drawGrid) {
                path.moveTo(x0 - pointingArrowIndents, y0 - drawingWorkspaceHeight + fromWorkspaceToScalesHeight * 2 + pointingArrowIndents);
                path.quadTo(x0, y0 - drawingWorkspaceHeight + fromWorkspaceToScalesHeight * 2 - pointingArrowIndents, x0 + pointingArrowIndents, y0 - drawingWorkspaceHeight + fromWorkspaceToScalesHeight * 2 + pointingArrowIndents);
            }
            path.moveTo(x0, y0);

            path.lineTo(x0 + drawingWorkspaceWidth - fromWorkspaceToScalesWidht * 2, y0);
            if(!gr_drawGrid) {
                path.moveTo(x0 + drawingWorkspaceWidth - fromWorkspaceToScalesWidht * 2 - pointingArrowIndents, y0 + pointingArrowIndents);
                path.quadTo(x0 + drawingWorkspaceWidth - fromWorkspaceToScalesWidht * 2 + pointingArrowIndents, y0, x0 + drawingWorkspaceWidth - fromWorkspaceToScalesWidht * 2 - pointingArrowIndents, y0 - pointingArrowIndents);
            }
            canvas.drawPath(path, axisPaint);

            path.reset();
        }
    }

    private void drawBounds(Canvas canvas){
        path.reset();
        int textSize = gr_showTitle?gr_textSize*2:0;
        path.moveTo(fromBoundsToWorkspaceLeft, fromBoundsToWorkspaceTop - textSize);
        path.lineTo(fromBoundsToWorkspaceLeft + drawingWorkspaceWidth, fromBoundsToWorkspaceTop - textSize);
        if(gr_showTitle) {
            path.lineTo(fromBoundsToWorkspaceLeft + drawingWorkspaceWidth, fromBoundsToWorkspaceTop);
            path.lineTo(fromBoundsToWorkspaceLeft, fromBoundsToWorkspaceTop);
            path.lineTo(fromBoundsToWorkspaceLeft, fromBoundsToWorkspaceTop - gr_textSize * 2);
        }
        else{
            path.moveTo(fromBoundsToWorkspaceLeft, fromBoundsToWorkspaceTop);
        }
        path.lineTo(fromBoundsToWorkspaceLeft,fromBoundsToWorkspaceTop+drawingWorkspaceHeight);
        path.lineTo(fromBoundsToWorkspaceLeft+drawingWorkspaceWidth,fromBoundsToWorkspaceTop+drawingWorkspaceHeight);
        path.lineTo(fromBoundsToWorkspaceLeft+drawingWorkspaceWidth,fromBoundsToWorkspaceTop);
        canvas.drawPath(path,boundsPaint);

        path.reset();

        path.moveTo(fromBoundsToWorkspaceLeft+5/2,fromBoundsToWorkspaceTop+5/2);
        path.lineTo(fromBoundsToWorkspaceLeft+5/2,fromBoundsToWorkspaceTop+drawingWorkspaceHeight-5/2);
        path.lineTo(fromBoundsToWorkspaceLeft+drawingWorkspaceWidth-5/2,fromBoundsToWorkspaceTop+drawingWorkspaceHeight-5/2);
        path.lineTo(fromBoundsToWorkspaceLeft+drawingWorkspaceWidth-5/2,fromBoundsToWorkspaceTop+5/2);
        path.lineTo(fromBoundsToWorkspaceLeft+5/2,fromBoundsToWorkspaceTop+5/2);
        canvas.drawPath(path,backgroundPaint);

        path.reset();
    }

}