package com.jenetics.smocker.model.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jenetics.smocker.model.CommunicationMocked;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.util.SmockerUtility;

public class Cloner {
	private static ObjectMapper mapper = new ObjectMapper();
	
	static {
		mapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
	}
	
	public static<T> T clone(T entity, Class<T> clazz) {
		StringWriter pw = new StringWriter();
		try {
			mapper.writeValue(pw, entity);
			return mapper.readValue(pw.toString(), clazz);
		} catch (IOException e) {
			SmockerUI.log(Level.SEVERE, SmockerUtility.getStackTrace(e));
		} 
		return null;
		
		
	}

}
