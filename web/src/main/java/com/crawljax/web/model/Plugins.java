package com.crawljax.web.model;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.crawljax.core.plugin.HostInterface;
import com.crawljax.web.exception.CrawljaxWebException;
import com.crawljax.web.fs.PluginManager;
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
		String id = fileName.substring(0, extensionIndex);
		id = id.toLowerCase().replaceAll("[^a-z0-9]+", "-");
		if (pluginList.containsKey(id)) {
			int i = 1;
			while (pluginList.containsKey(id + Integer.toString(i))) {
				i++;
			}
			id += Integer.toString(i);
		}
		Plugin plugin = pluginManager.save(id, data);
		if(plugin != null) {
			pluginList.put(plugin.getId(), plugin);
		}
		return plugin;
	}

	public Plugin remove(Plugin plugin) {
		if(pluginManager.delete(plugin)) {
			pluginList.remove(plugin.getId());
		}
		return plugin;
	}

	public Plugin findByID(final String id) {
		return pluginList.get(id);
	}

	public com.crawljax.core.plugin.Plugin getInstanceOf(Plugin plugin,
	        HostInterface hostInterface) {
		com.crawljax.core.plugin.Plugin instance = null;
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		ClassLoader newClassLoader = new URLClassLoader(new URL[]{plugin.getUrl()}, originalClassLoader);
		ClassLoader cl = new URLClassLoader(new URL[] { plugin.getUrl() });
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

}
