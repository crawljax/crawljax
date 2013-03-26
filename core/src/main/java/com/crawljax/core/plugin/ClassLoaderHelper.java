package com.crawljax.core.plugin;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

// Internal helper class that creates a ClassLoader that is able to load classes
// from all jars in a directory (that is not originally in application's classpath
public final class ClassLoaderHelper
{
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
		for (File dir: directories)
		{
			fillJarsList(allJars, dir, includeSubDirs);
		}
		return new URLClassLoader(allJars.toArray(new URL[allJars.size()]), parent);
	}
	
	static private void fillJarsList(List<URL> jars, File dir, boolean includeSubDirs)
    {
		if(dir.exists())
		{
			try
			{
				for (File jar: dir.listFiles(_jarsFilter))
					jars.add(jar.toURI().toURL());
			
				if (includeSubDirs)
					for (File subdir: dir.listFiles(_dirsFilter))
						fillJarsList(jars, subdir, true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
    }

	private ClassLoaderHelper()
	{
	}
	
	static final private FileFilter _jarsFilter = new FileFilter()
	{
		public boolean accept(File pathname)
		{
			return	pathname.isFile()
				&&	pathname.getName().toUpperCase().endsWith(JAR_SUFFIX);
		}
	};

	static final private FileFilter _dirsFilter = new FileFilter()
	{
		public boolean accept(File pathname)
		{
			return	pathname.isDirectory();
		}
	};

	static final private String JAR_SUFFIX = ".JAR";
}
