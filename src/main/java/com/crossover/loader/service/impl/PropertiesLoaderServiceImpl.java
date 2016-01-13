package com.crossover.loader.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.crossover.loader.exception.PropertyNotFoundException;
import com.crossover.loader.exception.UnsupportedFileExtensionException;
import com.crossover.loader.exception.UnsupportedFileProtocolException;
import com.crossover.loader.service.PropertiesLoaderService;
import com.crossover.loader.utils.PropertyLoader;

public final class PropertiesLoaderServiceImpl implements PropertiesLoaderService {
	
	public final static Logger logger = Logger.getLogger(PropertiesLoaderServiceImpl.class.getName());
	
	private static PropertiesLoaderServiceImpl propertiesLoader;
	
	private static TreeMap<String, Object> typeSafeProperties = new TreeMap<String, Object>();
	
	private PropertiesLoaderServiceImpl(){}
	
	public static synchronized PropertiesLoaderServiceImpl getInstance(){
		if(propertiesLoader == null)
			propertiesLoader = new PropertiesLoaderServiceImpl();
		
		return propertiesLoader;
	}
	
	public Map<String, Object> fetchPropertyValues(String[] uri)
			throws Exception {
		Properties props = null;
		if(PropertiesLoaderServiceImpl.typeSafeProperties.size() < 1){
			for(int i = 0; i < uri.length; i++){
				if(uri[i].contains(".properties")){
					props = loadPropertyFile(uri[i]);
				}
				else if(uri[i].contains(".json")){
					props = loadJsonFile(uri[i]);
				}else{
					throw new UnsupportedFileExtensionException("Unsupported File Extension");
				}
				
				setTypeSafeProperties(props);
			}
			
			
		}
		
		printTypeSafeProperties(PropertiesLoaderServiceImpl.typeSafeProperties);
		
		return PropertiesLoaderServiceImpl.typeSafeProperties;
	}

				
	private static void setTypeSafeProperties(Properties properties) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		Set<String> propkeys = new TreeSet(properties.keySet());
		
		for(String key : propkeys){
			
			if((String.valueOf(key)).equalsIgnoreCase("aws_region_id")){
				Region r = RegionUtils.getRegion(String.valueOf(properties.get(key)));
				PropertiesLoaderServiceImpl.typeSafeProperties.put(key.toLowerCase(), r);
				
			}else if((String.valueOf(properties.get(key))).equalsIgnoreCase("true") || (String.valueOf(properties.get(key))).equalsIgnoreCase("false")){
				
				PropertiesLoaderServiceImpl.typeSafeProperties.put(key.toLowerCase(), Boolean.valueOf(String.valueOf(properties.get(key))));
			}else if((String.valueOf(properties.get(key))).matches("[0-9]+")){
				PropertiesLoaderServiceImpl.typeSafeProperties.put(key.toLowerCase(), Integer.valueOf(String.valueOf(properties.get(key))));
			}else{
				PropertiesLoaderServiceImpl.typeSafeProperties.put(key.toLowerCase(), String.valueOf(properties.get(key)));
			}
			
		}
			
	}
	
	public void printTypeSafeProperties(TreeMap<String, Object> typeSafeProperties){
		Set<String> propkeys2 = new TreeSet<String>(typeSafeProperties.keySet());
		
		for(String key : propkeys2){
			System.out.println(key+ ", " + typeSafeProperties.get(key).getClass().getName() +", "+ typeSafeProperties.get(key) );
			
		}
		
	}
	
	

	@Override
	public Object getPropertyValue(String key) throws PropertyNotFoundException {
		// TODO Auto-generated method stub
		Object obj = PropertiesLoaderServiceImpl.typeSafeProperties.get(key);
		if(obj == null){
			throw new PropertyNotFoundException("Property was not set");
		}
		return obj;
	}
	
	private Properties loadPropertyFile(String uri) throws UnsupportedFileProtocolException{
		
		Properties currConfigProperties = new Properties();
		InputStream fileStream = null;
		Reader reader = null;
		try {
			if(uri.startsWith("classpath:")){
				String file = uri.replaceFirst("classpath:resources/", "");
				fileStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
				currConfigProperties.load(fileStream);
			}else if(uri.startsWith("file//")){
				File f = new File(uri.replaceFirst("file://", ""));
				fileStream = new FileInputStream(f);
				currConfigProperties.load(fileStream);
			}else if(uri.startsWith("http")){
				URL url = new URL(uri);
				InputStream in = url.openStream();
				reader = new InputStreamReader(in, "UTF-8");
				 currConfigProperties.load(reader);
				
			}else{
				throw new UnsupportedFileProtocolException("Unsupported File Protocol");
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}finally{
			if(fileStream != null){
				try {
					fileStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return currConfigProperties;
	}
	
	private Properties readJsonFile(File file) throws FileNotFoundException, IOException, ParseException{
		Properties currConfigProperties = new Properties();
		JSONObject json;
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader(file));
		json = (JSONObject)obj;
		
		Set<String> keys = json.keySet();
		for(String key : keys){
	        Object val = null;
	        try{
	             JSONObject value = (JSONObject) json.get(key);
	             
	        }catch(Exception e){
	            val = json.get(key);
	        }

	        if(val != null){
	        	currConfigProperties.put(key,val);
	        }
	    }
		return currConfigProperties;
	}
	
private Properties loadJsonFile(String uri) throws UnsupportedFileProtocolException, URISyntaxException, ParseException{
	
		Properties currConfigProperties = new Properties();
		InputStream fileStream = null;
		OutputStream outputStream = null;
		Reader reader = null;
		File file = null;
		
		try {
			if(uri.startsWith("classpath:")){
				fileStream = this.getClass().getResourceAsStream("/"+uri.replaceFirst("classpath:", ""));
				file = new File("props.json");
				outputStream = new FileOutputStream(file);
				IOUtils.copy(fileStream, outputStream);
				currConfigProperties = readJsonFile(file);				
			}else if(uri.startsWith("file://")){
				file = new File(uri.replaceFirst("file://", ""));
				currConfigProperties = readJsonFile(file);
			}else if(uri.startsWith("http")){
				URL url = new URL(uri);
				fileStream = url.openStream();
				file = new File("props.json");
				outputStream = new FileOutputStream(file);
				IOUtils.copy(fileStream, outputStream);
				currConfigProperties = readJsonFile(file);				
			}else{
				throw new UnsupportedFileProtocolException("Unsupported File Protocol");
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}finally{
			if(fileStream != null){
				try {
					fileStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(outputStream != null){
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		return currConfigProperties;
	}
}
