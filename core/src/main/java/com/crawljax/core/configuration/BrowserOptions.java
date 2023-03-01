package com.crawljax.core.configuration;

import org.openqa.selenium.firefox.FirefoxProfile;

public class BrowserOptions {

    public static final int MACBOOK_PRO_RETINA_PIXEL_DENSITY = 2;
    private int pixelDensity;
    private FirefoxProfile profile = null;
    /**
     * a flag available for chrome (use chrome developer tools)
     */
    private boolean USE_CDP = true;

    public BrowserOptions() {
        this.pixelDensity = -1;
    }

    /**
     * @param pixelDensity Specify the device scale factor or pixel density : For MacBook Pro, it is
     *                     2: use the MACBOOK_PRO_RETINA_PIXEL_DENSITY constant
     */
    public BrowserOptions(int pixelDensity) {
        //		super();
        this.pixelDensity = pixelDensity;
    }

    /**
     * @param pixelDensity Specify the device scale factor or pixel density : For MacBook Pro, it is
     *                     2: use the MACBOOK_PRO_RETINA_PIXEL_DENSITY constant
     * @param USE_CDP      Set true if you want to enable chrome developer tools (Used in clickable
     *                     detection)
     */
    public BrowserOptions(int pixelDensity, boolean USE_CDP) {
        this.pixelDensity = pixelDensity;
        this.USE_CDP = USE_CDP;
    }

    /**
     * @param USE_CDP Set true if you want to enable chrome developer tools (Used in clickable
     *                detection)
     */
    public BrowserOptions(boolean USE_CDP) {
        this.pixelDensity = -1;
        this.USE_CDP = USE_CDP;
    }

    public boolean isUSE_CDP() {
        return USE_CDP;
    }

    /**
     * Enables/Disables usage of Chrome Developer Protocol (CDP)
     *
     * @param USE_CDP
     */
    public void setUSE_CDP(boolean USE_CDP) {
        this.USE_CDP = USE_CDP;
    }

    public int getPixelDensity() {
        return pixelDensity;
    }

    public void setPixelDensity(int pixelDensity) {
        this.pixelDensity = pixelDensity;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "( pixelDensity: "
                + this.pixelDensity + ", USE_CDP : " + this.USE_CDP
                + ")";
    }

    public FirefoxProfile getProfile() {
        return this.profile;
    }

    public void setProfile(FirefoxProfile profile) {
        this.profile = profile;
    }
}
