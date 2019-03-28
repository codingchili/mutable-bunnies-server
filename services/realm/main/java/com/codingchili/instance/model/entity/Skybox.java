package com.codingchili.instance.model.entity;

/**
 * Skybox configuration.
 */
public class Skybox {
    private String sky = "#0000ff";
    private String clouds = "#ffffff";

    public String getSky() {
        return sky;
    }

    public void setSky(String sky) {
        this.sky = sky;
    }

    public String getClouds() {
        return clouds;
    }

    public void setClouds(String clouds) {
        this.clouds = clouds;
    }
}
