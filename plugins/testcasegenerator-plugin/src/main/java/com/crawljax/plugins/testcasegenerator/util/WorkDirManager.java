package com.crawljax.plugins.testcasegenerator.util;

import com.crawljax.plugins.testcasegenerator.report.TestRecord;
import com.crawljax.util.DomUtils;
import com.crawljax.util.FSUtils;
import com.google.gson.Gson;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.inject.Singleton;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@Singleton
public class WorkDirManager {

	private static final Logger LOG = LoggerFactory.getLogger(WorkDirManager.class);

	public WorkDirManager() {
	}

	public void saveTestRecordMap(List<TestRecord> records, int testRunIndex, String url, String testRunFolder) {
		File runFile = new File(testRunFolder, "testRun.json");

		HashMap<String, TestRecord> recordMap = new HashMap<String, TestRecord>();

		for (TestRecord record : records) {
			recordMap.put(record.getMethodName(), record);
		}

		try {
			if (!runFile.exists()) {
				if (!runFile.getParentFile().exists()) {
					runFile.getParentFile().mkdirs();
				}
				runFile.createNewFile();
			}
			FileOutputStream file = new FileOutputStream(runFile);
			String json = new Gson().toJson(recordMap);
			file.write(json.getBytes());
			file.close();
			copyHTMLReport(testRunFolder, testRunIndex, url, recordMap);
		} catch (IOException e) {
			LOG.error("Could not save test run record {}", recordMap);
		}
	}

	private void copyHTMLReport(String testRunFolder, int testExecutionNumber, String url, HashMap<String, TestRecord> recordMap) throws IOException {
		VelocityEngine engine = new VelocityEngine();
		engine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.NullLogChute");
		engine.setProperty("resource.loader", "file");
		engine.setProperty("file.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		engine.init();
		VelocityContext context = new VelocityContext();
		recordMap.values().forEach(testRecord -> {
			testRecord.getDiffs().forEach(testStateDiff -> {
				// TODO: Checking the type of differences likes this is fragile.
				if (!testStateDiff.getOldState().endsWith(".png")) {
					try {
						testStateDiff.setOldState(formatDOM(testStateDiff.getOldState()));
						testStateDiff.setNewState(formatDOM(testStateDiff.getNewState()));
					} catch (IOException | TransformerException io) {
						LOG.error("Error while formatting the stripped DOM");
						io.printStackTrace();
					}
				}
			});
		});
		String json = new Gson().toJson(recordMap);
		context.put("diff_json", json.replace("\\", "\\\\").replace("`", "\\`"));
		context.put("url", url);
		context.put("execution", testExecutionNumber);
		Template template = engine.getTemplate("TestResults.html.vm");
		FSUtils.directoryCheck(testRunFolder);
		File f = new File(testRunFolder + File.separator + "TestResults.html");
		FileWriter writer = new FileWriter(f);
		template.merge(context, writer);
		writer.flush();
		writer.close();
	}

	private String formatDOM(String dom) throws TransformerException, IOException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "html");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		transformer.transform(new DOMSource(DomUtils.asDocument(dom)),
				new StreamResult(new OutputStreamWriter(baos, StandardCharsets.UTF_8)));
		baos.flush();
		return baos.toString("UTF-8");
	}

	public void saveTestRecord(TestRecord record, String testFolder) {
		File recordFile =
		        new File(testFolder, "test.json");
		try {
			if (!recordFile.exists()) {
				recordFile.getParentFile().mkdirs();
				recordFile.createNewFile();
				record.setOutputFolder(recordFile.getParent());
			}
			FileOutputStream file = new FileOutputStream(recordFile);
			file.write((new Gson().toJson(record).getBytes()));
			file.close();
			// mapper.writeValue(recordFile, record);
		} catch (IOException e) {
			LOG.error("Could not save crawl record {}", record);
		}
	}
	/*
	 * public List<TestRecord> loadTestRecords(File testFolder) { List<TestRecord> testRecords = new
	 * ArrayList<TestRecord>(); File[] testFiles = testFolder.listFiles(); for (File f : testFiles)
	 * { if (f.isDirectory()) { File record = new File(f, "test.json"); if (record.exists()) {
	 * TestRecord testRecord = loadTestRecord(record); // clean up records that crashed unexpectedly
	 * if (testRecord.getTestStatus() != TestStatusType.success && testRecord.getTestStatus() !=
	 * TestStatusType.failure) testRecord.setTestStatus(TestStatusType.failure); int length =
	 * testRecords.size(); if (length > 0) { for (int i = 0; i < length; i++) { if
	 * (testRecords.get(i).getMethodName() < testRecord.getMethodName()) { testRecords.add(i,
	 * testRecord); break; } } } else testRecords.add(testRecord); } } } return testRecords; }
	 */

	private static void writeThumbNail(File target, BufferedImage screenshot) throws IOException {
		int THUMBNAIL_WIDTH = 100;
		int THUMBNAIL_HEIGHT = 100;
		BufferedImage resizedImage =
		        new BufferedImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(screenshot, 0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Color.WHITE, null);
		g.dispose();
		ImageIO.write(resizedImage, "JPEG", target);
	}

	/*
	 * public TestRecord loadTestRecord(File testFile) { TestRecord testRecord = null; try {
	 * testRecord = mapper.readValue(testFile, TestRecord.class); } catch (IOException e) {
	 * LOG.error("Could not load test {}", testFile.getName()); } return testRecord; }
	 */

	public int getNumTestRecords(File testRecordsFolder) {
		int maxID = -1;
		if (testRecordsFolder.exists()) {

			/* Get the directories. */
			String[] dirs = testRecordsFolder.list(new FilenameFilter() {
				@Override
				public boolean accept(File current, String name) {
					return new File(current, name).isDirectory();
				}
			});

			if (null != dirs && dirs.length > 0) {
				/* Find the directory with the highest number. */
				maxID = -1;
				for (String dir : dirs) {
					try {
						int id = Integer.parseInt(dir);
						maxID = id > maxID ? id : maxID;
					} catch (NumberFormatException e) {
						continue;
					}
				}
			}
		} else {
			testRecordsFolder.mkdir();
		}
		return maxID + 1;
	}

	/**
	 * @param testRunFolderPath
	 *            the folder in which tests are
	 * @param testRecord
	 *            the actual test outcome
	 * @return the current output folder for the diff.
	 */
	public File getDiffsFolder(String testRunFolderPath, TestRecord testRecord) {
		File methodFolder =
		        new File(testRunFolderPath + File.separator + testRecord.getMethodName());
		if (!methodFolder.exists()) {
			methodFolder.mkdir();
		}
		File diffsFolder = new File(methodFolder, "diffs");

		if (!diffsFolder.exists()) {
			boolean created = diffsFolder.mkdirs();
			checkArgument(created, "Could not create diffs dir");
		}

		return diffsFolder;
	}

}
