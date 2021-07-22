/*
 * Adapted from http://pdiff.sourceforge.net/
 *
 * Copyright (C) 2006 Yangli Hector Yee
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
package com.crawljax.stateabstractions.visual;

import static java.awt.Transparency.OPAQUE;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.log10;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Adapted from <a href="http://pdiff.sourceforge.net/">Perceptual Image
 * Difference Utility</a>.
 */
public class PerceptualImageDifferencing {

	public static final int COLOR_PASS = 0xff000000;
	public static final int COLOR_FAIL = 0xffffffff;

	private static final int MAX_PYR_LEVELS = 8;

	private final double colorFactor;
	private final double fieldOfView;
	private final double gamma;
	private final double luminance;
	private final boolean luminanceOnly;
	private final int thresholdPixels;
	private final boolean failFast;

	private final double numOneDegreePixels;
	private final int adaptationLevel;

	private final double[] lut = new double[256];

	private static final int ALPHA_MASK = 0xff000000;

	/**
	 * Builds parameter list for the PerceptualDiff
	 * {@linkplain PerceptualImageDifferencing#PerceptualDiff(double, int, boolean, double, double, boolean, double)
	 * constructor}.
	 */
	public static class Builder {

		private double colorFactor = 1.0;
		private boolean failFast = false;
		private double fieldOfView = 45.0;
		private double gamma = 2.2;
		private double luminance = 100.0;
		private boolean luminanceOnly = false;
		private int thresholdPixels = 100;

		public Builder setColorFactor(double colorFactor) {
			this.colorFactor = colorFactor;
			return this;
		}

		public Builder setFailFast(boolean failFast) {
			this.failFast = failFast;
			return this;
		}

		public Builder setFieldOfView(double fieldOfView) {
			this.fieldOfView = fieldOfView;
			return this;
		}

		public Builder setGamma(double gamma) {
			this.gamma = gamma;
			return this;
		}

		public Builder setLuminance(double luminance) {
			this.luminance = luminance;
			return this;
		}

		public Builder setLuminanceOnly(boolean luminanceOnly) {
			this.luminanceOnly = luminanceOnly;
			return this;
		}

		public Builder setThresholdPixels(int thresholdPixels) {
			this.thresholdPixels = thresholdPixels;
			return this;
		}

		public PerceptualImageDifferencing build() {
			return new PerceptualImageDifferencing(fieldOfView, thresholdPixels, failFast, gamma, luminance, luminanceOnly, colorFactor);
		}
	}

	/**
	 * Creates PerceptualDiff instance with specified parameters.
	 * 
	 * @param fieldOfView
	 *            field of view in degrees
	 * @param thresholdPixels
	 *            failure threshold
	 * @param failFast
	 *            whether to fail immediately when threshold is reached
	 * @param gamma
	 *            value to convert rgb into linear space
	 * @param luminance
	 *            white luminance
	 * @param luminanceOnly
	 *            whether to ignore chroma in comparison
	 * @param colorFactor
	 *            how much of color to use
	 */
	public PerceptualImageDifferencing(double fieldOfView, int thresholdPixels, boolean failFast, double gamma, double luminance,
			boolean luminanceOnly, double colorFactor) {
		this.colorFactor = colorFactor;
		this.fieldOfView = fieldOfView;
		this.gamma = gamma;
		this.luminance = luminance;
		this.luminanceOnly = luminanceOnly;
		this.thresholdPixels = thresholdPixels;
		this.failFast = failFast;

		numOneDegreePixels = 2 * tan(fieldOfView * 0.5 * PI / 180) * 180 / PI;

		double numPixels = 1;
		int level = 0;
		for (int i = 0; i < MAX_PYR_LEVELS; i++) {
			level = i;
			if (numPixels > numOneDegreePixels) {
				break;
			}
			numPixels *= 2;
		}
		adaptationLevel = level;

		// precompute color conversion table
		for (int i = 0; i < lut.length; i++) {
			lut[i] = pow(i / 255.0, gamma);
		}
	}

	/**
	 * Prints some parameters to the log.
	 */
	public void dump() {
		System.out.println(String.format("Field of view is %s degrees", fieldOfView));
		System.out.println(String.format("Threshold is %d pixels", thresholdPixels));
		System.out.println(String.format("Gamma is %s", gamma));
		System.out.println(String.format("The display's Luminance is %s candelas per meter squared", luminance));
	}

	/**
	 * Compares images using Yee's method.
	 * 
	 * References: A Perceptual Metric for Production Testing, Hector Yee, Journal
	 * of Graphics Tools 2004.
	 * 
	 * @param pool
	 *            fork-join pool for task execution
	 * @param imgA
	 *            first of two images to compare
	 * @param imgB
	 *            second of two images to compare
	 * @param imgDiff
	 *            accumulates differences (optional)
	 * @return whether images are perceptually indistinguishable
	 */
	public boolean compare(ForkJoinPool pool, BufferedImage imgA, BufferedImage imgB, BufferedImage imgDiff) {

		if (imgA.getWidth() != imgB.getWidth() || imgA.getHeight() != imgB.getHeight()) {
			// System.err.println("Image dimensions do not match");
			// return false;
			for (int r = 0; r < imgDiff.getWidth(); r++) {
				for (int c = 0; c < imgDiff.getHeight(); c++) {
					imgDiff.setRGB(r, c, COLOR_FAIL);
				}
			}
		}

		int w = (int) Math.min(imgA.getWidth(), imgB.getWidth());
		int h = (int) Math.min(imgA.getHeight(), imgB.getHeight());

		// assuming colorspaces are in Adobe RGB (1998)
		int[] aRGB = imgA.getRGB(0, 0, w, h, null, 0, w);
		int[] bRGB = imgB.getRGB(0, 0, w, h, null, 0, w);

		// accept if all pixels are identical
		if (Arrays.equals(aRGB, bRGB)) {
			// System.out.println("Images are binary identical");
			return true;
		}

		int dim = aRGB.length;

		// reject if alpha values are not identical
		if (imgA.getTransparency() != OPAQUE || imgB.getTransparency() != OPAQUE) {
			for (int index = 0; index < dim; index++) {
				if ((aRGB[index] & ALPHA_MASK) != (bRGB[index] & ALPHA_MASK)) {
					// System.out.println("Images have different alpha values");
					return false;
				}
			}
		}

		// assuming colorspaces are in Adobe RGB (1998) convert to XYZ
		float[] aA = new float[dim];
		float[] bA = new float[dim];
		float[] aB = new float[dim];
		float[] bB = new float[dim];

		// Successively blurred versions of the original image.
		float[][] la = new float[MAX_PYR_LEVELS][dim];
		float[][] lb = new float[MAX_PYR_LEVELS][dim];

		// System.out.println("Converting RGB and constructing Laplacian Pyramids");

		ForkJoinTask<?> taskA = pool.submit(new PyramidTask(aRGB, aA, aB, la, w, h));
		ForkJoinTask<?> taskB = pool.submit(new PyramidTask(bRGB, bA, bB, lb, w, h));

		double pixelsPerDegree = w / numOneDegreePixels;

		double[] cpd = new double[MAX_PYR_LEVELS];
		cpd[0] = 0.5 * pixelsPerDegree;
		for (int i = 1; i < MAX_PYR_LEVELS; i++) {
			cpd[i] = 0.5 * cpd[i - 1];
		}
		double csfMax = csf(3.248, 100.0);

		double[] freq = new double[MAX_PYR_LEVELS - 2];
		for (int i = 0; i < MAX_PYR_LEVELS - 2; i++) {
			freq[i] = csfMax / csf(cpd[i], 100.0);
		}

		taskA.join();
		taskB.join();

		// System.out.println("Performing test");

		AtomicInteger pixelsFailed = new AtomicInteger();

		int[] pixDiff = (imgDiff != null) ? new int[dim] : null;

		boolean completed = pool.invoke(new Comparison(aA, aB, la, bA, bB, lb, pixelsFailed, pixDiff, adaptationLevel, cpd, freq).rootTask());
		assert completed | failFast;

		if (imgDiff != null) {
			imgDiff.setRGB(0, 0, w, h, pixDiff, 0, w);
		}

		String difference = String.format("%d pixels are different", pixelsFailed.get());

		if (pixelsFailed.get() >= thresholdPixels) {
			// System.out.println("Images are visibly different");
			if (failFast) {
				difference = "At least " + difference;
			}
			// System.out.println(difference);
			return false;
		}

		// System.out.println("Images are perceptually indistinguishable");
		// System.out.println("pixel differences = " + difference);
		return true;
	}

	/**
	 * Converts color values and constructs Laplacian pyramids.
	 */
	private class PyramidTask implements Runnable {

		private final int[] rgb;
		private final float[] a;
		private final float[] b;
		private final float[][] levels;
		private final int width;
		private final int height;

		protected PyramidTask(int[] rgb, float[] a, float[] b, float[][] levels, int width, int height) {
			this.rgb = rgb;
			this.a = a;
			this.b = b;
			this.levels = levels;
			this.width = width;
			this.height = height;
		}

		public void run() {
			convert(rgb, a, b, levels[0]);
			construct(levels, width, height);
		}
	}

	/**
	 * Converts RGB to AB and luminance.
	 * 
	 * @param rgb
	 *            pixel values
	 * @param a
	 *            A
	 * @param b
	 *            B
	 * @param lum
	 *            Y * luminance
	 */
	protected void convert(int[] rgb, float[] a, float[] b, float[] lum) {
		for (int index = 0; index < rgb.length; index++) {
			int color = rgb[index];
			double red = lut[(color >> 16) & 0xff];
			double grn = lut[(color >> 8) & 0xff];
			double blu = lut[color & 0xff];

			/*
			 * Convert from Adobe RGB (1998) with reference white D65 to XYZ. Matrix is from
			 * http://www.brucelindbloom.com/
			 */
			double x = red * 0.5767309 + grn * 0.1855540 + blu * 0.1881852;
			double y = red * 0.2973769 + grn * 0.6273491 + blu * 0.0752741;
			double z = red * 0.0270343 + grn * 0.0706872 + blu * 0.9911085;

			/*
			 * Convert XYZ to LAB
			 */
			double[] f = new double[3];
			double[] r = { x / XW, y / YW, z / ZW };
			for (int i = 0; i < 3; i++) {
				if (r[i] > EPSILON) {
					f[i] = pow(r[i], 1.0 / 3.0);
				} else {
					f[i] = (KAPPA * r[i] + 16.0) / 116.0;
				}
			}

			// L = 116.0 * f[1] - 16.0; // unused
			a[index] = (float) (500.0 * (f[0] - f[1]));
			b[index] = (float) (200.0 * (f[1] - f[2]));

			lum[index] = (float) (y * luminance);
		}
	}

	/* Reference white */
	private static final double XW = 0.5767309 + 0.1855540 + 0.1881852;
	private static final double YW = 0.2973769 + 0.6273491 + 0.0752741;
	private static final double ZW = 0.0270343 + 0.0706872 + 0.9911085;

	/* Constants for XYZ to LAB conversion. */
	private static final double EPSILON = 216.0 / 24389.0;
	private static final double KAPPA = 24389.0 / 27.0;

	/**
	 * Filter kernel for Laplacian convolution.
	 */
	private static final float[] KERNEL = { 0.05f, 0.25f, 0.4f, 0.25f, 0.05f };

	/**
	 * Constructs the Laplacian pyramid by successively copying earlier levels and
	 * blurring them.
	 */
	protected static void construct(float[][] levels, int width, int height) {
		float[] tmp = new float[height * width]; // transposed
		for (int i = 1, n = levels.length; i < n; i++) {
			// apply filter kernel horizontally and then vertically
			convolveAndTranspose(levels[i - 1], tmp, width, height);
			convolveAndTranspose(tmp, levels[i], height, width);
		}
	}

	/**
	 * Convolves image <code>src</code> with 1D filter kernel and stores it
	 * transposed in <code>dst</code>.
	 * <p>
	 * Adapted from <a href="http://www.jhlabs.com/ip/GaussianFilter.java">Jerry
	 * Huxtable's Gaussian Filter</a>
	 */
	private static void convolveAndTranspose(float[] src, float[] dst, int width, int height) {
		for (int offset = 0, y = 0; y < height; y++, offset += width) {
			for (int index = y, x = 0; x < width; x++, index += height) {
				float f = 0;
				for (int i = -2; i <= 2; i++) {
					int ix = x + i;
					// wrap edges
					if (ix < 0) {
						ix = -ix;
					} else if (ix >= width) {
						ix = (width - ix) + width - 1;
					}
					f += KERNEL[i + 2] * src[offset + ix];
				}
				dst[index] = f;
			}
		}
	}

	/**
	 * ForkJoin idioms adapted from {@link java.util.concurrent.RecursiveAction
	 * sumOfSquares} sample.
	 */
	protected class Comparison {

		private static final int LEAF_SIZE = 512;

		private final float[] aA;
		private final float[] aB;
		private final float[][] la;

		private final float[] bA;
		private final float[] bB;
		private final float[][] lb;

		private final AtomicInteger pixelsFailed;

		private final int[] pixDiff;

		private final int adaptationLevel;

		private final double[] cpd;
		private final double[] freq;

		protected Comparison(float[] aA, float[] aB, float[][] la, float[] bA, float[] bB, float[][] lb, AtomicInteger pixelsFailed, int[] pixDiff,
				int adaptationLevel, double[] cpd, double[] freq) {
			this.aA = aA;
			this.aB = aB;
			this.la = la;
			this.bA = bA;
			this.bB = bB;
			this.lb = lb;
			this.pixelsFailed = pixelsFailed;
			this.pixDiff = pixDiff;
			this.adaptationLevel = adaptationLevel;
			this.cpd = cpd;
			this.freq = freq;
		}

		/**
		 * Returns root task for recursive comparison.
		 */
		protected RecursiveTask<Boolean> rootTask() {
			return new CompareTask(0, aA.length, null);
		}

		private class CompareTask extends RecursiveTask<Boolean> {

			private final int beginIndex;
			private final int endIndex;
			private final CompareTask next; // keeps track of forked tasks

			protected CompareTask(int beginIndex, int endIndex, CompareTask next) {
				this.beginIndex = beginIndex;
				this.endIndex = endIndex;
				this.next = next;
			}

			@Override
			protected Boolean compute() {
				int lo = beginIndex;
				int hi = endIndex;
				CompareTask right = null;
				while (!isCancelled() && hi - lo > LEAF_SIZE && getSurplusQueuedTaskCount() <= 3) {
					int mid = (lo + hi) >>> 1;
					right = new CompareTask(mid, hi, right);
					right.fork();
					hi = mid;
				}
				boolean running = atLeaf(lo, hi);
				while (right != null) {
					if (running) {
						if (right.tryUnfork()) {
							// directly calculate if not stolen
							running &= right.atLeaf(right.beginIndex, right.endIndex);
						} else {
							running &= right.join();
						}
					} else {
						right.cancel(false);
					}
					right = right.next;
				}
				return running;
			}

			/**
			 * Compares pixels in specified range of indices.
			 * 
			 * @param begin
			 *            beginning index, inclusive
			 * @param end
			 *            ending index, exclusive
			 * @return <code>true</code> if all pixels in specified range were compared
			 */
			protected boolean atLeaf(int begin, int end) {

				float[] contrast = new float[MAX_PYR_LEVELS - 2];
				double[] mask = new double[MAX_PYR_LEVELS - 2];

				for (int index = begin; index < end; index++) {
					if (isCancelled()) {
						return false;
					}
					float sumContrast = 0;
					for (int i = 0; i < MAX_PYR_LEVELS - 2; i++) {
						float n1 = abs(la[i][index] - la[i + 1][index]);
						float n2 = abs(lb[i][index] - lb[i + 1][index]);
						float numerator = (n1 > n2) ? n1 : n2;
						float d1 = abs(la[i + 2][index]);
						float d2 = abs(lb[i + 2][index]);
						float denominator = (d1 > d2) ? d1 : d2;
						if (denominator < 1e-5f) {
							denominator = 1e-5f;
						}
						contrast[i] = numerator / denominator;
						sumContrast += contrast[i];
					}
					if (sumContrast < 1e-5f) {
						sumContrast = 1e-5f;
					}

					double adapt = 0.5 * (la[adaptationLevel][index] + lb[adaptationLevel][index]);
					if (adapt < 1e-5f) {
						adapt = 1e-5f;
					}
					for (int i = 0; i < MAX_PYR_LEVELS - 2; i++) {
						mask[i] = mask(contrast[i] * csf(cpd[i], adapt));
					}
					double factor = 0;
					for (int i = 0; i < MAX_PYR_LEVELS - 2; i++) {
						factor += contrast[i] * freq[i] * mask[i] / sumContrast;
					}
					if (factor < 1) {
						factor = 1;
					}
					if (factor > 10) {
						factor = 10;
					}
					double delta = abs(la[0][index] - lb[0][index]);

					boolean pass = true;
					// pure luminance test
					if (delta > factor * tvi(adapt)) {
						pass = false;
					} else if (!luminanceOnly) {
						// CIE delta E test with modifications
						// skip color test in scotopic regions
						if (adapt >= 10.0) {
							float da = aA[index] - bA[index];
							float db = aB[index] - bB[index];
							double deltaE = (da * da + db * db) * colorFactor;
							if (deltaE > factor) {
								pass = false;
							}
						}
					}

					if (pixDiff != null) {
						pixDiff[index] = pass ? COLOR_PASS : COLOR_FAIL;
					}

					if (!pass && pixelsFailed.incrementAndGet() >= thresholdPixels && failFast) {
						return false;
					}
				}
				return true;
			}
		}
	}

	/**
	 * Given the adaptation luminance, computes the threshold of visibility in cd
	 * per m^2.
	 * 
	 * TVI means Threshold vs Intensity function.
	 * 
	 * This version comes from Ward Larson Siggraph 1997
	 */
	private static double tvi(double adaptationLuminance) {
		double logA = log10(adaptationLuminance);
		double r;
		if (logA < -3.94) {
			r = -2.86;
		} else if (logA < -1.44) {
			r = pow(0.405 * logA + 1.6, 2.18) - 2.86;
		} else if (logA < -0.0184) {
			r = logA - 0.395;
		} else if (logA < 1.9) {
			r = pow(0.249 * logA + 0.65, 2.7) - 0.72;
		} else {
			r = logA - 1.255;
		}
		return pow(10.0, r);
	}

	/**
	 * Computes the contrast sensitivity function (Barten SPIE 1989) given the
	 * cycles per degree (cpd) and luminance (lum).
	 */
	private static double csf(double cpd, double lum) {
		double a = 440.0 * pow((1.0 + 0.7 / lum), -0.2);
		double b = 0.3 * pow((1.0 + 100.0 / lum), 0.15);
		return a * cpd * exp(-b * cpd) * sqrt(1.0 + 0.06 * exp(b * cpd));
	}

	/**
	 * Visual Masking Function from Daly 1993
	 */
	private static double mask(double contrast) {
		double a = pow(392.498 * contrast, 0.7);
		double b = pow(0.0153 * a, 4.0);
		return pow(1.0 + b, 0.25);
	}

	private static final boolean FAST_POW = true;

	private static double pow(double a, double b) {
		return FAST_POW ? fastpow(a, b) : Math.pow(a, b);
	}

	/**
	 * Returns the first argument raised to the power of the second argument.
	 * 
	 * This approximate implementation from <a href=
	 * "http://martin.ankerl.com/2012/01/25/optimized-approximative-pow-in-c-and-cpp/"
	 * > martin.ankerl.com</a> is several times faster than
	 * {@link Math#pow(double, double) Math.pow}.
	 * 
	 * <p>
	 * For an implementation with adjustable accuracy, see article by Harrison
	 * Ainsworth: <a href=
	 * "http://www.hxa.name/articles/content/fast-pow-adjustable_hxa7241_2007.html"
	 * > Fast pow() With Adjustable Accuracy</a>.
	 * 
	 * @param a
	 *            the base
	 * @param b
	 *            the exponent
	 * @return the value a<sup>b</sup>
	 */
	private static double fastpow(double a, double b) {
		// if b < 0, compute 1.0/pow(a, -b)
		boolean negative = b < 0;
		if (negative) {
			b = -b;
		}
		// exponentiation by squaring
		double r = 1.0;
		int exp = (int) b;
		double base = a;
		while (exp != 0) {
			if ((exp & 1) != 0) {
				r *= base;
			}
			base *= base;
			exp >>= 1;
		}
		// use the IEEE 754 trick for the fraction of the exponent
		double bFraction = b - (int) b;
		long tmp = Double.doubleToLongBits(a);
		long tmp2 = (long) (bFraction * (tmp - 4606921280493453312L)) + 4606921280493453312L;
		double result = r * Double.longBitsToDouble(tmp2);
		return negative ? (1.0 / result) : result;
	}
}