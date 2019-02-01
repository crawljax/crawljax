package com.crawljax.core.configuration;

public class BrowserOptions {

	public static final int MACBOOK_PRO_RETINA_PIXEL_DENSITY = 2;
	private boolean headless;
	private int pixelDensity;

	public BrowserOptions() {
		this.headless = false;
		this.pixelDensity = -1;
	}

	/**
	 * @param headless Set true for Chrome and Firefox browsers to run them in headless mode
	 */
	public BrowserOptions(boolean headless) {
		super();
		this.headless = headless;
	}

	/**
	 * @param pixelDensity Specify the device scale factor or pixel density : For MacBook Pro, it is 2: use
	 *                     the MACBOOK_PRO_RETINA_PIXEL_DENSITY constant
	 */
	public BrowserOptions(int pixelDensity) {
		super();
		this.pixelDensity = pixelDensity;
	}

	/**
	 * @param headless     Set true for Chrome and Firefox browsers to run them in headless mode
	 * @param pixelDensity Specify the device scale factor or pixel density : For MacBook Pro, it is 2: use
	 *                     the MACBOOK_PRO_RETINA_PIXEL_DENSITY constant
	 */
	public BrowserOptions(boolean headless, int pixelDensity) {
		this.headless = headless;
		this.pixelDensity = pixelDensity;
	}

	public boolean isHeadless() {
		return this.headless;
	}

	public int getPixelDensity() {
		return pixelDensity;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + this.headless + ", " + this.pixelDensity
				+ ")";

	}

}
