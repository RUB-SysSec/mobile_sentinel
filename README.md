Mobile Sentinel
===============

Mobile Sentinel is an Android App that allows you to detect vulnerabilities in deployed LTE and (future) 5G networks. With the current release, Mobile Sentinel focuses on the detection of the ReVoLTE vulnerability  (www.revolte-attack.net). Mobile Sentinel requires a Qualcomm based  Android phone with root access as it builds upon the Qualcomm's mdlog tool.  

The application includes:
 - An automized test run to detect the ReVoLTE vulnerability
 - A logging view to capture cellular traffic (currently RRC only) and view protocol messages in-app
 - Writing the captured traffic into PCAP files
 - Upload function of logs to an http server (under development)
 
 
## Installation
Download the latest APK build from here (add download link) and install it directly on the device. 

```
adb install mobilesentinel.apk
```

## Build instructions

You can build the application from source as well however, you will need a [Chaquopy SDK](https://chaquo.com/chaquopy/license/) license to be able to deploy the app outside of Android Studio.


## Requirements
 
- Rooted Android Phone with a Qualcomm Baseband
- The phone AND the used SIM Card must support VoLTE
- Currently requires minimum Android Pie (9.0)

## Tested Devices
 - Xiaomi Mi A3 (Android 9.0)
 - One Plus 6T (Android 9.0)
 - Xiaomi Mix 3 5G (Android 9.0)

## Used Libraries 
Mobile Sentinel uses the following libraries: 
 - [Chaquopy SDK](https://chaquo.com/chaquopy/license/)
 - [SCAT](https://github.com/fgsect/scat)
 - [Pycrate](https://github.com/P1sec/pycrate)

## Known Bugs

- Some phones with a Qualcomm baseband do not allow extracting cellular network traffic from the DIAG interface
