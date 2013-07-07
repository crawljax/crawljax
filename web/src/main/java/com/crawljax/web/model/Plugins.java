package com.crawljax.web.model;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.crawljax.core.plugin.HostInterface;
import com.crawljax.web.LogWebSocketServlet;
import com.crawljax.web.fs.PluginManager;

@Singleton
public class Plugins {
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

	public Plugin add(String fileName, byte[] data) {
		int extensionIndex = fileName.indexOf(".jar");
		if (extensionIndex < 0) {
			LogWebSocketServlet.sendToAll("Expected .jar file, got " + fileName);
			return null;
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
		ClassLoader cl = new URLClassLoader(new URL[] { plugin.getUrl() });
		try {
			Class pluginClass = cl.loadClass(plugin.getImplementation());
			Constructor constructor = pluginClass.getDeclaredConstructor(HostInterface.class);
			instance = (com.crawljax.core.plugin.Plugin) constructor.newInstance(hostInterface);
		} catch (Throwable e) {
			LogWebSocketServlet.sendToAll("Could not create instance of plugin "
			        + plugin.getName());
		}
		return instance;
	}

}
