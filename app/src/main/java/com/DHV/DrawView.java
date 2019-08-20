package com.DHV;

import android.graphics.Canvas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.util.AttributeSet;

import com.jjoe64.graphview.series.DataPoint;

public class DrawView extends View
{
    Paint paint = new Paint();
    Line3D line = new Line3D();

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(15);
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        DataPoint start = line.getStartPos();
        DataPoint end = line.getEndPos();
        canvas.drawLine((int)(100 + start.getX()), (int)(100 + start.getY()),
                100, 100, paint);
        Log.w("Tag", "got here " + line.angleX);
    }
    public class Line3D
    {
        double length = 200;
        double angleX = 0,
                angleY = Math.PI/4;

        public DataPoint getStartPos ()
        {
            return new DataPoint(Math.cos(angleX) * length / 2 * Math.sin(angleY), Math.sin(angleX) * length / 2 * Math.cos(angleY));
        }

        public DataPoint getEndPos ()
        {
            DataPoint start = getStartPos();
            return new DataPoint(-start.getX(), -start.getY());
        }
    }
}
