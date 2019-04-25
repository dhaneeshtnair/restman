package com.restman.core;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Path("/transformer")
public class RestmanTransformer {
	
	 
	@POST
	@Path("transform")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String transform(@QueryParam("userName") String userName, @QueryParam("projectName") String projectName,
			@Context UriInfo ui,String body) {
		
		JsonObject jo = (JsonObject) new JsonParser().parse(body);
		
		 List<Object> specs = JsonUtils.jsonToList(jo.get("transformSpec").toString());//JsonUtils.classpathToList("/spec.json");
		
		 Chainr chainr = Chainr.fromSpec(specs);
		 
		 Object transformedOutput = chainr.transform(JsonUtils.jsonToObject(jo.get("input").toString()));
		 
		return JsonUtils.toPrettyJsonString(transformedOutput);
	}

	
	 
}
