package com.restman.core;

import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.restman.core.auth.AuthLogin;



public class App
{
	public static void main(String[] args)
	{
		
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.setResourceBase("/");
		
		int port =System.getProperty("server.port")!=null?Integer.parseInt(System.getProperty("server.port")):44444;
		System.setProperty("server.port",""+port);

		Server jettyServer = new Server(port);
		//jettyServer.setHandler(context);
		
		ResourceHandler resource_handler = new ResourceHandler();

        // Configure the ResourceHandler. Setting the resource base indicates where the files should be served out of.
        // In this example it is the current directory but it can be configured to anything that the jvm has access to.
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        //webAppContext.setResourceBase();

        try {
			resource_handler.setResourceBase(App.class.getClassLoader().getResource("www").toURI().toString());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        // Add the ResourceHandler to the server.
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler,context });
        jettyServer.setHandler(handlers);


		ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
		jerseyServlet.setInitOrder(0);

		jerseyServlet.setInitParameter("jersey.config.server.provider.classnames",RestmanTestCaseManager.class.getCanonicalName()+" "+ RestPy.class.getCanonicalName()+" "+CorsFilter.class.getCanonicalName()+" "+AuthLogin.class.getCanonicalName());

		FilterHolder holder = new FilterHolder(new CorsFilter());
		
		context.addFilter(holder, "/*", EnumSet.of(DispatcherType.REQUEST)); 
		try
		{
			jettyServer.start();
			jettyServer.join();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			jettyServer.destroy();
		}
		
		
//		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
//		context.setContextPath("/");
//		context.setResourceBase("/");
//
//		Server jettyServer = new Server(44444);
//		jettyServer.setHandler(context);
//
//		ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
//		jerseyServlet.setInitOrder(0);
//
//		// Tells the Jersey Servlet which REST service/class to load.
//		jerseyServlet.setInitParameter("jersey.config.server.provider.classnames",HerestTestCaseManager.class.getCanonicalName()+" "+ RestPy.class.getCanonicalName()+" "+CorsFilter.class.getCanonicalName()+" "+AuthLogin.class.getCanonicalName());
//
//		FilterHolder holder = new FilterHolder(new CorsFilter());
//		//holder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM,  "*");
//		//holder.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
//
//		context.addFilter(holder, "/*", EnumSet.of(DispatcherType.REQUEST)); 
//		try
//		{
//			jettyServer.start();
//			jettyServer.join();
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//		finally
//		{
//			jettyServer.destroy();
//		}
	}
}