package com.crawljax.web.model;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.crawljax.core.plugin.HostInterface;
import com.crawljax.web.exception.CrawljaxWebException;
import com.crawljax.web.fs.PluginManager;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Plugins {
	private static final Logger LOG = LoggerFactory.getLogger(Plugins.class);

	private final Map<String, Plugin> pluginList;
	private final PluginManager pluginManager;

	@Inject
	public Plugins(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
		pluginList = pluginManager.loadAll();
	}

	/**
	 * @return the pluginList
	 */
	public Collection<Plugin> getPluginList() {
		return pluginList.values();
	}

	public Collection<Plugin> reloadFromDisk() {
		pluginList.clear();
		pluginList.putAll(pluginManager.loadAll());
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

	public Plugin add(String name, URL url) throws CrawljaxWebException {
		String id = adaptToId(name);
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
		if(pluginManager.delete(plugin)) {
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
		File source = null;
		File dest = new File(resourceDir, plugin.getId() + ".jar");
		try {
			source = new File(plugin.getUrl().toURI());
			copyFile(source, dest);
		} catch (Exception e) {
			LOG.error("Could not create instance of plugin {}", plugin.getName());
			LOG.debug("Could not create instance of plugin {}. {}", plugin.getName(), e.getStackTrace());
			return null;
		}
		URL instanceURL = null;
		try {
			instanceURL = (new File(resourceDir.getAbsolutePath() + File.separatorChar + source.getName())).toURI().toURL();
		} catch (MalformedURLException e) {
			LOG.error("Could not create instance of plugin {}", plugin.getName());
			LOG.debug("Could not create instance of plugin {}. {}", plugin.getName(), e.getStackTrace());
			return null;
		}
		com.crawljax.core.plugin.Plugin instance = null;
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		ClassLoader newClassLoader = new URLClassLoader(new URL[]{instanceURL}, originalClassLoader);
		try {
			Thread.currentThread().setContextClassLoader(newClassLoader);
			Class pluginClass = newClassLoader.loadClass(plugin.getImplementation());
			Constructor constructor = pluginClass.getDeclaredConstructor(HostInterface.class);
			instance = (com.crawljax.core.plugin.Plugin) constructor.newInstance(hostInterface);
		} catch (Throwable e) {
			LOG.error("Could not create instance of plugin " + plugin.getName());
			LOG.debug("Could not create instance of plugin " + plugin.getName());
		} finally {
			//Thread.currentThread().setContextClassLoader(originalClassLoader); //Currently commented out so plugins can get their resources from the classloader
		}
		return instance;
	}

	private void copyFile(File source, File dest) {
		try (InputStream in = new FileInputStream(source)) {
			try(FileOutputStream out = new FileOutputStream(dest)) {
				ByteStreams.copy(in, out);
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not copy file " + source + " to " + dest);
		}
	}
}
