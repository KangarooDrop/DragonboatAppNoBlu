package com.DHV;

import android.text.InputType;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;
import android.widget.EditText;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Arrays;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity
{
    private double startRecordTime = -1;
    private double stopRecordTime = -1;
    private LineGraphSeries<DataPoint> recordSeries = new LineGraphSeries<>();
    private boolean recording = false;

    private Spinner dataSelectionSpinner;
    ArrayList<String> spinnerList = new ArrayList<>();

    private final Handler graphHandler = new Handler();
    private Runnable graphTimer;
    GraphView graph;
    private ArrayList<LineGraphSeries<DataPoint>> graphSeries;
    Random mRand = new Random();

    double graphYValue = 0;
    private double graphXValue = 0;

    ArrayList<DataPoint> dataList = new ArrayList<>();
    ArrayList<DataPoint> dataBuffer = new ArrayList<>();
    private double graphXStep = 0;

    private int numOfPoints = 1023;
    private double timeStep = 0.1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        graph = findViewById(R.id.graph);
        graphSeries = new ArrayList<>();
        graphSeries.add(new LineGraphSeries<DataPoint>());
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(5);
        graph.getViewport().setScrollable(true);

        recordSeries.setColor(0xffff0000);

        spinnerList.add("Live");
        List<String> fileNames = getAllFilenames();
        for(int i = 0; fileNames != null && i < fileNames.size(); i++)
        {
            graphSeries.add(new LineGraphSeries<DataPoint>());
            String fileName = fileNames.get(i);
            spinnerList.add(fileName.substring(0, fileName.length() - 4));
        }
        dataSelectionSpinner = findViewById(R.id.data_spinner);
        updateSpinnerList();
        dataSelectionSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
            {
                graph.removeAllSeries();
                graph.addSeries(graphSeries.get(position));
                if (position != 0)
                {
                    graph.getViewport().setScrollable(true);
                    double startX = graphXValue;
                    graphXValue = 0;
                    graphSeries.get(position).resetData(new DataPoint[] {});
                    List<String> strData = readFromDataFile(spinnerList.get(position));
                    for(String line : strData)
                        addStringToGraph(line, position);
                    graphXValue = startX;
                }
                else {
                    graph.getViewport().setScrollable(false);
                    graph.addSeries(recordSeries);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {

            }
        });

        Button saveButton = findViewById(R.id.savebutton);
        saveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);

                new AlertDialog.Builder(MainActivity.this).setTitle("Save Data As").setView(input).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        ArrayList<String> dataToSave = new ArrayList<>();
                        double lastTime = -1;
                        Iterator<DataPoint> iter = recordSeries.getValues(startRecordTime, stopRecordTime);
                        DataPoint currentPoint;
                        while (iter.hasNext())
                        {
                            currentPoint = iter.next();
                            if(lastTime == -1)
                                lastTime = currentPoint.getX();
                            dataToSave.add("t: " + (currentPoint.getX() - lastTime) + " p: " + currentPoint.getY());
                            lastTime = currentPoint.getX();
                        }
                        if (writeToDataFile(dataToSave, input.getText().toString()))
                        {
                            graphSeries.add(new LineGraphSeries<DataPoint>());
                            if (!spinnerList.contains(input.getText().toString())) {
                                spinnerList.add(input.getText().toString());
                                updateSpinnerList();
                            }
                            recordSeries.resetData(new DataPoint[] {});
                        }
                        else
                            Toast.makeText(MainActivity.this, "Could not create file with the selected name. Please try again with a different file name", Toast.LENGTH_LONG).show();
                    }
                }).setNegativeButton("Cancel", null).show();
            }
        });

        Button startButton = findViewById(R.id.startrecordbutton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                startRecordTime = graphXValue;
                recordSeries.resetData(new DataPoint[] {});
                recording = true;
            }
        });

        Button stopButton = findViewById(R.id.stoprecordbutton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (recording) {
                    stopRecordTime = graphXValue;
                    recording = false;
                }
            }
        });

        Button closeButton = findViewById(R.id.closebuttton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String name = dataSelectionSpinner.getSelectedItem().toString();
                if (dataSelectionSpinner.getSelectedItemPosition() != 0) {
                    new AlertDialog.Builder(MainActivity.this).setTitle("Are you sure you want to delete \"" + name + "\" forever?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String name = dataSelectionSpinner.getSelectedItem().toString();
                                    if (deleteFileFromDir(name)) {
                                        int index = spinnerList.indexOf(name);
                                        graphSeries.remove(index);
                                        spinnerList.remove(name);
                                        updateSpinnerList();
                                    }
                                    else
                                        Toast.makeText(MainActivity.this, "Could not delete file", Toast.LENGTH_SHORT).show();
                                }
                            }).setNegativeButton("No", null)
                            .show();
                }
                else
                    Toast.makeText(MainActivity.this, "Cannot close the live feed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addStringToGraph(String line, int index)
    {
        try {
            List<String> lineData = Arrays.asList(line.split(" "));
            if (lineData.size() >= 4) {
                int timeStepIndex = lineData.indexOf("t:") + 1;
                int pressureIndex = lineData.indexOf("p:") + 1;
                if (timeStepIndex != 0) {
                    double deltaTime = Double.parseDouble(lineData.get(timeStepIndex));
                    if (pressureIndex != 0)
                    {
                        double pressureVal = Double.parseDouble(lineData.get(pressureIndex));
                        DataPoint receivedPoint = new DataPoint(graphXValue, pressureVal);
                        graphSeries.get(index).appendData(receivedPoint, true, numOfPoints);
                        graphXValue += deltaTime;
                    }
                }
            }
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
        }
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
                dataBuffer.add(getData(timeStep));

                while(dataBuffer.size() > 0)
                {
                    DataPoint pointToAdd = dataBuffer.get(0);
                    dataBuffer.remove(0);

                    graphXStep += pointToAdd.getX();
                    if (graphXStep >= timeStep)
                    {
                        graphXValue += timeStep;
                        graphXStep -= timeStep;
                        boolean scrollToEnd = (dataSelectionSpinner.getSelectedItemPosition() == 0);
                        if (recording)
                        {
                            recordSeries.appendData(new DataPoint(graphXValue, pointToAdd.getY()), scrollToEnd, numOfPoints);
                            graphSeries.get(0).appendData(new DataPoint(graphXValue, pointToAdd.getY()), scrollToEnd, numOfPoints);
                        }
                        else
                            graphSeries.get(0).appendData(new DataPoint(graphXValue, pointToAdd.getY()), scrollToEnd, numOfPoints);
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

    private boolean writeToDataFile(ArrayList<String> data, String fileName)
    {
        try
        {/*
            String path = "";
            int slashIndex = 0;
            while((slashIndex = fileName.indexOf('/')) != -1) {
                path += fileName.substring(0, slashIndex+1);
                fileName = fileName.substring(slashIndex+1, fileName.length());
            }
            Log.w("path", "Path="+path);*/
            File parentFolder = getDir("dataset", MODE_PRIVATE);
            File outFile = new File(parentFolder, fileName + ".txt");
            FileOutputStream fos = new FileOutputStream(outFile);
            for(int i = 0; i < data.size(); i++)
                fos.write((data.get(i) + "\n").getBytes());
            fos.close();
            Log.w("Writing", "Writing to data log file");
            return true;
        }
        catch (IOException e)
        {
            Log.w("Exception", "Error writing to file: " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    private ArrayList<String> readFromDataFile(String fileName)
    {
        try
        {
            File parentFolder = getDir("dataset", MODE_PRIVATE);
            File outFile = new File(parentFolder, fileName + ".txt");
            FileInputStream fis = new FileInputStream(outFile);
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

    private List<String> getAllFilenames ()
    {
        List<String> fileNames = new ArrayList<>();
        File dir = getDir("dataset", MODE_PRIVATE);
        if (dir != null && dir.list() != null)
            Collections.addAll(fileNames, dir.list());
        else
            return new ArrayList<>();
        return fileNames;
    }

    private boolean deleteFileFromDir (String fileName)
    {
        File dir = getDir("dataset", MODE_PRIVATE);
        File f = new File(dir, fileName + ".txt");
        return f.delete();
    }

    private void updateSpinnerList()
    {
        ArrayAdapter<String> adp1 = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, spinnerList);
        adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataSelectionSpinner.setAdapter(adp1);
    }
}
