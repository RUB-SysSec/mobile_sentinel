package com.bedrankarakoc.mobilesentinel;

import com.bedrankarakoc.mobilesentinel.BaseStation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView cellInfoView;
    private View view;
    private TelephonyManager telephonyManager;
    private Context mContext;
    private boolean isVolteEnabled;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.home_fragment, container, false);
        cellInfoView = view.findViewById(R.id.cellInfoView);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        mContext = getActivity();

    }

    @Override
    public void onStart() {
        super.onStart();
        showCellinfo(view);
        cellInfoView.append("SIM 1 IMSI : " + getImsi() + "\n");


    }

    public String getImsi() {


        Process p;
        StringBuffer output = new StringBuffer();
        try {
            p = Runtime.getRuntime().exec("su -c service call iphonesubinfo 7 | awk -F \"'\" '{print $2}' | sed '1 d' | tr -d '.' | awk '{print}' ORS=");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
                p.waitFor();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String response = output.toString();
        return response;


    }


    public void showCellinfo(View view) {

        List<CellInfo> cellInfoList = null;

        try {
            // getAllCellInfo() does not work on Exynos chipsets. Right now Qualcomm appears to be the only one working
            // cellInfoList[0] is the current serving cell and returned all values correctly.
            // All other measured neighbor cells do not return their proper MCC, MNC, LAC values. Cause unknown.
            cellInfoList = telephonyManager.getAllCellInfo();
        } catch (SecurityException e) {
            e.printStackTrace();
        }


        if (cellInfoList == null) {
            cellInfoView.setText("Activate your GPS for cellinfo \n");
        } else if (cellInfoList.size() == 0) {
            cellInfoView.setText("Base station list empty \n");
        } else {
            int cellNumber = cellInfoList.size();
            BaseStation servingBaseStation = bindData(cellInfoList.get(0));
            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard + "/logs/cellProperties.txt");
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
            String timestamp = sdf.format(new Date());


            try {
                OutputStreamWriter file_writer = new OutputStreamWriter(new FileOutputStream(file, true));
                BufferedWriter buffered_writer = new BufferedWriter(file_writer);
                buffered_writer.write(timestamp + "----->" + servingBaseStation.toString() + "\n");
                buffered_writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            cellInfoView.setText("Obtained " + cellNumber + " Base Stations" + "\nServing Base station：\n" + servingBaseStation.toString() + "\n");
            for (CellInfo cellInfo : cellInfoList) {
                BaseStation bs = bindData(cellInfo);


            }
        }

    }

    private BaseStation bindData(CellInfo cellInfo) {
        BaseStation baseStation = null;
        if (cellInfo instanceof CellInfoWcdma) {
            //3G
            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
            CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
            baseStation = new BaseStation();
            baseStation.setType("WCDMA");
            baseStation.setCid(cellIdentityWcdma.getCid());
            baseStation.setLac(cellIdentityWcdma.getLac());
            baseStation.setMcc(cellIdentityWcdma.getMccString());
            baseStation.setMnc(cellIdentityWcdma.getMncString());
            baseStation.setBsic_psc_pci(cellIdentityWcdma.getPsc());
            baseStation.setArfcn(cellIdentityWcdma.getUarfcn());
            if (cellInfoWcdma.getCellSignalStrength() != null) {
                baseStation.setAsuLevel(cellInfoWcdma.getCellSignalStrength().getAsuLevel()); //Get the signal level as an asu value between 0..31, 99 is unknown Asu is calculated based on 3GPP RSRP.
                baseStation.setSignalLevel(cellInfoWcdma.getCellSignalStrength().getLevel()); //Get signal level as an int from 0..4
                baseStation.setDbm(cellInfoWcdma.getCellSignalStrength().getDbm()); //Get the signal strength as dBm
            }
        } else if (cellInfo instanceof CellInfoLte) {
            //4G
            CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
            CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();

            baseStation = new BaseStation();
            baseStation.setType("LTE");
            baseStation.setCid(cellIdentityLte.getCi());
            baseStation.setMnc(cellIdentityLte.getMncString());
            baseStation.setMcc(cellIdentityLte.getMccString());
            baseStation.setLac(cellIdentityLte.getTac());
            baseStation.setBsic_psc_pci(cellIdentityLte.getPci());
            baseStation.setArfcn(cellIdentityLte.getEarfcn());

            if (cellInfoLte.getCellSignalStrength() != null) {
                baseStation.setAsuLevel(cellInfoLte.getCellSignalStrength().getAsuLevel());
                baseStation.setSignalLevel(cellInfoLte.getCellSignalStrength().getLevel());
                baseStation.setDbm(cellInfoLte.getCellSignalStrength().getDbm());
            }
        } else if (cellInfo instanceof CellInfoGsm) {
            //2G
            CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
            CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();
            baseStation = new BaseStation();
            baseStation.setType("GSM");
            baseStation.setCid(cellIdentityGsm.getCid());
            baseStation.setLac(cellIdentityGsm.getLac());
            baseStation.setMcc(cellIdentityGsm.getMccString());
            baseStation.setMnc(cellIdentityGsm.getMncString());
            baseStation.setBsic_psc_pci(cellIdentityGsm.getPsc());
            baseStation.setArfcn(cellIdentityGsm.getArfcn());
            if (cellInfoGsm.getCellSignalStrength() != null) {
                baseStation.setAsuLevel(cellInfoGsm.getCellSignalStrength().getAsuLevel());
                baseStation.setSignalLevel(cellInfoGsm.getCellSignalStrength().getLevel());
                baseStation.setDbm(cellInfoGsm.getCellSignalStrength().getDbm());
            }
        } else {

            System.out.println("CDMA CellInfo................................................");
        }
        return baseStation;
    }

}
