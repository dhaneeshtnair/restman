package com.restman.core;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;

public class Login {
	
	public static void main1(String[] args) {
		ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
		ExchangeCredentials credentials = new WebCredentials("dhaneesh.tb@here.com", "","here.com");
		service.setCredentials(credentials);
		
		try {
			service.autodiscoverUrl("dhaneesh.tb@here.com");
			//service.validate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
