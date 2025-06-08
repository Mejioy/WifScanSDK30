package ru.kafpin.wifscansdk30;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Result  {
    private String ssid;
    private String rssi;
    private int time;

    public String getSsid() {
        return ssid;
    }

    public String getRssi() {
        return rssi;
    }

    public int getTime() {
        return time;
    }

    public Result(String ssid, String rssi, int time) {
        this.ssid = ssid;
        this.rssi = rssi;
        this.time = time;
    }
}
