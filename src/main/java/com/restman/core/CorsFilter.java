package com.restman.core;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

/***
 * 
 * @author dhatb
 *
 */
@WebFilter("/*")
public class CorsFilter implements Filter
{
	public void init(FilterConfig arg0) throws ServletException{}

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException
	{
		HttpServletResponse response = (HttpServletResponse) resp;
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", "X-HTTP-Method-Override, Content-Type, x-requested-with");

		response.setCharacterEncoding("utf-8");
		chain.doFilter(req, resp);
	}
	public void destroy()
	{

	}
}