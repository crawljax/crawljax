package com.crawljax.web.fs;

import com.crawljax.core.plugin.descriptor.PluginDescriptor;
import com.crawljax.web.di.CrawljaxWebModule;
import com.crawljax.web.exception.CrawljaxWebException;
import com.crawljax.web.model.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Singleton
public class PluginManager {

	private static final Logger LOG = LoggerFactory.getLogger(PluginManager.class);

	private final File pluginsFolder;
	
	@Inject
	public PluginManager(@CrawljaxWebModule.PluginsFolder File pluginsFolder) {
		this.pluginsFolder = pluginsFolder;
		if (!this.pluginsFolder.exists())
			this.pluginsFolder.mkdirs();
	}

	public ConcurrentHashMap<String, Plugin> loadAll() {
		ConcurrentHashMap<String, Plugin> plugins = new ConcurrentHashMap<>();
		File[] pluginJars = pluginsFolder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});
		for (File f : pluginJars) {
			Plugin p = load(f);
			if(p != null) {
				plugins.put(p.getId(), p);
			}
		}
		return plugins;
	}

	private Plugin load(File jarFile) {
		Plugin plugin = null;
		try {
			PluginDescriptor descriptor = loadPluginDescriptorFromJar(jarFile);
			if(descriptor == null) {
				throw new Exception("Failed to load plugin descriptor");
			}
			plugin = new Plugin();
			plugin.setId(jarFile.getName().substring(0, jarFile.getName().indexOf(".jar")));
			plugin.setUrl(jarFile.toURI().toURL());
			plugin.setName(descriptor.getName());
			plugin.setDescription(descriptor.getDescription());
			plugin.setImplementation(descriptor.getImplementation());
			plugin.setParameters(descriptor.getParameters());
			plugin.setCrawljaxVersions(descriptor.getCrawljaxVersions());
		} catch (Exception e) {
			LOG.error("Could not load plugin {}", jarFile.getName());
			LOG.debug("Could not load plugin {}. \n{}", jarFile.getName(), e.getStackTrace());
		}
		return plugin;
	}

	private PluginDescriptor loadPluginDescriptorFromJar(File jarFile) {
		PluginDescriptor pluginDescriptor = null;
		try {
			try(ZipFile zipFile = new ZipFile(jarFile)) {
				ZipEntry descriptorEntry = zipFile.getEntry("plugin-descriptor.xml");
				if(descriptorEntry == null) {
					throw new Exception("Could not find plugin-descriptor.xml in root of " + jarFile.getName());
				}
				try(InputStream is = zipFile.getInputStream(zipFile.getEntry("plugin-descriptor.xml"))) {
					pluginDescriptor = PluginDescriptor.fromXMLStream(is);
				}
			}
		}  catch (Exception e) {
			LOG.error("Could not load plugin descriptor in {}. ", jarFile.getName());
			LOG.debug("Could not load plugin descriptor in {}. \n{}", jarFile.getName(), e.getStackTrace());
		}
		return pluginDescriptor;
	}

	public Plugin save(String id, byte[] data) throws CrawljaxWebException {
		File pluginJar = new File(pluginsFolder, id + ".jar");
		try {
			if (pluginJar.exists()) {
				pluginJar.delete();
			}
			pluginJar.createNewFile();
			try(FileOutputStream fos = new FileOutputStream(pluginJar)) {
				fos.write(data);
				fos.flush();
			}
		} catch (IOException e) {
			LOG.error("Could not save plugin file {}.", pluginJar.getName());
			LOG.debug("Could not save plugin file {}.\n{}", pluginJar.getName(), e.getStackTrace());
			throw new CrawljaxWebException("Could not save plugin file");
		}

		Plugin plugin = load(pluginJar);
		if(plugin == null) {
			pluginJar.delete();
			throw new CrawljaxWebException("Could not read plugin descriptor");
		}

		return plugin;
	}

	public boolean delete(Plugin plugin) {
		File pluginJar = new File(pluginsFolder, plugin.getId() + ".jar");
		return pluginJar.delete();
	}
}
