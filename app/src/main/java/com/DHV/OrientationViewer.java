package com.DHV;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.RectF;

import com.jjoe64.graphview.series.DataPoint;

public class OrientationViewer extends View
{
    private Paint paint = new Paint();
    private Paddle paddle;
    int w, h;

    public OrientationViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        w = getWidth();
        h = getHeight();
        paddle = new Paddle(0, 0);
    }

    int arrowHeadLen = 40;
    @Override
    public void onDraw(Canvas canvas)
    {
        if (paddle != null) {
            int mid = (int)(w / 2);

            paint.setStrokeWidth(5);
            paint.setColor(0xFFBBBBBB);
            canvas.drawOval(mid-16, mid-16,mid+16, mid+16, paint);

            paint.setColor(0xFF000000);
            RectF oval = new RectF(8, 8, w-8, w-8);
            paint.setStrokeWidth(10);
            canvas.drawOval(oval, paint);
            int rotX = (int)(Math.cos(paddle.angleX) * w / 2 * 0.9),
                rotY = (int)(Math.sin(paddle.angleX) * w / 2 * 0.9);
            paint.setStrokeWidth(15);
            canvas.drawLine(mid-rotX, mid-rotY, mid+rotX, mid+rotY, paint);
            paint.setStrokeWidth(25);
            canvas.drawLine(mid+(int)(rotX*2/3.0), mid+(int)(rotY*2/3.0), mid+(int)(rotX*.95), mid+(int)(rotY*.95), paint);
            paint.setStrokeWidth(20);
            canvas.drawLine(mid+(int)(rotX*.95), mid+(int)(rotY*.95), mid+(int)(rotX*.975), mid+(int)(rotY*.975), paint);



            paint.setStrokeWidth(5);
            paint.setColor(0xFFBBBBBB);
            canvas.drawOval(mid-16, mid+w-16,mid+16, mid+w+16, paint);

            paint.setColor(0xFF000000);
            oval = new RectF(8, w+8, w-8, w*2-8);
            paint.setStrokeWidth(10);
            canvas.drawOval(oval, paint);

            rotX = (int)(Math.cos(paddle.angleY) * w / 2 * 0.9);
            rotY = (int)(Math.sin(paddle.angleY) * w / 2 * 0.9);

            paint.setStrokeWidth(4);
            canvas.drawLine(mid-rotX, w+mid-rotY, mid+rotX, w+mid+rotY, paint);

            paint.setStrokeWidth(15);
            int n = 10;
            double hMul = 0.1;
            for(int i = 0; i < n; i++)
            {
                double perpAngle = paddle.angleY + Math.PI/2;
                double perpX = Math.cos(perpAngle),
                        perpY = Math.sin(perpAngle);

                double angle = Math.PI * i / n;
                double drawX1 = mid-rotX+rotX * (double)i/n*2 + perpX * w * hMul * Math.sin((double)i / n * Math.PI),
                        drawY1 = w+mid-rotY+rotY * (double)i/n*2 + perpY * w * hMul * Math.sin((double)i / n * Math.PI);
                double drawX2 = mid-rotX+rotX * (i+1.0)/n*2 + perpX * w * hMul * Math.sin((double)(i+1) / n * Math.PI),
                        drawY2 = w+mid-rotY+rotY * (i+1.0)/n*2 + perpY * w * hMul * Math.sin((double)(i+1) / n * Math.PI);
                canvas.drawLine((float)drawX1, (float)drawY1, (float)drawX2, (float)drawY2, paint);
                drawX1 = mid-rotX+rotX * (double)i/n*2 - perpX * w * hMul * Math.sin((double)i / n * Math.PI);
                drawY1 = w+mid-rotY+rotY * (double)i/n*2 - perpY * w * hMul * Math.sin((double)i / n * Math.PI);
                drawX2 = mid-rotX+rotX * (i+1.0)/n*2 - perpX * w * hMul * Math.sin((double)(i+1) / n * Math.PI);
                drawY2 = w+mid-rotY+rotY * (i+1.0)/n*2 - perpY * w * hMul * Math.sin((double)(i+1) / n * Math.PI);
                canvas.drawLine((float)drawX1, (float)drawY1, (float)drawX2, (float)drawY2, paint);
            }
        }
        else
            invalidate();
    }

    public void setAngles(double angleX, double angleY)
    {
        if (paddle != null) {
            paddle.angleX = angleX;
            paddle.angleY = angleY;
        }
    }

    public void translate(double deltaXAngle, double deltaYAngle)
    {

        if (paddle != null) {
            paddle.angleX += deltaXAngle;
            paddle.angleY += deltaYAngle;
        }
    }

    public class Paddle
    {
        double angleX = 0,
                angleY = 0;

        Paddle(double startAngleX, double startAngleY)
        {
            this.angleX = startAngleX;
            this.angleY = startAngleY;
        }
    }
}
