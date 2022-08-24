/*******************************************************************************
 * IBM Confidential
 * OCO Source Materials
 * (c) Copyright IBM Corp. 2011
 *
 * The source code for this program is not published or otherwise divested of its trade secrets, 
 * irrespective of what has been deposited with the U.S. Copyright Office.
 *******************************************************************************/

package com.ibm.tivoli.unity.util;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.tivoli.unity.common.logging.LoggerConstants;
import com.ibm.tivoli.unity.common.logging.UnityLogger4j2;
import com.ibm.tivoli.unity.handlers.UnityInitializer;

/**
 * @author akdixit1
 *
 */

public class AliasUtil {
	private static final Class<AliasUtil> thisClass = AliasUtil.class;
	private static  String DEFAULT_PROPS_DIR = "/wlp/usr/servers/Unity/apps/Unity.war/WEB-INF/";
	private static final String PROP_FILE = "aliasSource.json";
	private static String SCALA_HOME = "";
	private static UnityLogger4j2 logger = (UnityLogger4j2)UnityLogger4j2.getLogger(LoggerConstants.UNITY_APP_LOG_APPENDER);
	//private static JSONObject aliasJsonObject = null;
	private static HashMap<String,HashMap<String,HashMap<String,String>>> aliasHashMap = new HashMap<>();
	private static HashMap<String,HashMap<String,String>> aliasFieldMap  = new HashMap<>();
	private static ExecutorService fileChangerService = null;
	private static AliasUtil myInstance = null;
	private AliasUtil()
	{
		
	}
	public static AliasUtil getInstance()
	{
		if(myInstance == null)
		{
			myInstance = new AliasUtil();
			//Intialize the util
			try {
				myInstance.SCALA_HOME  = UnityInitializer.getScalaHomeDirectory();
				//test
				/*myInstance.SCALA_HOME  = "C:\\Users\\IBM_ADMIN\\Desktop\\stats\\alias\\";
				myInstance.DEFAULT_PROPS_DIR = "";*/
				
				// Added a null check since the AliasUtil class also gets initialized when the delete utility is run.
				// SCALA_HOME is not set in this path
				if (myInstance.SCALA_HOME != null) myInstance.initialize();
			} catch (IOException e) {
				logger.error(e);
			}
			
		}
		return myInstance;
	}
	private synchronized JSONObject readFile()
	{
		JSONObject ret = null;
		FileReader file1;
		FileReader file2;
		try {
			file1 = new FileReader(SCALA_HOME+DEFAULT_PROPS_DIR+PROP_FILE);
			if(file1.read() == -1){
				ret = JSONObject.parse("{\"aliasKeyValueArray\": []}");
				file1.close();
			}
			else{
				file2 = new FileReader(SCALA_HOME+DEFAULT_PROPS_DIR+PROP_FILE);
				ret = JSONObject.parse(file2);
				file2.close();
				file1.close();
			}
			//ret = JSONObject.parse(file);
			//file.close();
		} catch (IOException e) {
			logger.error(e);
		}
		return ret;
	}
	private void  parseToHashMap(JSONObject aliasJsonObj,HashMap<String,HashMap<String,HashMap<String,String>>> aliasDataMap,HashMap<String,HashMap<String,String>> aliasFieldMap)
	{
		if(aliasJsonObj == null) 
			return;
		 JSONArray aliasKeyValueArray = (JSONArray)aliasJsonObj.get("aliasKeyValueArray");
	        for (int i = 0; i < aliasKeyValueArray.size(); i++) {
	        	 JSONObject jsonobject = (JSONObject) aliasKeyValueArray.get(i);
	        	 	String sourceTye =(String) jsonobject.get("sourceType");
	        	 	if(sourceTye == null ||sourceTye.equals("")) {
	        	 		logger.warn(thisClass, "sourceType is either null or blank...");
	        	 		continue;
	        	 	}
	        	 	HashMap<String,HashMap<String,String>> fieldsMap = aliasDataMap.get(sourceTye);
	        	 	if(fieldsMap == null)
	        	 	{
	        	 		fieldsMap = new HashMap<String,HashMap<String,String>>();
	        	 		aliasDataMap.put(sourceTye, fieldsMap);
	        	 	}
	        	 	HashMap<String,String> sourcetypeMapping = aliasFieldMap.get(sourceTye);
	        	 	if(sourcetypeMapping == null){
	        	 		sourcetypeMapping = new HashMap<String,String>();
	        	 		aliasFieldMap.put(sourceTye, sourcetypeMapping);
	        	 	}
	                	JSONArray arrayFields = (JSONArray) jsonobject.get("fields");
	                	for (int j = 0; j < arrayFields.size(); j++){
	                		JSONObject fieldObj = (JSONObject) arrayFields.get(j);
	                		String fieldKey = (String) fieldObj.get("fieldName");
	                		if(fieldKey == null ||fieldKey.equals("")) {
	                			logger.warn(thisClass, "field is either null or blank...");
	                			continue;
	                		}
	                		String fieldKeyAlias = (String) fieldObj.get("alias_field");
	                		if(fieldKeyAlias == null || fieldKeyAlias.equals("")){
	                			logger.warn(thisClass, "alias for the field is either null or blank...");
	                			continue;
	                		}
	                		HashMap<String,String> valueMap = fieldsMap.get(fieldKey);
	                		if(valueMap == null)
	                		{
	                				valueMap = new HashMap<>();
	                				fieldsMap.put(fieldKey, valueMap);
	                				//aliasFieldMap.put(fieldKey, fieldKeyAlias);
	                		}
	                		String fieldAlias = sourcetypeMapping.get(fieldKey);
	                		if(fieldAlias == null){
	                			sourcetypeMapping.put(fieldKey, fieldKeyAlias);
	                		}
	                		JSONObject transObj = (JSONObject) fieldObj.get("translations");
	                		Iterator<String> itr = transObj.keySet().iterator();
	                		while(itr.hasNext())
	                		{
	                			String key = itr.next();
	                			if(key == null ||key.equals("")) continue;
	                			valueMap.put(key,(String)transObj.get(key));
	                		}
	                	}
	                }
	}
	private synchronized void setAliasHashMap(HashMap<String,HashMap<String,HashMap<String,String>>> hashMap,HashMap<String,HashMap<String,String>> aliasFieldMap)
	{
		this.aliasHashMap = hashMap;
		this.aliasFieldMap = aliasFieldMap;
	}
	private void initialize() throws IOException {
			JSONObject aliasJsonObject = readFile();
			parseToHashMap(aliasJsonObject,aliasHashMap,aliasFieldMap);
			if (fileChangerService == null) {
				fileChangerService = Executors.newSingleThreadExecutor();
				fileChangerService.execute(new Runnable() {
					WatchService watcher = FileSystems.getDefault()
							.newWatchService();

					@Override
					public void run() {
						Path dir = Paths.get(SCALA_HOME+DEFAULT_PROPS_DIR);
							WatchKey key = null;
							try {
								dir.register(watcher,
										StandardWatchEventKinds.ENTRY_MODIFY);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							
							while (true) {
								try {
									key = watcher.take();
									for (WatchEvent<?> event : key.pollEvents()) {
										String file = event.context().toString();
										//System.out.println("---->"+event.context().toString());
										if (file.equals( PROP_FILE)) {
											JSONObject obj = readFile();
											if(obj != null)
											{
												HashMap<String,HashMap<String,HashMap<String,String>>> aliasDataMap = new HashMap<>();
												HashMap<String,HashMap<String,String>> aliasFieldMap = new HashMap<>();
												parseToHashMap(obj,aliasDataMap,aliasFieldMap);
												setAliasHashMap(aliasDataMap,aliasFieldMap);
											}

										}
									}
									
								}
							 catch (InterruptedException e) {
								logger.error(e);
							}finally
							{
								key.reset();
							}
					}
					}
				});
			}
		}
	
		public synchronized ArrayList<String[]> getAllFieldsForSourceType(String sourceType)
		{
			logger.debug(thisClass, "inside getAllFieldsForSourceType....");
			ArrayList<String[]> fields = null;
		    HashMap<String,String> fieldsAlias = aliasFieldMap.get(sourceType);
		    if(fieldsAlias != null){
		    	fields = new ArrayList<>();
			    for (Map.Entry<String, String> aliasEntry: fieldsAlias.entrySet())
				{
				   String field = aliasEntry.getKey();
				   String fieldAlias = aliasEntry.getValue();
				   String[] data = new String[2];
				   data[0] = field;
				   data[1] = fieldAlias;
				   fields.add( data );
				}
		    }
		    return fields;
		}
		
		public synchronized String getValueForSourceTypeFieldValue(String sourceType, String field, String key)
		{
			logger.debug(thisClass, "inside getValueForSourceTypeFieldValue....");
			String value = null;
			 HashMap<String,HashMap<String,String>> filedMap = aliasHashMap.get(sourceType);
			 if(filedMap != null){
				 HashMap<String,String> valueMap = filedMap.get(field);
				 if(valueMap != null){
					 value = valueMap.get(key);
				 }
			 }
			return value;
		}
		
			/*public static void main(String[] args) {
				AliasUtil u =AliasUtil.getInstance();
				for(int j=0;j<100;j++){
					ArrayList<String[]> fields = u.getAllFieldsForSourceType("sourceType1");
					for(int i=0;i<fields.size();i++)
						System.out.println("name: "+fields.get(i)[0]+" alias: "+fields.get(i)[1]);
					System.out.println(u.getValueForSourceTypeFieldValue("sourceType2", "databaseName", "BLUECIRS"));
					System.out.println(u.getValueForSourceTypeFieldValue("sourceType1", "functionProductName", "WEBUBD"));
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}*/
}
