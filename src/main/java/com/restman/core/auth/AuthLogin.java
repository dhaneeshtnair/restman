package com.restman.core.auth;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.http.client.ClientProtocolException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@SuppressWarnings("unused")
@Path("/auth")
public class AuthLogin {
	
	@Path("login")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String login(String body)
					throws URISyntaxException, ClientProtocolException, IOException {
		
		JsonObject jsonObject = (JsonObject) new JsonParser().parse(body);
		
		String userName = jsonObject.get("userName").getAsString();
		String password = jsonObject.get("password").getAsString();
		
		return new Gson().toJson(LdapSearchHelper.getInstance().authenticate(userName, password)).toString();
	}

}
