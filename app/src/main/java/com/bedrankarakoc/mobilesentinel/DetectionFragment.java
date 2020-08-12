package com.bedrankarakoc.mobilesentinel;

import com.bedrankarakoc.mobilesentinel.BaseStationLTE;
import com.bedrankarakoc.mobilesentinel.Uploader;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DetectionFragment extends Fragment {


    private View view;
    private String filename;
    private Button startDetectionButton;
    private Button stopDetectionButton;
    private TextView detectionProgressText;
    private TextView cellStatusText;
    private TextView isVolteEnabledText;
    private TextView cellIDtextview;
    private TextView TACtextview;
    private TextView PLMNtextview;
    private TelephonyManager telephonyManager;
    private ProgressBar progressBar;
    private TelecomManager telecomManager;
    private String phoneNumber;
    public int isVulnerable = 0;
    private Context mContext;

    private int uploadSetting = 2;
    private volatile boolean startSecondTest = false;
    private SimpleDateFormat simpleDateFormat;
    ArrayList<LogPacket> packetList;
    private volatile boolean stopDetection = false;
    private BaseStationLTE baseStationLTE = new BaseStationLTE();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    @Override
    public void onStart() {
        super.onStart();
        if (isVolteEnabled(telephonyManager) == true) {
            isVolteEnabledText.setText("isVolteEnabled : True");
            isVolteEnabledText.setTextColor(Color.GREEN);
        } else {
            isVolteEnabledText.setText("isVolteEnabled : False");
            isVolteEnabledText.setTextColor(Color.RED);
        }
        updateCellParameters();


    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Initialize UI elements and properties
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
        view = inflater.inflate(R.layout.detection_fragment, container, false);
        startDetectionButton = (Button) view.findViewById(R.id.start_detection_button);
        stopDetectionButton = (Button) view.findViewById(R.id.stop_detection_button);
        mContext = getActivity();
        stopDetectionButton.setClickable(false);
        detectionProgressText = (TextView) view.findViewById(R.id.detectionProgressText);
        cellStatusText = (TextView) view.findViewById(R.id.cellStatusTextView);
        isVolteEnabledText = (TextView) view.findViewById(R.id.volteStatusTextView);
        cellIDtextview = (TextView) view.findViewById(R.id.cellIDtextView);
        TACtextview = (TextView) view.findViewById(R.id.TACtextview);
        PLMNtextview = (TextView) view.findViewById(R.id.PLMNtextview);
        progressBar = view.findViewById(R.id.detectionProgress);
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.animate();
        detectionProgressText.setVisibility(View.INVISIBLE);
        progressBar.setMax(32);
        progressBar.setScaleY(3f);
        startDetectionButtonListener();
        stopDetectionButtonListener();
        telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        baseStationLTE.setTelephonyManager(telephonyManager);
        updateCellParameters();
        telecomManager = (TelecomManager) getActivity().getSystemService(Context.TELECOM_SERVICE);
        packetList = new ArrayList<>();

        return view;
    }

    public void updateCellParameters() {
        baseStationLTE.bindServingCellParameter();
        cellIDtextview.setText(("Cell ID: " + String.valueOf(baseStationLTE.getCid())));
        TACtextview.setText("TAC: " + String.valueOf(baseStationLTE.getTac()));
        PLMNtextview.setText("PLMN: " + String.valueOf(baseStationLTE.getMcc()) + String.valueOf(baseStationLTE.getMnc()));
    }

    public void parseDiagLogs(String filename) {
        // Stop the logging process

        Python py = Python.getInstance();
        PyObject pyf = py.getModule("setup_parser");
        pyf.callAttr("stop_logging");

        // Extra safety time buffer
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Parse the DIAG logs
        File baseDir = new File(Environment.getExternalStorageDirectory() + "/MobileSentinel/" + filename);
        String qmdlFilename = "";

        for (File f : baseDir.listFiles()) {
            if (f.getName().endsWith(".qmdl")) {
                qmdlFilename = f.getName();
            }
        }

        if (qmdlFilename.isEmpty()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Error: No DIAG Logs found", Toast.LENGTH_LONG).show();

                }
            });
            return;

        }

        pyf.callAttr("initiate_parsing", packetList, filename, qmdlFilename, cellStatusText, isVulnerable);



    }

    public void initTestRun() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Start the first test
                filename = simpleDateFormat.format(new Date());
                detectionRunner(4, filename);

                if (startSecondTest) {
                    startSecondTest = false;
                    filename = simpleDateFormat.format(new Date());
                    detectionRunner(28, filename);



                }


            }
        }).start();

    }

    public void detectionRunner(final int intervall, final String filename) {
        Python py = Python.getInstance();
        PyObject pyf = py.getModule("setup_parser");
        pyf.callAttr("start_logging", filename);


        for (int i = 0; i < intervall + 1; i++) {

            // End loop when user presses abort button
            if (stopDetection) {

                startSecondTest = false;
                py.getModule("setup_parser");
                pyf.callAttr("stop_logging");
                break;
            }

            // Case Test One
            if ((i == intervall) && (intervall == 4)) {
                parseDiagLogs(filename);
                // If cell is vulnerable in the first interval update UI and end the test
                if (cellStatusText.getText().toString().matches("Cell: VULNERABLE")) {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Update UI elements accordingly
                            cellStatusText.setTextColor(Color.RED);
                            Toast.makeText(getActivity(), "Vulnerable eNodeB !", Toast.LENGTH_LONG).show();
                            detectionProgressText.setTextColor(Color.RED);
                            detectionProgressText.setText("eNodeB is vulnerable");
                            startDetectionButton.setClickable(true);
                            progressBar.setProgress(progressBar.getMax());

                            // Check for Upload Setting
                            switch (uploadSetting) {
                                case 0:
                                    try {
                                        generateLogs(filename, true);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    showUploadDialog(filename, true);
                                    break;
                            }


                        }
                    });

                } else {
                    // Else set flag for the second test and break loop
                    if (!stopDetection) {
                        // Do not initiate second call if testrun is aborted during the parsing of the first test
                        startSecondTest = true;
                    }

                    break;
                }
                break;


            }
            if ((i == intervall) && (intervall == 28)) {
                parseDiagLogs(filename);
                if (cellStatusText.getText().toString().matches("Cell: VULNERABLE")) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cellStatusText.setTextColor(Color.RED);
                            Toast.makeText(getActivity(), "Vulnerable eNodeB!", Toast.LENGTH_LONG).show();
                            detectionProgressText.setTextColor(Color.RED);
                            detectionProgressText.setText("eNodeB is vulnerable");
                            startDetectionButton.setClickable(true);

                            switch (uploadSetting) {
                                case 0:
                                    try {
                                        generateLogs(filename, true);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    showUploadDialog(filename, true);
                                    break;
                            }

                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cellStatusText.setTextColor(Color.GREEN);
                            cellStatusText.setText("Cell: Not Vulnerable");
                            Toast.makeText(getActivity(), "eNodeB not vulnerable!", Toast.LENGTH_LONG).show();
                            detectionProgressText.setTextColor(Color.GREEN);
                            detectionProgressText.setText("eNodeB is not vulnerable");
                            startDetectionButton.setClickable(true);

                            switch (uploadSetting) {
                                case 0:
                                    try {
                                        generateLogs(filename, false);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    showUploadDialog(filename, false);
                                    break;
                            }

                        }
                    });
                }
                break;

            }


            // Calling procedure
            try {
                // Start the test call
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            } catch (SecurityException s) {
                s.printStackTrace();
            }

            // Update progressBar
            progressBar.incrementProgressBy(1);

            try {
                // Call duration 4 seconds, should be enough to catch the bearer
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // End the test call
            telecomManager.endCall();

            try {
                // Wait 3 seconds untill next call so users can click on the abort button if needed
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }


    }

    public void showUploadDialog(final String filename, final boolean isVulnerable) {
        AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setCancelable(false)
                .setTitle("Upload base station logs")
                .setMessage("Do you want to upload the base station logs ?")

                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            generateLogs(filename, isVulnerable);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                })
                .setNegativeButton("No", null)
                .create();
        dialog.show();
    }

    public void generateLogs(String currentTime, Boolean isVulnerable) throws IOException {
        JSONObject meta = new JSONObject();
        try {
            meta.put("Is Vulnerable: ", String.valueOf(isVulnerable));
            meta.put("Cell ID: ", String.valueOf(baseStationLTE.getCid()));
            meta.put("TAC: ", String.valueOf(baseStationLTE.getTac()));
            meta.put("PLMN: ", String.valueOf(baseStationLTE.getMcc()) + String.valueOf(baseStationLTE.getMnc()));
        } catch (JSONException j) {
            j.printStackTrace();
        }

        File sdcard = Environment.getExternalStorageDirectory();
        File jsonFile = new File(sdcard + "/MobileSentinel/" + currentTime + "/", currentTime + ".json");


        try {
            OutputStreamWriter file_writer = new OutputStreamWriter(new FileOutputStream(jsonFile, true));
            BufferedWriter buffered_writer = new BufferedWriter(file_writer);
            buffered_writer.write(meta.toString(2));
            buffered_writer.close();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        try {
            File pcapFile = new File(sdcard + "/MobileSentinel/" + currentTime + "/", currentTime + ".pcap");
            Uploader uploader = new Uploader();

            uploader.zipFiles(pcapFile.getPath(), jsonFile.getPath(), currentTime);
            File zipFile = new File(sdcard + "/MobileSentinel/" + currentTime + "/", currentTime + ".zip");
            uploader.uploadFile(zipFile);
        } catch (Exception e) {
            e.printStackTrace();
        }





    }


    public void startDetectionButtonListener() {
        startDetectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SHARED_PREFERENCES", Context.MODE_PRIVATE);
                phoneNumber = sharedPreferences.getString("CALL_NUMBER", "+4915792389038");
                uploadSetting = sharedPreferences.getInt("UPLOAD_SETTING", 2);
                updateCellParameters();
                // Inform user about the detection run
                AlertDialog dialog = new AlertDialog.Builder(mContext)
                        .setCancelable(false)
                        .setTitle("Starting Detection")
                        .setMessage("You are about to start the detection run.\n" +
                                "Please note that up to 32 calls will be started on our test number and there can be costs depending on your subscription.\n" +
                                "You can change the target number in the settings.\n" +
                                "Make sure you do not move the device during the test to stay connected to the base station.\n" +
                                "Press START to initiate the test run.")

                        .setPositiveButton("START", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (!isVolteEnabled(telephonyManager)) {
                                    AlertDialog VoLTEdialog = new AlertDialog.Builder(mContext)
                                            .setCancelable(false)
                                            .setTitle("No VoLTE Service")
                                            .setMessage("The app detected that VoLTE may not be supported by either your SIM card or device\n" +
                                                    "The test will not work without VoLTE support!\n" +
                                                    "If you are sure that VoLTE is working on your phone click \"Continue\" \n")

                                            .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //Update UI elements
                                                    detectionProgressText.setVisibility(View.VISIBLE);
                                                    detectionProgressText.setTextColor(Color.GREEN);
                                                    detectionProgressText.setText("Detection Running");
                                                    cellStatusText.setTextColor(Color.CYAN);
                                                    cellStatusText.setText("Cell: Testing...");
                                                    startDetectionButton.setClickable(false);
                                                    stopDetectionButton.setClickable(true);
                                                    progressBar.setVisibility(View.VISIBLE);
                                                    progressBar.setProgress(0);

                                                    stopDetection = false;
                                                    updateCellParameters();
                                                    // Initiate the test runs
                                                    initTestRun();
                                                }
                                            })
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    return;
                                                }
                                            })
                                            .create();
                                    VoLTEdialog.show();
                                } else {
                                    //Update UI elements
                                    detectionProgressText.setVisibility(View.VISIBLE);
                                    detectionProgressText.setTextColor(Color.GREEN);
                                    detectionProgressText.setText("Detection Running");
                                    cellStatusText.setTextColor(Color.CYAN);
                                    cellStatusText.setText("Cell: Testing...");
                                    startDetectionButton.setClickable(false);
                                    stopDetectionButton.setClickable(true);
                                    progressBar.setVisibility(View.VISIBLE);
                                    progressBar.setProgress(0);

                                    stopDetection = false;
                                    updateCellParameters();
                                    // Initiate the test runs
                                    initTestRun();
                                }


                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();




            }
        });
    }

    public void stopDetectionButtonListener() {
        stopDetectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Update UI elements
                detectionProgressText.setTextColor(Color.RED);
                detectionProgressText.setText("Stopped Detection");
                cellStatusText.setTextColor(Color.RED);
                cellStatusText.setText("Cell: Not Tested");
                stopDetectionButton.setClickable(false);
                startDetectionButton.setClickable(true);
                progressBar.setProgress(progressBar.getMax());
                // Set volatile boolean to stop caller thread
                stopDetection = true;


            }
        });
    }

    // Use reflection to access method with @UnsupportedAppUsage tag
    // Returns wether VoLTE is enabled or not, may not be 100% reliable
    public boolean isVolteEnabled(TelephonyManager telephonyManager) {
        boolean isVolteEnabled = false;
        try {

            Method method = telephonyManager.getClass().getMethod("isVolteAvailable");
            isVolteEnabled = (boolean) method.invoke(telephonyManager);


        } catch (Exception e) {
            e.printStackTrace();

        }
        return isVolteEnabled;

    }
}
