package com.crawljax.vips_selenium;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.browser.WebDriverBrowserBuilder;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.BrowserOptions;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.util.DomUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

/**
 * Vision-based Page Segmentation algorithm
 * @author Tomas Popela
 *
 */
public class VipsSelenium {
	private static final Logger LOG = LoggerFactory.getLogger(VipsSelenium.class);

	private String url = null;
	public WebDriver driver  = null;
	private EmbeddedBrowser browser = null;
	public Document dom = null;
	public BufferedImage viewport = null;

	private boolean graphicsOutput = false;
	private boolean outputToFolder = false;
	private boolean outputEscaping = true;
	private int pDoC = 11;
	private String filename = "test";
	private	int sizeTresholdWidth = 850;
	private	int sizeTresholdHeight = 900;

	private PrintStream originalOut = null;
	long startTime = 0;
	long endTime = 0;
	private int numberOfIterations = 2;
	private File outputFolder = new File("testOutput");

	private boolean fragOutput = true;

	/**
	 * Default constructor
	 */
	public VipsSelenium(String url)
	{
		this.url = url;
		CrawljaxConfigurationBuilder configBuilder = CrawljaxConfiguration.builderFor(url);
		BrowserConfiguration browserConfiguration = new BrowserConfiguration(BrowserType.CHROME, 1,
             //   new BrowserOptions(BrowserOptions.MACBOOK_PRO_RETINA_PIXEL_DENSITY));
				new BrowserOptions());
		configBuilder.setBrowserConfig(browserConfiguration);
		WebDriverBrowserBuilder builder = new WebDriverBrowserBuilder(configBuilder.build(), null);
	
		browser = builder.get();
		driver = browser.getWebDriver();
		driver.navigate().to(url);
		
		// phoenix
		
		driver.findElement(By.xpath("//*[@id=\"sign_in_form\"]/button")).click();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		driver.findElement(By.id("add_new_board")).click();
		
		// addressbook
//		driver.findElement(By.name("user")).sendKeys("admin");
//		driver.findElement(By.name("pass")).sendKeys("admin");
//		driver.findElement(By.xpath("//*[@id=\"LoginForm\"]/input[3]")).click();
//		
//		//claroline
//		driver.findElement(By.id("login")).sendKeys("astocco");
//		driver.findElement(By.id("password")).sendKeys("password");
//		driver.findElement(By.xpath("//*[@id=\"loginBox\"]/form/fieldset/button")).click();
//		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//driver.findElement(By.xpath("//*[@id='nav']/ul/li[2]/a")).click();

		//		driver.findElement(By.xpath("//*[@id='userBannerRight']/ul/li[2]/span/a")).click();;
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		exportPageToImage();

		getDomTree();
		
		if(!this.outputFolder.exists()) {
			this.outputFolder.mkdirs();
		}
		if(driver!=null) {
			VipsUtils.populateStyle(dom, driver);
		}
	}
	
	public VipsSelenium(WebDriver driver, Document dom, BufferedImage screenshot, int numberOfIterations, File folder, String filename, boolean fragOutput) {
		this.driver = driver;
		this.dom = dom;
		this.viewport = screenshot;
		this.fragOutput  = fragOutput;
		this.numberOfIterations = numberOfIterations;
		this.sizeTresholdHeight = this.sizeTresholdWidth = ((numberOfIterations-5)*50 + 100);
		if(folder != null) {
			this.outputFolder  = folder;
		}
		else {
			if(!this.outputFolder.exists()) {
				this.outputFolder.mkdirs();
			}
		}
		this.filename =  filename;
		if(driver!=null) {
			VipsUtils.populateStyle(dom, driver);
		}
	}

	/**
	 * Enables or disables graphics output of VIPS algorithm.
	 * @param enable True for enable, otherwise false.
	 */
	public void enableGraphicsOutput(boolean enable)
	{
		graphicsOutput = enable;
	}

	/**
	 * Enables or disables creation of new directory for every algorithm run.
	 * @param enable True for enable, otherwise false.
	 */
	public void enableOutputToFolder(boolean enable)
	{
		outputToFolder = enable;
	}

	/**
	 * Enables or disables output XML character escaping.
	 * @param enable True for enable, otherwise false.
	 */
	public void enableOutputEscaping(boolean enable)
	{
		outputEscaping = enable;
	}

	/**
	 * Sets permitted degree of coherence (pDoC) value.
	 * @param value pDoC value.
	 */
	public void setPredefinedDoC(int value)
	{
		if (value <= 0 || value > 11)
		{
			System.err.println("pDoC value must be between 1 and 11! Not " + value + "!");
			return;
		}
		else
		{
			pDoC = value;
		}
	}

	/**
	 * Parses a builds DOM tree from page source.
	 * @param urlStream Input stream with page source.
	 */
	private void getDomTree()
	{
		try {
			dom = DomUtils.asDocument(driver.getPageSource());
			boolean offline = false;
			com.crawljax.vips_selenium.VipsUtils.cleanDom(dom, offline);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * Exports rendered page to image.
	 */
	private void exportPageToImage()
	{
		try
		{
			viewport = browser.getScreenShotAsBufferedImage(1000);
			String filename = System.getProperty("user.dir") + "/page.png";
			ImageIO.write(viewport, "png", new File(filename));
		} catch (Exception e)
		{
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Generates folder filename
	 * @return Folder filename
	 */
	private String generateFolderName()
	{
		
		String outputFolder = "";

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
		outputFolder += sdf.format(cal.getTime());
		outputFolder += "_";
		try {
			outputFolder += (new URL(url)).getHost().replaceAll("\\.", "_").replaceAll("/", "_");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return outputFolder;
	}

	/**
	 * Performs page segmentation.
	 * @return 
	 */
	private List<VipsRectangle> performSegmentation()
	{

		startTime = System.nanoTime();
		int pageWidth = viewport.getWidth();
		int pageHeight = viewport.getHeight();

		
		VipsSeparatorGraphicsDetector detector;
		VipsSeleniumParser vipsParser = new VipsSeleniumParser(this);
		VisualStructureConstructor constructor = new VisualStructureConstructor(pDoC, viewport, driver);
		constructor.setGraphicsOutput(graphicsOutput);
//		constructor.setGraphicsOutput(false);

		for (int iterationNumber = 1; iterationNumber < numberOfIterations+1; iterationNumber++)
		{
//			System.out.println("Iteration " + iterationNumber);
			detector = new VipsSeparatorGraphicsDetector(viewport, driver);

			//visual blocks detection
			vipsParser.setSizeTresholdHeight(sizeTresholdHeight);
			vipsParser.setSizeTresholdWidth(sizeTresholdWidth);

			vipsParser.parse();

			Node vipsBlocks = vipsParser.getVipsBlocks();

			if (iterationNumber == 1)
			{
				if (graphicsOutput)
				{
					// in first round we'll export global separators
					detector.setVipsBlock(vipsBlocks);
					detector.fillPool();
					detector.saveToImage("blocks" + iterationNumber);
					detector.setCleanUpSeparators(0);
					detector.detectHorizontalSeparators();
					detector.detectVerticalSeparators();
					detector.exportHorizontalSeparatorsToImage();
					detector.exportVerticalSeparatorsToImage();
					detector.exportAllToImage();
				}

				// visual structure construction
				constructor.setVipsBlocks(vipsBlocks);
				constructor.setPageSize(pageWidth, pageHeight);
			}
			else
			{
				vipsBlocks = vipsParser.getVipsBlocks();
				constructor.updateVipsBlocks(vipsBlocks);

				if (graphicsOutput)
				{
					detector.setVisualBlocks(constructor.getVisualBlocks());
					detector.fillPool();
					detector.saveToImage("blocks" + iterationNumber);
				}
			}

			// visual structure construction
			constructor.constructVisualStructure();

			// prepare tresholds for next iteration
			if (iterationNumber <= numberOfIterations-5)
			{
				sizeTresholdHeight -= 50;
				sizeTresholdWidth -= 50;

			}
			if (iterationNumber == numberOfIterations-4)
			{
				sizeTresholdHeight = 100;
				sizeTresholdWidth = 100;
			}
			if (iterationNumber == numberOfIterations-3)
			{
				sizeTresholdHeight = 80;
				sizeTresholdWidth = 80;
			}
			if (iterationNumber == numberOfIterations-2)
			{
				sizeTresholdHeight = 50;
				sizeTresholdWidth = 50;
			}
			if (iterationNumber == numberOfIterations-1)
			{
				sizeTresholdHeight = 1;
				sizeTresholdWidth = 1;
			}

		}

		constructor.normalizeSeparatorsSoftMax();
		constructor.normalizeSeparatorsMinMax();

		File xmlTarget = new File(this.outputFolder, "vipsOutput_" + this.filename + ".xml");
		File screenshotFile = new File(this.outputFolder, "frag_" + this.filename +  ".png");
		VipsOutput vipsOutput = new VipsOutput(pDoC, xmlTarget, fragOutput);
		vipsOutput.setEscapeOutput(outputEscaping);
//		vipsOutput.setOutputFileName(filename);
		
		if(graphicsOutput)
			vipsOutput.writeXML(constructor.getVisualStructure(), viewport, url, driver.getTitle());
		
		List<VipsRectangle> rectangles = vipsOutput.exportVisualStructureToImage(constructor.getVisualStructure(), viewport, screenshotFile, fragOutput, driver);
		
		if(fragOutput) {
			File jsonTarget = new File(this.outputFolder,   "vipsOutput_" + this.filename + ".json");
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			try {
				FileWriter fileWriter = new FileWriter(jsonTarget);
				gson.toJson(rectangles, fileWriter);
				fileWriter.flush();
				fileWriter.close();
	//			System.out.println(gson.toJson(rectangles));
			} catch (JsonIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		endTime = System.nanoTime();

		long diff = endTime - startTime;

		LOG.info("Execution time of VIPS: " + diff + " ns; " +
				(diff / 1000000.0) + " ms; " +
				(diff / 1000000000.0) + " sec");
		
		return rectangles;
	}

	/**
	 * Restores stdout
	 */
	private void restoreOut()
	{
		if (originalOut != null)
		{
			System.setOut(originalOut);
		}
	}

	/**
	 * Redirects stdout to nowhere
	 */
	private void redirectOut()
	{
		originalOut = System.out;
		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException
			{

			}
		}));
	}

	/**
	 * Starts visual segmentation of page
	 * @return 
	 * @throws Exception
	 */
	public List<VipsRectangle> startSegmentation()
	{
		try
		{

//			redirectOut();

			startTime = System.nanoTime();
//			getViewport();
//			restoreOut();

			String outputFolder = "";
			String oldWorkingDirectory = "";
			String newWorkingDirectory = "";

			if (outputToFolder)
			{
				outputFolder = generateFolderName();

				if (!new File(outputFolder).mkdir())
				{
					System.err.println("Something goes wrong during directory creation!");
				}
				else
				{
					oldWorkingDirectory = System.getProperty("user.dir");
					newWorkingDirectory += oldWorkingDirectory + "/" + outputFolder + "/";
					System.setProperty("user.dir", newWorkingDirectory);
				}
			}

			List<VipsRectangle> rectangles = performSegmentation();

			if (outputToFolder)
				System.setProperty("user.dir", oldWorkingDirectory);
			
			return rectangles;
		}
		catch (Exception e)
		{
			System.err.println("Something's wrong!");
			e.printStackTrace();
		}
		return null;
	}

	public void setOutputFileName(String filenameStr)
	{
		if (!filenameStr.equals(""))
		{
			filename = filenameStr;
		}
		else
		{
			LOG.info("Invalid filename!");
		}
	}

	public BufferedImage getViewport() {
		if(this.viewport != null) {
			return this.viewport;
		}
		exportPageToImage();
		return this.viewport;
	}

	public void cleanup() {
		this.driver.close();
	}
}
