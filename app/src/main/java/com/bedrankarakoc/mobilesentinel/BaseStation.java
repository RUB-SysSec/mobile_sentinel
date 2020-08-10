package com.bedrankarakoc.mobilesentinel;

public class BaseStation {

    private String mcc;            // Mobile Country Code

    private String mnc;            // Mobile Network Code

    private int lac;            // Location Area Code or TAC(Tracking Area Code) for LTE

    private int cid;            // Cell Identity

    private int arfcn;          // Absolute RF Channel Number (or UMTS Absolute RF Channel Number for WCDMA)

    private int bsic_psc_pci;   /* bsic for GSM, psc for WCDMA, pci for LTE,
                                   GSM has #getPsc() but always get Integer.MAX_VALUE,
                                   psc is undefined for GSM */

    private double lon;         // Base station longitude

    private double lat;         // Base station latitude

    private int asuLevel;       /* Signal level as an asu value, asu is calculated based on 3GPP RSRP
                                   for GSM, between 0..31, 99 is unknown
                                   for WCDMA, between 0..31, 99 is unknown
                                   for LTE, between 0..97, 99 is unknown
                                   for CDMA, between 0..97, 99 is unknown */

    private int signalLevel;    // Signal level as an int from 0..4

    private int dbm;            // Signal strength as dBm

    private String type;        // Signal type, GSM or WCDMA or LTE or CDMA

    // Getter and Setter for all fields
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

    public int getLac() {
        return lac;
    }

    public void setLac(int lac) {
        this.lac = lac;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getArfcn() {
        return arfcn;
    }

    public void setArfcn(int arfcn) {
        this.arfcn = arfcn;
    }

    public int getBsic_psc_pci() {
        return bsic_psc_pci;
    }

    public void setBsic_psc_pci(int bsic_psc_pci) {
        this.bsic_psc_pci = bsic_psc_pci;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
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
                " mcc: " + mcc + "\n" +
                " mnc: "  + mnc + "\n" +
                " tac: " + lac + "\n" +
                " cid: " + cid + "\n" +
                " earfcn: " + arfcn + "\n" +
                " pci: " + bsic_psc_pci + "\n" +
                " asuLevel: " + asuLevel + "\n" +
                " signalLevel: " + signalLevel + "\n" +
                " dbm: " + dbm + "\n" +
                " type: '" + type + '\'';
    }
}