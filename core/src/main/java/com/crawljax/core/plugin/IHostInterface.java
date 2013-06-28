package com.crawljax.core.plugin;

import java.io.File;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 2013/05/31
 * Time: 12:19 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IHostInterface {

	public File getOutputDirectory();
	public Map<String, String> getParameters();
}
