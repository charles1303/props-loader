package com.crossover.loader.service.impl;

import org.junit.Test;

import com.crossover.loader.service.PropertiesLoaderService;

public class PropertiesLoaderTest {

	@Test
	public void testLoader(){
		PropertiesLoaderService pls = PropertiesLoaderServiceImpl.getInstance();
		String[] uris = {"classpath:resources/jdbc.properties"};
		
		try {
			pls.fetchPropertyValues(uris);
						
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	
}
