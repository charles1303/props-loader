package com.crossover.loader.service;

import java.util.Map;

import com.crossover.loader.exception.PropertyNotFoundException;

public interface PropertiesLoaderService {
	
	Map<String,Object> fetchPropertyValues(String[] uri) throws Exception;
	
	Object getPropertyValue(String key) throws PropertyNotFoundException;

}
