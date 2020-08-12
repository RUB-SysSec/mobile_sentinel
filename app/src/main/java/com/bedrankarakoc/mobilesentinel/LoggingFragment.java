package com.bedrankarakoc.mobilesentinel;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LoggingFragment extends Fragment {

    private Button startLoggingButton;
    private Button stopLoggingButton;
    private ListView listView;
    private String filename;
    private TextView loggingInfoText;


    // Setup
    ArrayList<LogPacket> packetList;
    private static LogAdapter adapter;
    Context mContext;
    private File sdcard;

    View view;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.logging_fragment, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        startLoggingButton = (Button) view.findViewById(R.id.start_logging_button);
        stopLoggingButton = (Button) view.findViewById(R.id.stop_logging_button);
        loggingInfoText = (TextView)view.findViewById(R.id.loggingInfoTextView);
        startLoggingButtonListener();
        stopLoggingButtonListener();
        mContext = getActivity();
        sdcard = Environment.getExternalStorageDirectory();
        if (!Python.isStarted())
            Python.start(new AndroidPlatform(mContext));
        packetList = new ArrayList<>();
        adapter = new LogAdapter(packetList, mContext);
        stopLoggingButton.setClickable(false);
        listView.setAdapter(adapter);
        return view;
    }


    public void startLoggingButtonListener() {


        startLoggingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startLoggingButton.setClickable(false);
                adapter.clear();
                listView.setAdapter(adapter);
                loggingInfoText.setVisibility(View.VISIBLE);
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
                filename = sdf.format(new Date());
                loggingInfoText.setText("Packet capture started ...");
                loggingInfoText.setTextColor(Color.GREEN);
                Python py = Python.getInstance();
                PyObject pyf = py.getModule("setup_parser");
                pyf.callAttr("start_logging", filename);
                stopLoggingButton.setClickable(true);




            }
        });


    }

    public void stopLoggingButtonListener() {

        stopLoggingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loggingInfoText.setText("Processing packets please wait ....");
                startLoggingButton.setClickable(false);
                stopLoggingButton.setClickable(false);


                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Python py = Python.getInstance();
                        PyObject pyf = py.getModule("setup_parser");
                        pyf.callAttr("stop_logging");

                        File baseDir = new File(Environment.getExternalStorageDirectory() + "/MobileSentinel/" + filename);



                        String qmdlFilename = "";

                        for (File f : baseDir.listFiles()) {
                            if (f.getName().endsWith(".qmdl")) {
                                qmdlFilename = f.getName();
                            }
                        }
                        if (!qmdlFilename.isEmpty()) {
                            pyf.callAttr("initiate_parsing", packetList, filename, qmdlFilename);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listView.setAdapter(adapter);

                                    loggingInfoText.setVisibility(View.INVISIBLE);
                                    startLoggingButton.setClickable(true);
                                }
                            });
                        }


                    }
                }

                ).start();


            }
        });
    }

}
