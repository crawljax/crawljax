package com.crawljax.web.model;

import com.crawljax.core.plugin.HostInterface;
import com.crawljax.web.exception.CrawljaxWebException;
import com.crawljax.web.fs.PluginManager;
import com.google.common.io.ByteStreams;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Singleton
public class Plugins {
	private static final Logger LOG = LoggerFactory.getLogger(Plugins.class);

	private final Map<String, Plugin> pluginList;
	private final PluginManager pluginManager;

	Set<Class<? extends com.crawljax.core.plugin.Plugin>> basePluginClasses;

	@Inject
	public Plugins(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
		pluginList = pluginManager.loadAll();

		Reflections reflections = new Reflections("com.crawljax.core.plugin");
		basePluginClasses = reflections.getSubTypesOf(com.crawljax.core.plugin.Plugin.class);
	}

	/**
	 * @return the pluginList
	 */
	public Collection<Plugin> getPluginList() {
		return pluginList.values();
	}

	public Plugin add(String fileName, byte[] data) throws CrawljaxWebException {
		int extensionIndex = fileName.indexOf(".jar");
		if (extensionIndex < 0) {
			throw new CrawljaxWebException("Expected .jar file");
		}
		String id = adaptToId(fileName.substring(0, extensionIndex));
		Plugin plugin = pluginManager.save(id, data);
		if(plugin != null) {
			pluginList.put(plugin.getId(), plugin);
		}
		return plugin;
	}

	public Plugin add(String fileName, URL url) throws CrawljaxWebException {
		int extensionIndex = fileName.indexOf(".jar");
		if (extensionIndex > 0) {
			fileName = fileName.substring(0, extensionIndex);
		}
		String id = adaptToId(fileName);
		Plugin plugin = pluginManager.save(id, url);
		if(plugin != null) {
			pluginList.put(plugin.getId(), plugin);
		}
		return plugin;
	}

	private String adaptToId(String id) {
		id = id.toLowerCase().replaceAll("[^a-z0-9]+", "-");
		if (pluginList.containsKey(id)) {
			int i = 1;
			while (pluginList.containsKey(id + Integer.toString(i))) {
				i++;
			}
			id += Integer.toString(i);
		}
		return id;
	}

	public Plugin remove(Plugin plugin) throws CrawljaxWebException {
		if(pluginManager.delete(plugin.getId())) {
			pluginList.remove(plugin.getId());
		} else {
			throw new CrawljaxWebException("Failed to delete plugin file");
		}
		return plugin;
	}

	public Plugin findByID(final String id) {
		return pluginList.get(id);
	}

	public com.crawljax.core.plugin.Plugin getInstanceOf(Plugin plugin, File resourceDir,
	        HostInterface hostInterface) {
		File source = reload(plugin.getId()).getJarFile();
		File dest = new File(resourceDir, plugin.getJarFile().getName());
		copyFileContents(source, dest);
		URL instanceURL;
		try {
			instanceURL = dest.toURI().toURL();
		} catch (MalformedURLException e) {
			LOG.error("Could not create instance of plugin {}", plugin.getName());
			LOG.debug("Could not create instance of plugin {}. {}", plugin.getName(), e.getStackTrace());
			return null;
		}

		ClassLoader newClassLoader = new URLClassLoader(new URL[]{instanceURL}, Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(newClassLoader);

		Reflections reflections = new Reflections(new ConfigurationBuilder()
					.addUrls(instanceURL, ClasspathHelper.forClass(com.crawljax.core.plugin.Plugin.class))
					.addClassLoader(newClassLoader));

		Set<Class<? extends com.crawljax.core.plugin.Plugin>> pluginClasses = reflections.getSubTypesOf(com.crawljax.core.plugin.Plugin.class);
		pluginClasses.removeAll(basePluginClasses);

		com.crawljax.core.plugin.Plugin instance = null;
		for(Class<? extends com.crawljax.core.plugin.Plugin> pluginClass : pluginClasses) {
			try {
				Constructor constructor = pluginClass.getDeclaredConstructor(HostInterface.class);
				instance = (com.crawljax.core.plugin.Plugin) constructor.newInstance(hostInterface);
				break;
			} catch (Exception e) { }
		}

		if(instance == null) {
			LOG.error("Could not create instance of plugin " + plugin.getName());
			LOG.debug("Could not create instance of plugin " + plugin.getName());
		}

		return instance;
	}

	public Plugin reload(String pluginId) {
		pluginList.put(pluginId, pluginManager.load(pluginId));
		return pluginList.get(pluginId);
	}

	public Collection<Plugin> reloadAll() {
		pluginList.clear();
		pluginList.putAll(pluginManager.loadAll());
		return pluginList.values();
	}

	private void copyFileContents(File source, File dest) {
		try (InputStream in = new FileInputStream(source)) {
			try(FileOutputStream out = new FileOutputStream(dest)) {
				ByteStreams.copy(in, out);
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not copy file " + source + " to " + dest);
		}
	}
}
