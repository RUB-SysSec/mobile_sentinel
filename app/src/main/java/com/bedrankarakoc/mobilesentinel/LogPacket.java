package com.bedrankarakoc.mobilesentinel;

public class LogPacket {

    String packetName;
    String packetContent;

    public LogPacket(String packetName, String packetContent){
        this.packetName = packetName;
        this.packetContent = packetContent;

    }


    public String getPacketName() {
        return packetName;
    }

    public String getPacketContent() {
        return packetContent;
    }

}
