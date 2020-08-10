package com.bedrankarakoc.mobilesentinel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.text.Html;

import android.os.Environment;

import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;


import com.google.android.material.bottomnavigation.BottomNavigationView;


import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class HomeBottomBar extends AppCompatActivity {


    // Permissions
    private String[] permissions = {"android.permission.CALL_PHONE", "android.permission.ANSWER_PHONE_CALLS", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_PHONE_STATE", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"};
    private int requestCode = 1337;
    private Context mContext;
    private File sdcard;


    TextView actionBarText;

    Fragment homeFragment = new HomeFragment();
    Fragment detectionFragment = new DetectionFragment();
    Fragment loggingFragment = new LoggingFragment();
    Fragment settingsFragment = new SettingsFragment();
    Fragment active = homeFragment;
    FragmentManager fragmentManager = getSupportFragmentManager();
    String rrcConfig;
    String fullConfig;
    String rrc_filter_diag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_bottom_bar);
        mContext = getApplicationContext();
        fullConfig = mContext.getResources().getResourceEntryName(R.raw.full_diag);
        rrcConfig = mContext.getResources().getResourceEntryName(R.raw.rrc_diag);
        rrc_filter_diag = mContext.getResources().getResourceEntryName(R.raw.rrc_filter_diag);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        requestPermissions(permissions, requestCode);
        sdcard = Environment.getExternalStorageDirectory();
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        actionBarText = findViewById(R.id.actionBarTitle);


        fragmentManager.beginTransaction().add(R.id.fragment_container, settingsFragment, "4").hide(settingsFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, loggingFragment, "3").hide(loggingFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, detectionFragment, "2").hide(detectionFragment).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_container, homeFragment, "1").commit();

    }

    @Override
    public void onBackPressed() {
        // Disable OS return button
    }


    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            actionBarText.setText("Home");
                            fragmentManager.beginTransaction().hide(active).show(homeFragment).commit();
                            active = homeFragment;
                            return true;
                        case R.id.nav_detection:
                            actionBarText.setText("ReVoLTE Detection");
                            fragmentManager.beginTransaction().hide(active).show(detectionFragment).commit();
                            active = detectionFragment;
                            return true;
                        case R.id.nav_logging:
                            actionBarText.setText("Logging");
                            fragmentManager.beginTransaction().hide(active).show(loggingFragment).commit();
                            active = loggingFragment;
                            return true;
                        case R.id.nav_settings:
                            actionBarText.setText("Settings");
                            fragmentManager.beginTransaction().hide(active).show(settingsFragment).commit();
                            active = settingsFragment;
                            return true;

                    }

                    return false;
                }
            };


    // Create logging config files (from raw resources) to external storage
    public void createConfig(String filename) {
        String configDir = "/MobileSentinel";
        InputStream inputStream = mContext.getResources().openRawResource(R.raw.rrc_filter_diag);

        File f = new File(filename + ".cfg");
        try {
            OutputStream out = new FileOutputStream(new File(sdcard + configDir, filename));
            byte[] buffer = new byte[4096 * 2];
            int len;
            while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, len);
            }
            inputStream.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void showPermissionDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Accept all permissions")
                .setCancelable(false)
                .setMessage("Accept all requested permissions and superuser requests as the app won't work without")
                .setPositiveButton("Accept Permissions", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(permissions, requestCode);
                    }
                })
                .setNegativeButton("Exit Application", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                        System.exit(0);
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (checkMultiplePermissions(HomeBottomBar.this, permissions) == true) {
            System.out.println("All permissions granted");
            Python py = Python.getInstance();
            PyObject pyf = py.getModule("setup_parser");
            pyf.callAttr("create_directory");
            createConfig(rrc_filter_diag);
        } else {
            System.out.println("Permissions missing");
            showPermissionDialog(HomeBottomBar.this);
        }

    }

    public static boolean checkMultiplePermissions(Context context, String... permissions) {
        // Check if all permissions are granted, if not re-ask
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                //System.out.println(permission);
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    //System.out.println(permission + "Not granted");
                    return false;
                }
            }
        }

        return true;
    }
}
