package com.crawljax.test.rules;

import org.junit.rules.ExternalResource;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.io.FileUtils.deleteQuietly;

public class TempDirInTargetFolder extends ExternalResource {

	private static final String DATE_FORMAT = "yyyy-MM-dd-HH.mm.ss";
	private final File target;
	private final String prefix;
	private final boolean override;
	private File tmpDir;

	public TempDirInTargetFolder(String prefix, boolean override) {
		this.prefix = prefix;
		this.override = override;
		target = new File("target/test-data");
		if (!target.exists()) {
			boolean created = target.mkdirs();
			checkArgument(created, "Could not create target/test-data dir");
		}
	}

	@Override
	protected void before() {
		if (override) {
			tmpDir = new File(target, prefix);
			if (tmpDir.exists()) {
				deleteQuietly(tmpDir);
			}
		} else {
			SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
			String suffix = format.format(new Date());
			tmpDir = new File(target, prefix + '-' + suffix);
		}
		boolean created = tmpDir.mkdirs();
		checkArgument(created, "Could not create tmpDir");
	}

	public File getTempDir() {
		return tmpDir;
	}
}
