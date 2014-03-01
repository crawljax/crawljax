package com.crawljax.core.plugin.descriptor;

import com.crawljax.core.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PluginDescriptor {

	private static final Logger LOG = LoggerFactory.getLogger(PluginDescriptor.class);

	private String name;
	private String description;
	private List<String> crawljaxVersions = new ArrayList<>();
	private List<Parameter> parameters = new ArrayList<>();

	public static PluginDescriptor fromXMLStream(InputStream xmlInputStream) {
		com.crawljax.core.plugin.descriptor.jaxb.generated.PluginDescriptor pluginDescriptor = null;
		try {
			JAXBContext jc = JAXBContext.newInstance("com.crawljax.core.plugin.descriptor.jaxb.generated");
			Unmarshaller u = jc.createUnmarshaller();
			pluginDescriptor = (com.crawljax.core.plugin.descriptor.jaxb.generated.PluginDescriptor) u.unmarshal(xmlInputStream);
		} catch (JAXBException e) {
			LOG.info("Error reading plugin descriptor from xml stream");
			LOG.debug("Error reading plugin descriptor from xml stream");
		}
		return fromJaxbPluginDescriptor(pluginDescriptor);
	}

	public static PluginDescriptor forPlugin(Class<? extends Plugin> pluginClass) {
		PluginDescriptor pluginDescriptor = null;
		try(InputStream is = pluginClass.getResourceAsStream("/plugin-descriptor.xml")) {
			pluginDescriptor = fromXMLStream(is);
		} catch (IOException e) {
			LOG.info("Error loading descriptor for plugin {}", pluginClass);
			LOG.debug("Error loading descriptor for plugin {}.\n{}", pluginClass, e);
		}
		return pluginDescriptor;
	}

	private static PluginDescriptor fromJaxbPluginDescriptor(com.crawljax.core.plugin.descriptor.jaxb.generated.PluginDescriptor source) {
		PluginDescriptor plugin = new PluginDescriptor();
		plugin.name = source.getName();
		plugin.description = source.getDescription();
		for(String version : source.getCrawljaxVersions().getVersion()) {
			plugin.getCrawljaxVersions().add(version);
		}
		if(source.getParameters() != null) {
			for(com.crawljax.core.plugin.descriptor.jaxb.generated.Parameter parameter : source.getParameters().getParameter()) {
				Parameter copy = Parameter.fromJaxbParameter(parameter);
				plugin.getParameters().add(copy);
			}
		}
		return plugin;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<String> getCrawljaxVersions() {
		return crawljaxVersions;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}
}
