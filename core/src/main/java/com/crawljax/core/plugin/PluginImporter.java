package com.crawljax.core.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import com.crawljax.*;

public class PluginImporter
{
    static private final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
    static private final String MANIFEST_PLUGIN_ATTRIBUTE = "Plugins";
	public static final String PLUGIN_DIR = "C:/Users/User/Desktop/testplugins2";

    public PluginImporter(ClassLoader loader)
    {
        _loader = loader;
    }
  
    public static <T> List<T> getPluggedServices2(Class<T> clazz, ClassLoader loader)
    {
    	List<T> services = new ArrayList<T>();
        // we need a better way to figure out how to get files
        Enumeration<URL> manifests;
		try {
			manifests = loader.getResources(MANIFEST_PATH);
		
			while (manifests.hasMoreElements())
        	{
            	addOnePluginServices2(manifests.nextElement(), clazz, services, loader);
        	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return services;
    }

    private static void getClassNamesInJar (List<String> classNames, File jarfile)
    {
    	try{
    	 JarFile jarf = new JarFile(jarfile);
    	 Enumeration<? extends JarEntry> jarEntries = jarf.entries();
    	 
    
    	while (jarEntries.hasMoreElements())
    	{
    		JarEntry jarEntry = jarEntries.nextElement();
    		if(jarEntry.getName().endsWith(".class"))
    		{
    			String className = jarEntry.getName();
    			className = className.replace(".class", "").replace("/", ".");
    			classNames.add(className);
    		}
    	}
    	jarf.close();
    	} catch (IOException e)
        {
        	e.printStackTrace();
        	
        }	    	 	  	
    }
    
    public static <T> List<T> getPluggedServices(Class<T> clazz, File dir)
    {
    	List<T> services = new ArrayList<T>();	
        List<String> classNames = new ArrayList<String>();
        
        List<URL> jars = new ArrayList<URL>();
		ClassLoaderHelper.fillJarsList(jars,dir,true);
		
		ClassLoader cl = ClassLoaderHelper.buildClassLoader(true, dir);
		for(int iter = 0; iter < jars.size(); iter++)
			getClassNamesInJar(classNames, new File(jars.get(iter).getPath()));

		for(int iter = 0; iter < classNames.size(); iter++)
			addOneService(classNames.get(iter), clazz, services, cl);
		
        return services;
    }

    
    private static <T> void addOnePluginServices2(
            URL manifestUrl, Class<T> clazz, List<T> services, ClassLoader loader)
        {
            InputStream input = null;
            try {
    			input = manifestUrl.openStream();
    		
    			Manifest manifest = new Manifest(input);
            	String implementations =
                manifest.getMainAttributes().getValue(MANIFEST_PLUGIN_ATTRIBUTE);
            	if (implementations != null)
            	{
                	for (String impl: implementations.split("[ \t]+"))
                	{
                		addOneService2(impl, clazz, services, loader);
                	}
            	}
            } catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }
  

    private static <T> void addOneService(
        String implementation, Class<T> clazz, List<T> services, ClassLoader loader)
    {
    	try{
    		Class<?> service = Class.forName(implementation, false, loader);
    		if (clazz.isAssignableFrom(service))
    		{
    			services.add(clazz.cast(service.newInstance()));
    		}
        }
    	catch(ClassNotFoundException | IllegalAccessException | InstantiationException e )
    	{
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    }
    
    private static <T> void addOneService2(
            String implementation, Class<T> clazz, List<T> services, ClassLoader loader)
        {
        	try{
        		Class<?> service = Class.forName(implementation, false, loader);
        		if (clazz.isAssignableFrom(service))
        		{
        			services.add(clazz.cast(service.newInstance()));
        		}
            }
        	catch(ClassNotFoundException | IllegalAccessException | InstantiationException e )
        	{
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}
        }

    private final ClassLoader _loader;
}
