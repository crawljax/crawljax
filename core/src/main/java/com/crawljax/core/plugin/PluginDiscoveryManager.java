package com.crawljax.core.plugin;

public interface PluginDiscoveryManager
{
	public <T> Iterable<T> getPluggedServices(Class<T> service);
}
