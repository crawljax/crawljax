package com.crawljax.plugins.testcasegenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawljax.browser.EmbeddedBrowser.BrowserType;
import com.crawljax.core.configuration.BrowserConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.CrawljaxConfiguration.CrawljaxConfigurationBuilder;
import com.crawljax.core.plugin.HostInterfaceImpl;
import com.crawljax.core.state.CrawlPath;
import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Eventable.EventType;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.core.state.StateVertex;
import com.crawljax.core.state.StateVertexImpl;
import com.crawljax.plugins.crawloverview.model.Edge;
import com.crawljax.plugins.crawloverview.model.OutPutModel;
import com.crawljax.plugins.crawloverview.model.Serializer;
import com.crawljax.plugins.testcasegenerator.TestConfiguration.StateEquivalenceAssertionMode;
import com.crawljax.plugins.testcasegenerator.crawlPlugins.AddressbookCleanup;
import com.crawljax.plugins.testcasegenerator.crawlPlugins.ClarolineCleanup;
import com.crawljax.plugins.testcasegenerator.util.GsonUtils;
import com.crawljax.util.DomUtils;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class OfflineTestGenerator {
	private static final Logger LOGGER =
	        LoggerFactory.getLogger(OfflineTestGenerator.class.getName());
	
	public static final String JSON_STATES = TestSuiteGenerator.TEST_SUITE_PATH + "states.json";
	public static final String JSON_EVENTABLES = TestSuiteGenerator.TEST_SUITE_PATH + "eventables.json";

	
//	public final static class ImmutableMapDeserializer implements JsonDeserializer<ImmutableMap<?,?>> {
//	    @Override
//	    public ImmutableMap<?,?> deserialize(final JsonElement json, final Type type,
//	                                       final JsonDeserializationContext context) throws JsonParseException
//	    {
//	        final Type type2 =
//	            ParameterizedTypeImpl.make(Map.class, ((ParameterizedType) type).getActualTypeArguments(), null);
//	        final Map<?,?> map = context.deserialize(json, type2);
//	        return ImmutableMap.copyOf(map);
//	    }
//
//		
//	}
	
	public static class ImmutableMapTypeAdapterFactory implements TypeAdapterFactory {

	    public static final ImmutableMapTypeAdapterFactory INSTANCE = new ImmutableMapTypeAdapterFactory();
	    
	    @Override
	    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
	        if (!ImmutableMap.class.isAssignableFrom(type.getRawType())) {
	            return null;
	        }
	        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
	        return new TypeAdapter<T>() {
	            @Override
	            public void write(JsonWriter out, T value) throws IOException {
	                delegate.write(out, value);
	            }

	            @Override
	            @SuppressWarnings("unchecked")
	            public T read(JsonReader in) throws IOException {
	                return (T) ImmutableMap.copyOf((Map) delegate.read(in));
	            }
	        };
	    }
	    
	    public static <K,V> InstanceCreator<Map<K, V>> newCreator() {
		    return new InstanceCreator<Map<K, V>>() {
		        @Override
		        public Map<K, V> createInstance(Type type) {
		            return new HashMap<K, V>();
		        }
		    };
		}
	}
	
	
	
	public static String generateTestCases(Collection<List<Eventable>> crawlPaths, OutPutModel outputModel, JsonObject configObject, String absPath) {
		// TODO: Provide pathInfoMap to get the backtracking info
		List<TestMethod> testMethods = TestSuiteGeneratorHelper.getTestMethods(crawlPaths);
		HostInterfaceImpl overviewHostInterface = null;
		if (overviewHostInterface == null) {
			overviewHostInterface =
			        new HostInterfaceImpl(new File(absPath), null);
		}

		HostInterfaceImpl testgenHostInterface = null;
		if (testgenHostInterface == null) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("testRecordsDir",
			        new File(absPath, "test-results")
			                .getAbsolutePath());
			testgenHostInterface =
			        new HostInterfaceImpl(new File(absPath), params);
			absPath =
			        testgenHostInterface.getOutputDirectory().getAbsolutePath() + File.separator;
		}
		
//		GsonBuilder builder = new GsonBuilder();
//		Gson gson = builder.create();
		String browserType = configObject.get("browserConfig").getAsJsonObject().get("browserType").getAsString();
		BrowserConfiguration browserConfig = null;
		switch(browserType) {	
			case "FIREFOX":
				browserConfig = new BrowserConfiguration(BrowserType.FIREFOX);
			default:
				browserConfig = new BrowserConfiguration(BrowserType.CHROME);
		}
		
//				gson.fromJson(, BrowserConfiguration.class);
		TestConfiguration testConfiguration = null;
		if (testConfiguration == null) {
			testConfiguration = new TestConfiguration(StateEquivalenceAssertionMode.BOTH,
			        browserConfig);
		}
		
		String initialURL = configObject.get("url").getAsString();
		
		CrawljaxConfigurationBuilder builder = CrawljaxConfiguration.builderFor(initialURL);
		JsonArray pluginArray = configObject.getAsJsonArray("plugins");
		
		for(JsonElement plugin: pluginArray) {
			switch(plugin.getAsString().toLowerCase()) {
			case "addressbookcleanup":
				builder.addPlugin(new AddressbookCleanup());
				break;
			case "clarolinecleanup":
				builder.addPlugin(new ClarolineCleanup());
				break;
			default:
				break;
			}
		}
		/*
		 * 
	"clickOnce" : false,
    "randomizeCandidateElements" : false,
    "crawlHiddenAnchors" : true,
    "waitAfterReloadUrl" : 1000,
    "waitAfterEvent" : 1000,
    "crawlPriorityMode" : "NORMAL",
    "crawlNearDuplicates" : true,
    "delayNearDuplicateCrawling" : true,
		 */
		
		/* crawling rules. */
		
//		builder.crawlRules().clickElementsInRandomOrder(false);
//		builder.crawlRules().crawlHiddenAnchors(true);
//		builder.crawlRules().crawlFrames(false);
//		builder.crawlRules().clickOnce(false);
//
//		builder.crawlRules().setFormFillMode(FormFillMode.RANDOM);
//
//		builder.crawlRules().dontClick("a").withText("php-addressbook");
//		builder.crawlRules().dontClick("a").withText("v9.0.0.1");

		/* set timeouts. */
//		int maximumStates = configObject.get("maximumStates").getAsInt();
//		int maximumDepth = configObject.get("maximumDepth").getAsInt();
//		int maximumRuntime = configObject.get("maximumRuntime").getAsInt();
//		
//		if(maximumDepth == 0)
//			builder.setUnlimitedCrawlDepth();
//		else
//			builder.setMaximumDepth(maximumDepth);
//		// builder.setMaximumRunTime(30, TimeUnit.MINUTES);
//		
//		if(maximumStates == 0)
//			builder.setUnlimitedStates();
//		else
//			builder.setMaximumStates(maximumStates);
//		
//		//builder.setMaximumStates(150);
//		//builder.setUnlimitedRuntime();
//		builder.setMaximumRunTime(120, TimeUnit.MINUTES);
		
		CrawljaxConfiguration config = builder.build();
		
		int WAIT_TIME_AFTER_RELOAD = configObject.get("crawlRules").getAsJsonObject().get("waitAfterReloadUrl").getAsInt();
		int WAIT_TIME_AFTER_EVENT = configObject.get("crawlRules").getAsJsonObject().get("waitAfterEvent").getAsInt();
		builder.crawlRules().waitAfterReloadUrl(WAIT_TIME_AFTER_RELOAD, TimeUnit.MILLISECONDS);
		builder.crawlRules().waitAfterEvent(WAIT_TIME_AFTER_EVENT, TimeUnit.MILLISECONDS);

		
		try {
			JavaTestGenerator generator =
			        new JavaTestGenerator(TestSuiteGenerator.CLASS_NAME, initialURL,
			                testMethods, config, //Config,
			                absPath ,
//			                absPath+ TestSuiteGenerator.TEST_SUITE_PATH,
//			                overviewHostInterface.getOutputDirectory().getAbsolutePath(),
//			                testgenHostInterface.getParameters().get("testRecordsDir"),
			                testConfiguration);
//			testSuiteGeneratorHelper.writeStateVertexTestDataToJSON(absPath + JSON_STATES);
//			testSuiteGeneratorHelper.writeEventableTestDataToJSON(absPath + JSON_EVENTABLES);
			generator.useJsonInsteadOfDB(absPath + JSON_STATES, absPath + JSON_EVENTABLES);
			String generatedFileName = generator.generate(DomUtils.addFolderSlashIfNeeded(absPath + TestSuiteGenerator.TEST_SUITE_PATH),
					TestSuiteGenerator.FILE_NAME_TEMPLATE);
			if (null != generatedFileName) {
				generator.copyExecutionScripts(absPath, TestSuiteGenerator.TEST_SUITE_SRC_FOLDER, TestSuiteGenerator.TEST_SUITE_PACKAGE_NAME, TestSuiteGenerator.CLASS_NAME);
			}
			LOGGER.info("Generated : " + generatedFileName);

		} catch (Exception e) {
			System.out.println("Error generating testsuite: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	

	public static void main(String args[]) {
//		String crawlLocation = "/Users/rahulkrishna/git/art_fork/art/crawljax/plugins/testcasegenerator-plugin/src/test/resources/petclinic_HYBRID_0.0_5mins/localhost/crawl0/";
		String crawlLocation = "/Users/rahulkrishna/git/art_fork/art/crawljax/plugins//testcasegenerator-plugin/src/test/resources/crawls/crawl1/"; 
//				"/Users/rahulkrishna/git/art_fork/art/crawljax/plugins/testcasegenerator-plugin/src/test/resources/crawls/addressbook_HYBRID_0.0_60mins/localhost/crawl0/";

		boolean crawlPathsAvailable = false;
		File crawlPathsJson = new File(crawlLocation, "CrawlPaths.json"); 
		File resultJson = new File(crawlLocation + File.separator + "result.json");
		File jsonEventables = new File(crawlLocation + File.separator + JSON_EVENTABLES);
		Map<Long, Eventable> mapEventables = null;

		if(Files.exists(jsonEventables.toPath(), LinkOption.NOFOLLOW_LINKS)) {
			try {

				Gson gson = (new GsonBuilder())
				        .registerTypeAdapter(ImmutableMap.class, new GsonUtils.ImmutableMapDeserializer())
				        .create();
				
				mapEventables = gson.fromJson(new BufferedReader(new FileReader(jsonEventables)),
				        new TypeToken<Map<Long, Eventable>>() {
				        }.getType());
				if(mapEventables == null) {
					LOGGER.error("Error parsing Eventables json : "+ jsonEventables.toPath());
					System.exit(-1);
				}
			} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
				LOGGER.error("Error parsing Eventables json : "+ jsonEventables.toPath());
				System.exit(-1);
			}
		}
		
		if(Files.notExists(resultJson.toPath(), LinkOption.NOFOLLOW_LINKS)) {
			LOGGER.error("Error finding result json : "+ resultJson.toPath());
			System.exit(-1);
		}
		
		File configJson = new File(crawlLocation + File.separator + "config.json");
		if(Files.notExists(configJson.toPath(), LinkOption.NOFOLLOW_LINKS)) {
			LOGGER.error("Error finding config : "+ configJson.toPath());
			System.exit(-1);
		}
		
		OutPutModel result = null;
		try {
			result = Serializer.read(resultJson);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			LOGGER.error("Error parsing result json : "+ resultJson.toPath());

			System.exit(-1);

		}
		
		String configString = null;
		try {
			configString = FileUtils.readFileToString(configJson, Charset.defaultCharset());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();		
			LOGGER.error("Error parsing config : "+ configJson.toPath());
			System.exit(-1);

		}
		
		JsonParser parser = new JsonParser();
		JsonObject configObject = parser.parse(configString).getAsJsonObject();
//		System.out.println(configObject);
		
		JsonArray pluginArray=	configObject.getAsJsonArray("plugins");
		
		boolean isHybridState = false;
		for(JsonElement element: pluginArray) {
			if(element.getAsString().equalsIgnoreCase("fragmentationPlugin")) {
				isHybridState = true;
			}
		}
		if(crawlPathsJson.exists()) {
			crawlPathsAvailable = true;
			LOGGER.info("CrawlPathsJson found");
			GsonBuilder builder= new GsonBuilder();
//			builder.registerTypeAdapter(ImmutableMap.class, new ImmutableMapDeserializer());
			builder.registerTypeAdapterFactory(new ImmutableMapTypeAdapterFactory());
			builder.registerTypeAdapter(ImmutableMap.class, ImmutableMapTypeAdapterFactory.newCreator());

			Gson gson = builder.create();
			try {
				Collection<List<Eventable>> crawlPaths = gson.fromJson(new JsonReader(new FileReader(crawlPathsJson)), new TypeToken<ArrayList<CrawlPath>>(){}.getType());
//				System.out.println(crawlPaths);
				boolean fixed = cleanCrawlPaths(crawlPaths, mapEventables);
				
				List<List<Eventable>> fixedCrawlPaths = fixCrawlPaths(crawlPaths, mapEventables, result);
				
//				if(fixed) {
				if(mapEventables!=null) {
					boolean replaced = replaceJsonEventables(mapEventables, jsonEventables);
					LOGGER.info("Replaced Eventables with fixed version {}", true);
				}
//				}
				
				boolean replaced = replaceCrawlPaths(fixedCrawlPaths, crawlPathsJson);
				LOGGER.info("Replaced CrawlPaths with fixed version {}", replaced);

				generateTestCases(fixedCrawlPaths, result, configObject, crawlLocation);
			} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			LOGGER.error("CrawlPaths Json not found at :" + crawlPathsJson.getAbsolutePath());
			System.exit(-1);
		}
	}


	private static boolean replaceCrawlPaths(Collection<List<Eventable>> crawlPaths, File crawlPathsJson) {
		File copy = new File(crawlPathsJson.getAbsolutePath()+"_original");
		try {
			Files.copy(crawlPathsJson.toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			LOGGER.error("Error copying eventables {} to {}", crawlPathsJson.getAbsolutePath(), copy.getAbsolutePath());
			return false;
		}
		try {
			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.setPrettyPrinting().create();

			FileWriter writer = new FileWriter(crawlPathsJson);
			gson.toJson(crawlPaths, writer);
			writer.flush();
			writer.close();
			LOGGER.info("Wrote crawlpaths to CrawlPaths.json");
			return true;
		}catch(Exception ex) {
			LOGGER.error("Error exporting Crawlpaths");
			return false;
		}
	}


	private static boolean replaceJsonEventables(Map<Long, Eventable> mapEventables, File jsonEventables) {
		File copy = new File(jsonEventables.getAbsolutePath()+"_original");
		try {
			Files.copy(jsonEventables.toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			LOGGER.error("Error copying eventables {} to {}", jsonEventables.getAbsolutePath(), copy.getAbsolutePath());
			return false;
		}
//		Set<Eventable> eventables = new HashSet<Eventable>();
//		for(Eventable eventable: mapEventables.values()) {
//			try{eventables.add(eventable);
//			}catch(Exception ex) {
//				ex.printStackTrace();
//			}
//		}
//		eventables.addAll(mapEventables.values());
		try {
			TestSuiteGeneratorHelper.writeEventableTestDataToJson(jsonEventables.getAbsolutePath(), mapEventables.values());
		} catch (IOException e) {
			LOGGER.error("Error writing fixed eventables to the fiel system at {}", jsonEventables.getAbsolutePath());
			return false;
		}
		return true;
	}


	private static boolean cleanCrawlPaths(Collection<List<Eventable>> crawlPaths, Map<Long, Eventable> mapEventables) {
		if(mapEventables==null) {
			return false;
		}
		else {
			boolean fixed = false;
//			long maxId = 0;
//			for(long id: mapEventables.keySet()) {
//				if(id> maxId) {
//					maxId = id;
//				}
//			}
			for(List<Eventable> crawlPath: crawlPaths) {
				for(Eventable eventable: crawlPath) {
					if(!mapEventables.containsKey(eventable.getId())) {
						fixed = true;
						LOGGER.info("Added eventable {} {}", eventable.getId(), eventable);
//						eventable.setSource(null);
//						eventable.setTarget(null);
						mapEventables.put(eventable.getId(), eventable);
					}
//					if(eventable.getId()<=0) {
//						fixed = true;
//						maxId+=1;
//						eventable.setId(maxId);
//						mapEventables.put(maxId, eventable);
//					}
				}
			}
			
			return fixed;
		}
	}
	
	/**
	 * get eventable that maps source to target
	 * @param source
	 * @param target
	 * @return
	 */
	private static List<Eventable> getEventable(int source, int target,  List<Eventable> allEventables) {
		
		List<Eventable> returnList = new ArrayList<Eventable>();
		for(Eventable eventable: allEventables) {
			
			int thisSource =  (int)Double.parseDouble(((LinkedTreeMap<?, ?>)eventable.getEdgeSource()).get("id").toString());
			int thisTarget = (int)Double.parseDouble(((LinkedTreeMap<?, ?>)eventable.getEdgeTarget()).get("id").toString());
			
			if(thisSource == source && thisTarget == target) {
				returnList.add(eventable);
				return returnList;
			}

		}
		return null;
		
	}
	
	private static Eventable getEventableFromGraph(String source, String target, Map<Long, Eventable> mapEventables, OutPutModel model) {
		Eventable found = null;
		for(Edge edge: model.getEdges()) {
			if(edge.getFrom().equalsIgnoreCase(source) && edge.getTo().equalsIgnoreCase(target)) {
				String how = edge.getId();
				String xpath = how.substring(6);
				String eventType = edge.getEventType();
				LOGGER.info("Using info from result {}, {}", xpath, eventType);
				found = new Eventable(new Identification(How.xpath, xpath), EventType.click);
			}
		}
		
		if(found!=null) {
			long maxEventable = 0;
			long mapped = -1;
			for(Eventable eventable: mapEventables.values()) {
				if(eventable.getId() > maxEventable) {
					maxEventable = eventable.getId();
				}
				if(found.getIdentification().getValue().equalsIgnoreCase(eventable.getIdentification().getValue())) {
					mapped = eventable.getId();
					found.setId(mapped);
				}
			}
			
			if(mapped == -1) {
				// No eventable for the xpath value. So add an eventable to the map 
				found.setId(maxEventable+1);
				mapEventables.put(maxEventable +1 , found);
			}
			
		}
		return found;
	}

	private static int getStateId(Object state) {
		if(state instanceof StateVertex)
			return ((StateVertex) state).getId();
		else
			return  (int)Double.parseDouble(((LinkedTreeMap<?, ?>)state).get("id").toString());
	}
	
	private static String getCrawlPathString(List<Eventable> crawlPath) {
		StringBuilder builder = new StringBuilder();
		builder.append(getStateId(crawlPath.get(0).getEdgeSource()));
		builder.append(",");
		for(Eventable eventable: crawlPath) {
			builder.append(getStateId(eventable.getEdgeTarget()));
			builder.append(",");
		}
		return builder.toString();
	}
	
	private static String getCrawlPathEventableString(List<Eventable> crawlPath) {
		StringBuilder builder = new StringBuilder();
		for(Eventable eventable: crawlPath) {
			builder.append(eventable.getId());
			builder.append(",");
		}
		return builder.toString();
	}
	
	
	private static List<Eventable> fixCrawlPath(List<Eventable> crawlPath, List<Eventable> allEventables, Map<Long, Eventable> mapEventables, OutPutModel model) {
		LOGGER.info("Trying to fix {}", getCrawlPathString(crawlPath));
		List<Eventable> returnPath = new ArrayList<Eventable>();
		Object sourceState = null;
		Object targetState = null;
		int source = -1;
		int target = -1;
		for(Eventable eventable: crawlPath) {

			if(eventable.getEdgeSource() instanceof StateVertex)
				source = eventable.getSourceStateVertex().getId();	
			else
				source =  (int)Double.parseDouble(((LinkedTreeMap<?, ?>)eventable.getEdgeSource()).get("id").toString());

			sourceState = eventable.getEdgeSource();

			if(target!=-1 && source != target) {
				List<Eventable> fix = getEventable(target, source, allEventables);
				if(fix!=null) {
					returnPath.addAll(fix);
					LOGGER.info("Found a fix for the path with eventable {}", fix.get(0).getId());
				}
				else {
					LOGGER.error("Could not fix the broken crawlPath using eventables in crawlpath. No eventable from  {} to {}", target, source);
					LOGGER.info("Trying to use result Json");
					String src = null;
					String tgt = null;
					if(sourceState instanceof StateVertex) 
						src = ((StateVertex) sourceState).getName();
					else
						src = ((LinkedTreeMap<?, ?>)eventable.getEdgeSource()).get("name").toString();
					
					if(targetState instanceof StateVertex)
						tgt = ((StateVertex) targetState).getName();
					else
						tgt =   ((LinkedTreeMap<?, ?>)targetState).get("name").toString();
					
					Eventable graphFix = getEventableFromGraph(tgt, src, mapEventables, model);
					if(graphFix!=null) {
						StateVertex srcVertex = new StateVertexImpl(target, "", tgt, "", "");
						StateVertex tgtVertex = new StateVertexImpl(source, "", src, "", "");
						graphFix.setSource(srcVertex);
						graphFix.setTarget(tgtVertex);
						returnPath.add(graphFix);
					}
				}
			}
			if(eventable.getEdgeTarget() instanceof StateVertex)
				target = eventable.getTargetStateVertex().getId();
			else
				target = (int)Double.parseDouble(((LinkedTreeMap<?, ?>)eventable.getEdgeTarget()).get("id").toString());
			
			targetState = eventable.getEdgeTarget();
//			targetState = (LinkedTreeMap<?, ?>)eventable.getEdgeTarget();
//			
			returnPath.add(eventable);
		}
		
		LOGGER.info("Fixed path {}", getCrawlPathString(returnPath));

		return returnPath;
	}
	

	
	private static List<List<Eventable>> fixCrawlPaths(Collection<List<Eventable>> crawlPaths,  Map<Long, Eventable> mapEventables, OutPutModel model){
		List<List<Eventable>> finalCrawlPaths = new ArrayList<List<Eventable>>();
		List<Eventable> allEventables = new ArrayList<Eventable>();
		for(List<Eventable> crawlPath: crawlPaths) {
			for(Eventable eventable: crawlPath) {
				if(!allEventables.contains(eventable))
					allEventables.add(eventable);
			}
		}
		
		List<String> stateSequence = new ArrayList<String>();
		int source = -1;
		int target = -1;
		for(List<Eventable> crawlPath: crawlPaths) {
			List<Eventable> fixedPath = null;
			for(Eventable eventable: crawlPath) {
				if(source == -1) {
					if(eventable.getEdgeSource() instanceof StateVertex)
						source = eventable.getSourceStateVertex().getId();	
					else
						source =  (int)Double.parseDouble(((LinkedTreeMap<?, ?>)eventable.getEdgeSource()).get("id").toString());
//					stateSequence.add(source);
				}
				if(eventable.getEdgeSource() instanceof StateVertex)
					source = eventable.getSourceStateVertex().getId();	
				else
					source =  (int)Double.parseDouble(((LinkedTreeMap<?, ?>)eventable.getEdgeSource()).get("id").toString());
				if(target!=-1 && source != target ) {
					LOGGER.info("Found a broken crawlPath {} != {}", source, target);
					fixedPath = fixCrawlPath(crawlPath, allEventables, mapEventables, model);
				}
				if(eventable.getEdgeTarget() instanceof StateVertex)
					target = eventable.getTargetStateVertex().getId();
				else
					target = (int)Double.parseDouble(((LinkedTreeMap<?, ?>)eventable.getEdgeTarget()).get("id").toString());

//				stateSequence.add(target);
			}
			if(fixedPath!=null) {
				finalCrawlPaths.add(fixedPath);
				stateSequence.add(getCrawlPathEventableString(fixedPath));
			}
			else {
				finalCrawlPaths.add(crawlPath);
				stateSequence.add(getCrawlPathEventableString(crawlPath));
			}
			source = -1;
			target = -1;
		}
		LOGGER.info("State sequence {}", stateSequence);
		return finalCrawlPaths;
	}

}
