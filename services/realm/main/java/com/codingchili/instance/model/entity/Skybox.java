package com.codingchili.instance.model.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Skybox configuration.
 */
public class Skybox {
    private List<String> gradient = new ArrayList<>() {{
        // default blue sky.
        add("#000008");
        add("#000032");
    }};
    private Background background = new Background();
    private Clouds clouds = new Clouds();
    private Stars stars = new Stars();
    private Moon moon = new Moon();

    public Stars getStars() {
        return stars;
    }

    public void setStars(Stars stars) {
        this.stars = stars;
    }

    public List<String> getGradient() {
        return gradient;
    }

    public void setGradient(List<String> gradient) {
        this.gradient = gradient;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public void setClouds(Clouds clouds) {
        this.clouds = clouds;
    }

    public Background getBackground() {
        return background;
    }

    public void setBackground(Background background) {
        this.background = background;
    }

    public Moon getMoon() {
        return moon;
    }

    public void setMoon(Moon moon) {
        this.moon = moon;
    }

    public static class Background {
        public String resource;
        public String tint = "#ffffff";

        public boolean isEnabled() {
            return resource != null;
        }
    }

    public static class Moon {
        public boolean enabled = false;
        public String tint = "#ffffff";
        public String resource = "game/map/clouds/moon.png";
    }

    public static class Stars {
        public boolean enabled = true;
        public String tint = "#ffffff";
        public String resource = "game/map/clouds/skybox_stars.png";
    }

    public static class Clouds {
        public boolean enabled = true;
        public String tint = "#ffffff";
        public CloudType type = CloudType.puffy;
    }

    public enum CloudType {
        puffy, dark
    }
}
