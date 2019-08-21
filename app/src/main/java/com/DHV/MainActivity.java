package com.DHV;

import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.TypedArrayUtils;

import android.os.Handler;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Context;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Arrays;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity
{
    private Button saveButton;
    private Button resetButton;
    private Button loadButton;
    private Button pauseButton;
    private boolean paused = false;

    private final Handler graphHandler = new Handler();
    private Runnable graphTimer;
    GraphView graph;
    private LineGraphSeries<DataPoint> graphSeries;
    Random mRand = new Random();

    double graphYValue = 0;
    private double graphXValue = 0;

    ArrayList<DataPoint> dataList = new ArrayList<>();
    ArrayList<DataPoint> dataBuffer = new ArrayList<>();
    private double graphXStep = 0;

    private int numOfPoints = 50;
    private double timeStep = 0.1;

    private DrawView dv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        graph = findViewById(R.id.graph);
        graphSeries = new LineGraphSeries<>();
        graph.addSeries(graphSeries);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(numOfPoints * timeStep);

        pauseButton = findViewById(R.id.pausebutton);
        pauseButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                paused = !paused;
                updatePauseButtonText();
            }
        });

        resetButton = findViewById(R.id.resetbutton);
        resetButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                resetGraph();
            }
        });

        saveButton = findViewById(R.id.savebutton);
        saveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ArrayList<String> dataToSave = new ArrayList<>();
                double lastTime = 0;
                for(int i = 0; i < dataList.size(); i++)
                {
                    dataToSave.add("t: " + (dataList.get(i).getX() - lastTime) + " p: " + dataList.get(i).getY());
                    lastTime = dataList.get(i).getX();
                }
                writeToDataFile(dataToSave);
            }
        });

        loadButton = findViewById(R.id.loadbutton);
        loadButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                resetGraph();

                ArrayList<String> inputBuffer = readFromDataFile();
                for(int i = 0; i < inputBuffer.size(); i++)
                {
                    String line = inputBuffer.get(i);
                    List<String> lineData = Arrays.asList(line.split(" "));
                    if (lineData.size() >= 4) {
                        int timeStepIndex = lineData.indexOf("t:") + 1;
                        int pressureIndex = lineData.indexOf("p:") + 1;
                        if (timeStepIndex != 0 && pressureIndex != 0) {
                            double lineTimeStep = Double.parseDouble(lineData.get(timeStepIndex));
                            double linePressure = Double.parseDouble(lineData.get(pressureIndex));
                            dataBuffer.add(new DataPoint(lineTimeStep, linePressure));
                        }
                    }
                }
                paused = true;
                updatePauseButtonText();
            }
        });

        dv = findViewById(R.id.drawview);
    }

    private void updatePauseButtonText()
    {
        pauseButton.setText(paused ? "Play" : "Pause");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        graphTimer = new Runnable()
        {
            @Override
            public void run()
            {
                if (!paused)
                {
                    dataBuffer.add(getData(timeStep));
                    dv.line.angleX += Math.PI/16;
                    dv.line.angleY += Math.PI/16/16;
                    dv.invalidate();
                }

                while(dataBuffer.size() > 0)
                {
                    DataPoint pointToAdd = dataBuffer.get(0);
                    dataBuffer.remove(0);

                    graphXStep += pointToAdd.getX();
                    if (graphXStep >= timeStep)
                    {
                        graphXValue += timeStep;
                        graphXStep -= timeStep;
                        //graphXStep -= timeStep;
                        graphSeries.appendData(new DataPoint(graphXValue, pointToAdd.getY()), true, numOfPoints);
                        dataList.add(new DataPoint(graphXValue, pointToAdd.getY()));
                    }
                }

                graphHandler.postDelayed(this, (int) (1000 * timeStep));
            }
        };
        graphHandler.postDelayed(graphTimer, 200);
    }

    @Override
    public void onPause()
    {
        graphHandler.removeCallbacks(graphTimer);
        super.onPause();
    }

    private DataPoint getData(double timeStep)
    {
        return new DataPoint(timeStep, getRandom());
    }

    private double getRandom()
    {
        return graphYValue += mRand.nextDouble()*5 - 2.5;
    }

    private void resetGraph()
    {
        graphXValue = 0;
        graphXStep = 0;
        graphYValue = 0;
        dataList.clear();
        dataBuffer.clear();
        graphSeries.resetData(new DataPoint[] {});
    }

    private void writeToDataFile(ArrayList<String> data)
    {
        try
        {
            FileOutputStream fos = openFileOutput("data_log.txt", MODE_PRIVATE);
            for(int i = 0; i < data.size(); i++)
                fos.write((data.get(i) + "\n").getBytes());
            fos.close();
            Log.w("Writing", "Writing to data log file");
        }
        catch (IOException e)
        {
            Log.w("Exception", "Error writing to file: " + e.toString());
            e.printStackTrace();
        }
    }

    private ArrayList<String> readFromDataFile()
    {
        try
        {
            FileInputStream fis = openFileInput("data_log.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            ArrayList<String> logData = new ArrayList<>();
            String line;
            while((line = br.readLine()) != null)
                logData.add(line);
            fis.close();
            Log.w("Writing", "Reading from data log file");
            return logData;
        }
        catch (IOException e)
        {
            Log.w("Exception", "Error reading from file");
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
