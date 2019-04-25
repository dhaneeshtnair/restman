package com.restman.core.auth;

import com.restman.core.ConfigReader;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

/*
 * Dhaneesh TB
 */
public class SimpleLdapUserSerach {

	public static final String DISPLAYNAME = "displayname";
	public static final String DEPARTMENT_NUMBER = "departmentNumber";
	public static final String DISTINGUISHED_NAME = "distinguishedName";
	public static final String MAIL = "mail";
	public static final String MEMBER_OF = "memberOf";
	public static final String DIRECT_REPORTS = "directReports";
	public static final String INTERNAL_BASE = "OU=User Accounts,dc=ad,dc=here,dc=com";
	public static final String OPC_BASE = "OU=DirSyncExcluded,dc=ad,dc=here,dc=com";
	public static final String DG_BASE = "OU=Distribution Groups,OU=Groups,dc=ad,dc=here,dc=com";
	public static final String UG_BASE = "OU=User Groups,OU=Groups,dc=ad,dc=here,dc=com";
	public static final String MANAGER = "manager";
	public static final String SAM_ACCOUNT_NAME = "sAMAccountName";
	public static final String BASE_DN = "dc=ad,dc=here,dc=com";
	public static final String CN = "cn";

	private Hashtable<String, String> env = new Hashtable<>();

	private static SimpleLdapUserSerach ldapSearchHelper;

	private static String[] attributeFilter = { DISPLAYNAME, DEPARTMENT_NUMBER, DISTINGUISHED_NAME, MAIL, MEMBER_OF,
			DIRECT_REPORTS };

	public static SimpleLdapUserSerach getInstance() {
		if (ldapSearchHelper == null) {
			synchronized (LdapSearchHelper.class) {
				if (ldapSearchHelper == null) {
					ldapSearchHelper = new SimpleLdapUserSerach();
					ldapSearchHelper.initialiseEnvVariable();
				}
			}
		}

		return ldapSearchHelper;
	}

	//public static void main(String[] args) {
	//	System.out.println(getInstance().getUserDetails("dhatb"));
	//}

	private void initialiseEnvVariable() {
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.PROVIDER_URL, ConfigReader.getInstance().getProperty("ldap.url"));
		env.put("port",ConfigReader.getInstance().getProperty("ldap.port"));
		env.put(Context.SECURITY_PRINCIPAL, ConfigReader.getInstance().getProperty("ldap.cn"));
		env.put(Context.SECURITY_CREDENTIALS, ConfigReader.getInstance().getProperty("ldap.password"));
		env.put(Context.REFERRAL, "follow");
	}

	private SearchControls getSimpleSearchControls(String[] attributeFilter) {
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchControls.setTimeLimit(30000);
		if (attributeFilter.length > 0) {
			searchControls.setReturningAttributes(attributeFilter);
		}
		return searchControls;
	}

	private NamingEnumeration<SearchResult> getSearchResultNamingEnumeration(LdapContext ctx, String filter,
			SearchControls sc, UserInfo userInfo) throws NamingException {
		NamingEnumeration<SearchResult> results = null;
		results = ctx.search(INTERNAL_BASE, filter, sc);
		if (!results.hasMoreElements()) {
			results = ctx.search(OPC_BASE, filter, sc);
			if (results.hasMoreElements()) {
				userInfo.setIsOPC(true);
			}
		}
		return results;
	}
	private String getValueByAttributeName(Attributes attributes, String attributeName) throws NamingException {
        Attribute attr = attributes.get(attributeName);
        return (attr != null) ? (String) attr.get() : "";
    }
	private List<String> getValuesByAttributeName(Attributes attributes, String attributeName) throws NamingException {
        Attribute attribute = attributes.get(attributeName);
        if(attribute == null){
            return null;
        }
        List<String> userGroups = new ArrayList<>();
        NamingEnumeration allValues = attribute.getAll();
        StringBuffer value;
        while (allValues.hasMore()) {
            value = new StringBuffer((String) allValues.next());
            userGroups.add(value.substring(value.indexOf("=")+1, value.indexOf(",")).toLowerCase());
        }
        return userGroups;
    }
	public UserInfo getUserDetails(String userId) {
		LdapContext ctx = null;
		NamingEnumeration<?> namingEnum = null;
		String filter = String.format("(" + SAM_ACCOUNT_NAME + "=%s)", userId);

		try {
			UserInfo userInfo = new UserInfo();
			ctx = new InitialLdapContext(env, null);
			ctx.setRequestControls(null);
			namingEnum = getSearchResultNamingEnumeration(ctx, filter, getSimpleSearchControls(new String[] {}),
					userInfo);
			while (namingEnum.hasMore()) {
				SearchResult result = (SearchResult) namingEnum.next();
				Attributes attrs = result.getAttributes();
				userInfo.setDisplayName(getValueByAttributeName(attrs, DISPLAYNAME));
				userInfo.setEmail(getValueByAttributeName(attrs, MAIL));
				userInfo.setDepartmentNumber(getValueByAttributeName(attrs, DEPARTMENT_NUMBER));
				userInfo.setManager(getValueByAttributeName(attrs, MANAGER));
				userInfo.setsAMAccountName(getValueByAttributeName(attrs, SAM_ACCOUNT_NAME));
				userInfo.setUserGroups(getValuesByAttributeName(attrs, MEMBER_OF));
				userInfo.setDn(getValueByAttributeName(attrs, DISTINGUISHED_NAME));
			}
			return userInfo;
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (namingEnum != null) {
					namingEnum.close();
				}
				if (ctx != null) {
					ctx.close();
				}
			} catch (NamingException e) {
				e.printStackTrace();

			}
		}
		return null;
	}

}
