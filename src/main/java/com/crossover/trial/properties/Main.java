package com.crossover.trial.properties;

import java.net.URISyntaxException;

import org.json.simple.parser.ParseException;

import com.crossover.loader.exception.UnsupportedFileProtocolException;
import com.crossover.loader.service.PropertiesLoaderService;
import com.crossover.loader.service.impl.PropertiesLoaderServiceImpl;

public class Main {
	
	public static void main(String[] args) throws UnsupportedFileProtocolException, URISyntaxException, ParseException{
		PropertiesLoaderService pls = PropertiesLoaderServiceImpl.getInstance();
		
		try {
			pls.fetchPropertyValues(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
