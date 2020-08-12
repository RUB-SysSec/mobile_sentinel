package com.bedrankarakoc.mobilesentinel;

import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.TelephonyManager;

import java.util.List;

public class BaseStationLTE {

    private TelephonyManager telephonyManager;

    private List<CellInfo> cellInfoList = null;

    private String mcc;

    private String mnc;

    private int tac;

    private int cid;

    private int earfcn;

    private int pci;


    private int asuLevel;       /* Signal level as an asu value, asu is calculated based on 3GPP RSRP
                                  for LTE, between 0..97, 99 is unknown */

    private int signalLevel;    // Signal level as an int from 0..4

    private int dbm;            // Signal strength as dBm

    private String type;        // Signal type, GSM or WCDMA or LTE or CDMA


    public void setTelephonyManager(TelephonyManager telephonyManager) {
        this.telephonyManager = telephonyManager;
    }

    public void bindServingCellParameter() {
        try {
            // getAllCellInfo() does not work on Exynos chipsets. Right now Qualcomm appears to be the only one working
            // cellInfoList[0] is the current serving cell and returned all values correctly.
            // All other measured neighbor cells do not return their proper MCC, MNC, LAC values. Cause unknown.
            this.cellInfoList = telephonyManager.getAllCellInfo();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        if (this.cellInfoList != null) {
            try {
                CellInfo cellInfo = this.cellInfoList.get(0);

                if (cellInfo instanceof CellInfoLte) {
                    //4G
                    CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                    CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();


                    this.setType("LTE");
                    this.setCid(cellIdentityLte.getCi());
                    this.setMnc(cellIdentityLte.getMncString());
                    this.setMcc(cellIdentityLte.getMccString());
                    this.setTac(cellIdentityLte.getTac());
                    this.setPci(cellIdentityLte.getPci());
                    this.setEarfcn(cellIdentityLte.getEarfcn());

                    if (cellInfoLte.getCellSignalStrength() != null) {
                        this.setAsuLevel(cellInfoLte.getCellSignalStrength().getAsuLevel());
                        this.setSignalLevel(cellInfoLte.getCellSignalStrength().getLevel());
                        this.setDbm(cellInfoLte.getCellSignalStrength().getDbm());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }



        }

    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getMnc() {
        return mnc;
    }

    public void setMnc(String mnc) {
        this.mnc = mnc;
    }

    public int getTac() {
        return tac;
    }

    public void setTac(int lac) {
        this.tac = lac;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getEarfcn() {
        return earfcn;
    }

    public void setEarfcn(int earfcn) {
        this.earfcn = earfcn;
    }

    public int getPci() {
        return pci;
    }

    public void setPci(int bsic_psc_pci) {
        this.pci = bsic_psc_pci;
    }

    public int getAsuLevel() {
        return asuLevel;
    }

    public void setAsuLevel(int asuLevel) {
        this.asuLevel = asuLevel;
    }

    public int getSignalLevel() {
        return signalLevel;
    }

    public void setSignalLevel(int signalLevel) {
        this.signalLevel = signalLevel;
    }

    public int getDbm() {
        return dbm;
    }

    public void setDbm(int dbm) {
        this.dbm = dbm;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return
                " mcc=" + mcc + "\n" +
                        " mnc=" + mnc + "\n" +
                        " lac=" + tac + "\n" +
                        " cid=" + cid + "\n" +
                        " earfcn=" + earfcn + "\n" +
                        " pci=" + pci + "\n" +
                        " asuLevel=" + asuLevel + "\n" +
                        " signalLevel=" + signalLevel + "\n" +
                        " dbm=" + dbm + "\n" +
                        " type='" + type + '\'';
    }
}
