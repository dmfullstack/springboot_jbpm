package com.yczx.bee.utils;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;

public class JbpmUtils {
	private static JbpmUtils utils = null;
	private KieServicesClient client = null;
	
	 private JbpmUtils() {
		KieServicesConfiguration configuration  = KieServicesFactory.newRestConfiguration("http://localhost:8080/kie-server/services/rest/server", "wbadmin", "wbadmin");
		configuration.setMarshallingFormat(MarshallingFormat.JAXB);
		client = KieServicesFactory.newKieServicesClient(configuration);
	}
	
	
	public synchronized static JbpmUtils getInstance() {
		if(utils==null) {
			utils= new JbpmUtils();
		}
		return utils;
	}


	public KieServicesClient getClient() {
		return client;
	}

}
