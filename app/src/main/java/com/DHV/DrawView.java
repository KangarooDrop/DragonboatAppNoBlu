package com.DHV;

import android.graphics.Canvas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.app.Activity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.jjoe64.graphview.series.DataPoint;

public class DrawView extends View
{
    private Paint paint = new Paint();
    private Line3D line;

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(15);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        int w = getWidth(),
                h = getHeight();
        line = new Line3D(w > h ? h : w, 0, 0);
    }

    int arrowHeadLen = 40;
    @Override
    public void onDraw(Canvas canvas)
    {
        if (line != null) {
            double mid = line.length / 2;
            DataPoint start = line.getStartPos();
            DataPoint end = line.getEndPos();
            canvas.drawLine((int) (mid + start.getX()), (int) (mid + start.getY()),
                    (int) (mid + end.getX()), (int) (mid + end.getY()), paint);
            double arrowHeadAngle = Math.atan2(start.getY(), start.getX());
            canvas.drawLine((int) (mid + start.getX()), (int) (mid + start.getY()),
                    (int) (mid + start.getX() + arrowHeadLen * Math.cos(arrowHeadAngle + 5 * Math.PI / 6)),
                    (int) (mid + start.getY() + arrowHeadLen * Math.sin(arrowHeadAngle + 5 * Math.PI / 6)), paint);
            canvas.drawLine((int) (mid + start.getX()), (int) (mid + start.getY()),
                    (int) (mid + start.getX() + arrowHeadLen * Math.cos(arrowHeadAngle - 5 * Math.PI / 6)),
                    (int) (mid + start.getY() + arrowHeadLen * Math.sin(arrowHeadAngle - 5 * Math.PI / 6)), paint);
        }
    }

    public void setAngles(double angleX, double angleY)
    {
        if (line != null) {
            line.angleX = angleX;
            line.angleY = angleY;
        }
    }

    public void translate(double deltaXAngle, double deltaYAngle)
    {

        if (line != null) {
            line.angleX += deltaXAngle;
            line.angleY += deltaYAngle;
        }
    }

    public class Line3D
    {
        double length = 200;
        double angleX = 0,
                angleY = 0;

        Line3D(double len, double startAngleX, double startAngleY)
        {
            this.length = len;
            this.angleX = startAngleX;
            this.angleY = startAngleY;
        }

        private DataPoint getStartPos ()
        {
            return new DataPoint(length/2 * Math.cos(angleX) * Math.sin(angleY), length / 2 * Math.sin(angleX));
        }

        private DataPoint getEndPos ()
        {
            DataPoint start = getStartPos();
            return new DataPoint(-start.getX(), -start.getY());
        }
    }
}
