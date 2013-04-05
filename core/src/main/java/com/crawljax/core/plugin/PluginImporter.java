package com.crawljax.core.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginImporter {
	private static ClassLoader cl = null;

	private static void getClassNamesInJar(List<String> classNames, File jarfile) {
		try {
			JarFile jarf = new JarFile(jarfile);
			Enumeration<? extends JarEntry> jarEntries = jarf.entries();

			while (jarEntries.hasMoreElements()) {
				JarEntry jarEntry = jarEntries.nextElement();
				if (jarEntry.getName().endsWith(".class")) {
					String className = jarEntry.getName();
					className = className.replace(".class", "").replace("/",
							".");
					classNames.add(className);
				}
			}
			jarf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static <T> List<T> getPluggedServices(Class<T> clazz,
			File... directories) {
		List<T> services = new ArrayList<T>();
		List<String> classNames = new ArrayList<String>();

		List<URL> jars = new ArrayList<URL>();

		for (File file : directories)
			PluginImporterHelper.fillJarsList(jars, file, true);

		if (cl == null)
			cl = PluginImporterHelper.buildClassLoader(true, directories);

		for (int iter = 0; iter < jars.size(); iter++)
			getClassNamesInJar(classNames, new File(jars.get(iter).getPath()));

		for (int iter = 0; iter < classNames.size(); iter++)
			addOneService(classNames.get(iter), clazz, services, cl);

		return services;
	}

	private static <T> void addOneService(String implementation,
			Class<T> clazz, List<T> services, ClassLoader loader) {
		try {
			Class<?> service = Class.forName(implementation, false, loader);
			if (clazz.isAssignableFrom(service)) {
				T tempClass = clazz.cast(service.newInstance());
				for (int i = 0; i < services.size(); i++)
					if (services.get(i).getClass().getName() == tempClass
							.getClass().getName())
						return;

				services.add(tempClass);
			}
		} catch (ClassNotFoundException | IllegalAccessException
				| InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
