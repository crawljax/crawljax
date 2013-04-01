// Copyright 2008 Jean-Francois Poilpret
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.crawljax.core.plugin;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Provides helper functions for the PluginImporterClass that don't
// directly load files.
public final class PluginImporterHelper
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginImporterHelper.class);
	public static ClassLoader buildClassLoader(
	        boolean includeSubDirs, File... directories)
	{
		return buildClassLoader(
		        includeSubDirs, Thread.currentThread().getContextClassLoader(), directories);
	}

	static ClassLoader buildClassLoader(
	        boolean includeSubDirs, ClassLoader parent, File... directories)
	{
		List<URL> allJars = new ArrayList<URL>();
		// Find all Jars in each directory
		for (File dir : directories)
		{
			fillJarsList(allJars, dir, includeSubDirs);
		}
		return new URLClassLoader(allJars.toArray(new URL[allJars.size()]), parent);
	}

	public static File[] getDirsFromClassPath()
	{
		String classPath = System.getProperty("java.class.path");
		String[] paths = classPath.split(";");
		List<File> files = new ArrayList<File>();
		for (int iter = 0; iter < paths.length; iter++)
			files.add(new File(paths[iter]));
		return files.toArray(new File[files.size()]);
	}

	static public void fillJarsList(List<URL> jars, File dir, boolean includeSubDirs)
	{
		if (dir.exists())
		{
			try
			{
				for (File jar : dir.listFiles(_jarsFilter))
					jars.add(jar.toURI().toURL());

				if (includeSubDirs)
					for (File subdir : dir.listFiles(_dirsFilter))
						fillJarsList(jars, subdir, true);
			} catch (Exception e)
			{
				LOGGER.warn(e.getMessage(), e);
			}
		}
	}

	private PluginImporterHelper()
	{
	}

	static final private FileFilter _jarsFilter = new FileFilter()
	{
		public boolean accept(File pathname)
		{
			return pathname.isFile()
			        && pathname.getName().toUpperCase().endsWith(JAR_SUFFIX);
		}
	};

	static final private FileFilter _dirsFilter = new FileFilter()
	{
		public boolean accept(File pathname)
		{
			return pathname.isDirectory();
		}
	};

	static final private String JAR_SUFFIX = ".JAR";
}