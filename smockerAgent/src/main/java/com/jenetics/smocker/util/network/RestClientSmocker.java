package com.jenetics.smocker.util.network;

import java.net.InetAddress;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.jenetics.smocker.configuration.SystemPropertyConfiguration;
import com.jenetics.smocker.util.MessageLogger;
import com.jenetics.smocker.util.SmockerContainer;
import com.jenetics.smocker.util.TransformerUtility;

public class RestClientSmocker extends RESTClient {

	private static final String CONNECTIONS = "connections";
	private static final String SMOCKER_REST_PATH = "/smocker/rest";
	private static final String SMOCKER_JAVAAPP_PATH = "/javaapplications";
	private static final String SMOCKER_ADDCONN = "/manageJavaApplication/addConnection";
	private static final String SMOCKER_ADDCOMM = "/manageJavaApplication/addCommunication";
	
	private static RestClientSmocker instance;
	
	public static synchronized RestClientSmocker getInstance() {
		if (instance == null) {
			instance = new RestClientSmocker();
		}
		return instance;
	}

	private RestClientSmocker() {
		super(SystemPropertyConfiguration.getTargetHost(), 
				SystemPropertyConfiguration.getTargetPort());
	}
	
	public String getAll() {
		setPath(SMOCKER_REST_PATH + SMOCKER_JAVAAPP_PATH);
		return get();
	}
	
	public String postConnection(SmockerContainer smockerContainer, Long javaAppId) {
		StringBuffer buffer = new StringBuffer();
		Map<String, String> headers = buildHeader();
		buffer.append("{\"id\": 0, \"version\": 0,  \"host\": \"")
			.append(smockerContainer.getHost())
			.append("\",  \"port\":")
			.append(smockerContainer.getPort())
			.append("}");
		setPath(SMOCKER_REST_PATH + SMOCKER_ADDCONN + "/" + javaAppId);
		try {
			return post(buffer.toString(), headers, PUT);
		} catch (Exception e) {
			MessageLogger.logThrowable(e);
		}
		return null;
	}
	
	public String postCommunication(SmockerContainer smockerContainer, Long javaAppId, Long connectionId) {
		StringBuffer buffer = new StringBuffer();
		Map<String, String> headers = buildHeader();
		if (
				smockerContainer.getSmockerSocketOutputStream() != null &&
				smockerContainer.getSmockerSocketOutputStream().getSmockerOutputStreamData() != null &&
				smockerContainer.getSmockerSocketInputStream() != null &&
				smockerContainer.getSmockerSocketInputStream().getSmockerOutputStreamData() != null
		)
		try {
			buffer.append("{\"id\": 0, \"request\":\"")
			.append(encode(smockerContainer.getSmockerSocketOutputStream().getSmockerOutputStreamData().getString()))
			.append("\",  \"response\":\"")
			.append(encode(smockerContainer.getSmockerSocketInputStream().getSmockerOutputStreamData().getString()))
			.append("\",  \"callerStack\":\"")
			.append(encode(smockerContainer.getStackTrace()))
			.append("\"}");
			setPath(SMOCKER_REST_PATH + SMOCKER_ADDCOMM + "/" + javaAppId + "/" + connectionId);
			return post(buffer.toString(), headers, PUT);
		} catch (Exception e) {
			MessageLogger.logThrowable(e);
		}
		return null;
	}
	

	private Object encode(String source) {
		return Base64.getEncoder().encodeToString(source.getBytes());
	}

	public String postJavaApp(SmockerContainer smockerContainer) {
		StringBuffer buffer = new StringBuffer();
		Map<String, String> headers = buildHeader();
		String host = null;
		String ip = null;
		try {
			host = InetAddress.getLocalHost().getHostName();
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {
			MessageLogger.logErrorWithMessage("Unable to get current host", e, getClass());
			return null;
		}
		
		buffer.append("{\"id\": 0, \"version\": 0,  \"classQualifiedName\": \"")
			.append(TransformerUtility.getCallerApp())
			.append("\",  \"sourceHost\":\"")
			.append(host)
			.append("\",  \"sourceIp\":\"")
			.append(ip)
			.append("\",  \"sourcePort\":\"")
			.append(SystemPropertyConfiguration.getCommPort())
			.append("\"}");
		setPath(SMOCKER_REST_PATH + SMOCKER_JAVAAPP_PATH);
		
		try {
			return post(buffer.toString(), headers, POST);
		} catch (Exception e) {
			MessageLogger.logErrorWithMessage("Unable to communicate with target host", e, getClass());
		}
		return null;
	}

	private Map<String, String> buildHeader() {
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Content-Type", "application/json");
		return headers;
	}


}
