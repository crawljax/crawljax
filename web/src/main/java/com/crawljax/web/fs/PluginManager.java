package com.crawljax.web.fs;

import com.crawljax.core.plugin.jaxb.generated.PluginDescriptor;
import com.crawljax.web.di.CrawljaxWebModule;
import com.crawljax.web.model.Parameter;
import com.crawljax.web.model.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipFile;

@Singleton
public class PluginManager {

	private static final Logger LOG = LoggerFactory.getLogger(PluginManager.class);

	private final File pluginsFolder;
	
	@Inject
	public PluginManager(@CrawljaxWebModule.PluginDescriptorFolder File pluginsFolder) {
		this.pluginsFolder = pluginsFolder;
		if (!this.pluginsFolder.exists())
			this.pluginsFolder.mkdirs();
	}

	public Map<String, Plugin> loadAll() {
		Map<String, Plugin> plugins = new ConcurrentHashMap<>();
		File[] pluginJars = pluginsFolder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});
		if (pluginJars != null) {
			for (File f : pluginJars) {
				Plugin p = load(f);
				if(p != null) {
					plugins.put(p.getId(), p);
				}
			}
		}
		return plugins;
	}

	private Plugin load(File jarFile) {
		Plugin plugin = null;
		try {
			PluginDescriptor pluginDescriptor = loadPluginDescriptorFromJar(jarFile);
			if(pluginDescriptor == null) {
				throw new Exception("Failed to load plugin descriptor");
			}
			plugin = fromJaxb(pluginDescriptor);
			plugin.setId(jarFile.getName().substring(0, jarFile.getName().indexOf(".jar")));
			plugin.setUrl(jarFile.toURI().toURL());
		} catch (Exception e) {
			LOG.error("Could not load plugin " + jarFile.getName() + ". \n" + e.getStackTrace());
		}
		return plugin;
	}

	/*private PluginDescriptor loadPluginDescriptor(File descriptorFile) {
		PluginDescriptor pluginDescriptor = null;
		try {
			JAXBContext jc = JAXBContext.newInstance("com.crawljax.plugin.jaxb");
			Unmarshaller u = jc.createUnmarshaller();
			pluginDescriptor = (PluginDescriptor) u.unmarshal(descriptorFile);
		} catch (JAXBException e) {
			LOG.error("Could not read plugin descriptor. " + e.getStackTrace());
		}
		return pluginDescriptor;
	}*/

	private Plugin fromJaxb(PluginDescriptor pluginDescriptor) {
		Plugin plugin = new Plugin();
		plugin.setName(pluginDescriptor.getName());
		plugin.setDescription(pluginDescriptor.getDescription());
		plugin.setImplementation(pluginDescriptor.getImplementation());
		plugin.setCrawljaxVersion(pluginDescriptor.getCrawljaxVersion());
		for(com.crawljax.core.plugin.jaxb.generated.Parameter parameter : pluginDescriptor.getParameters()) {
			Parameter copy = new Parameter();
			copy.setId(parameter.getId());
			copy.setDisplayName(parameter.getDisplayName());
			plugin.getParameters().add(copy);
		}
		return plugin;
	}

	private PluginDescriptor loadPluginDescriptorFromJar(File jarFile) {
		PluginDescriptor pluginDescriptor = null;
		try {
			ZipFile zipFile = new ZipFile(jarFile);
			InputStream is = zipFile.getInputStream(zipFile.getEntry("plugin-descriptor.xml"));
			JAXBContext jc = JAXBContext.newInstance("com.crawljax.core.plugin.jaxb.generated");
			Unmarshaller u = jc.createUnmarshaller();
			pluginDescriptor = (PluginDescriptor) u.unmarshal(is);
		} catch (JAXBException e) {
			LOG.error("Could not read plugin descriptor. " + e.getStackTrace());
		} catch (IOException e) {
			LOG.error("Could not load plugin descriptor. " + e.getStackTrace());
		}
		//HACK --need to fix xml layout
		ArrayList<com.crawljax.core.plugin.jaxb.generated.Parameter> toBeDeleted = new ArrayList<>();
		for(com.crawljax.core.plugin.jaxb.generated.Parameter param : pluginDescriptor.getParameters()) {
			if(param.getId() == null) {
				toBeDeleted.add(param);
			}
		}
		///HACK
		pluginDescriptor.getParameters().removeAll(toBeDeleted);
		return pluginDescriptor;
	}

	public void save(String id, byte[] data) {
		File pluginJar = new File(pluginsFolder, id + ".jar");
		try {
			if (!pluginJar.exists()) {
				pluginJar.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(pluginJar);
			fos.write(data);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			LOG.error("Could not save plugin file");
		}
	}

	public void delete(Plugin plugin) {
		File pluginJar = new File(pluginsFolder, plugin.getId() + ".jar");
		try {
			pluginJar.delete();
		} catch (Exception e) {
			LOG.error("Could not delete plugin {}", pluginJar);
		}
	}
}
