package com.restman.core.auth;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.restman.core.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.*;
import java.util.*;

public class LdapSearchHelper {
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
    private static Logger logger = LoggerFactory.getLogger(LdapSearchHelper.class);

    private Hashtable<String, String> env = new Hashtable<>();

    private static LdapSearchHelper ldapSearchHelper;

    private static String[] attributeFilter = {
            DISPLAYNAME,
            DEPARTMENT_NUMBER,
            DISTINGUISHED_NAME,
            MAIL,
            MEMBER_OF,
            DIRECT_REPORTS
    };

    public static LdapSearchHelper getInstance() {
        if (ldapSearchHelper == null) {
            synchronized (LdapSearchHelper.class) {
                if (ldapSearchHelper == null) {
                    ldapSearchHelper = new LdapSearchHelper();
                    ldapSearchHelper.initialiseEnvVariable();
                }
            }
        }

        return ldapSearchHelper;
    }
//    public static void main(String[] args) {
//		System.out.println(getInstance().getUserDetails("dhatb"));
//	}

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
        if(attributeFilter.length > 0){
            searchControls.setReturningAttributes(attributeFilter);
        }
        return searchControls;
    }

    

    /**
     * Returns {@link boolean} with addition information in case of user exists and authicated correctly, otherwise null
     *
     * @param username username
     * @param password password
     * @return Item or null
     */
    public UserInfo authenticate(String username, String password) {
        LdapContext ctx = null;
        String filter = "("+ SAM_ACCOUNT_NAME +"=" + username + ")";
        NamingEnumeration<SearchResult> results = null;
        try {
            ctx = new InitialLdapContext(env, null);
            SearchControls sc = getSimpleSearchControls(attributeFilter);
            UserInfo userInfo = new UserInfo();
            results = getSearchResultNamingEnumeration(ctx, filter, sc, userInfo);

            if (results.hasMoreElements()) {
                Attributes attributes = results.next().getAttributes();
                String dn = getValueByAttributeName(attributes, DISTINGUISHED_NAME);
                verifyUser(password, dn);
                userInfo.setEmail(getValueByAttributeName(attributes, MAIL));
                userInfo.setDisplayName(getValueByAttributeName(attributes, DISPLAYNAME));
                userInfo.setDepartmentNumber(getValueByAttributeName(attributes, DEPARTMENT_NUMBER));
                //userInfo.setUserGroups(getValuesByAttributeName(attributes, MEMBER_OF));
                userInfo.setDirectReportees(getValuesByAttributeName(attributes, DIRECT_REPORTS));
                return userInfo;
            } else {
                return null;
            }
        } catch (NamingException nex){
            logger.error("Authentication Failed for User: " + username, nex);
            return null;
        } catch (Exception e) {
            logger.error("User Authentication Failed for user: " + username, e);
            return null;
        } finally {

            try {
                if (ctx != null) {
                    ctx.close();
                }
                if (results != null) {
                    results.close();
                }
            } catch (NamingException e) {
                e.printStackTrace();
                logger.info("Error while authenticate user ");
            }
        }
    }

    private NamingEnumeration<SearchResult> getSearchResultNamingEnumeration(LdapContext ctx, String filter, SearchControls sc,
                                                                             UserInfo userInfo) throws NamingException {
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

    private void verifyUser(String password, String dn) throws NamingException {
        Hashtable<String, String> env1 = new Hashtable<>();
        env1.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env1.put(Context.PROVIDER_URL, "ldap://ad.here.com");
        env1.put(Context.SECURITY_PRINCIPAL, dn);
        env1.put(Context.SECURITY_CREDENTIALS, password);
        new InitialLdapContext(env1, null); // in case of problem exception will be thrown
        logger.info("checking with ldap - ok");
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
        String filter = String.format("("+ SAM_ACCOUNT_NAME +"=%s)", userId);

        try {
            UserInfo userInfo = new UserInfo();
            ctx = new InitialLdapContext(env, null);
            ctx.setRequestControls(null);
            namingEnum = getSearchResultNamingEnumeration(ctx, filter, getSimpleSearchControls(new String[]{}), userInfo);
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
            logger.error("Error fetching matching User Details from ldap for user: " + userId + " Exception: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error fetching matching User Details from ldap for user: " + userId + " Exception: " + e.getMessage(), e);
        } finally {
            try {
                if (namingEnum != null) {
                    namingEnum.close();
                }
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException e) {
                logger.error("Error fetching matching User Details from ldap for user: " + userId + " Exception: " + e.getMessage(), e);

            }
        }
        return null;
    }

    public List<String> getUserGroups(String userId) {
        UserInfo userDetails = getUserDetails(userId);
        return userDetails == null ? null : userDetails.getUserGroups();
    }

    public String getUserEmail(String userId) {
        UserInfo userDetails = getUserDetails(userId);
        return userDetails == null ? null : userDetails.getEmail();
    }

    public JsonArray searchMatchingGroups(String groupName) {
        LdapContext ctx = null;
        NamingEnumeration<?> namingEnum = null;
        JsonArray out = new JsonArray();
        JsonObject jsonObject = new JsonObject();
        String filter = String.format("(cn=%s*)", groupName);
        try {
            ctx = new InitialLdapContext(env, null);
            ctx.setRequestControls(null);
            namingEnum = ctx.search("ou=groups,dc=ad,dc=here,dc=com", filter, getSimpleSearchControls(new String[]{}));
            while (namingEnum.hasMore()) {
                SearchResult result = (SearchResult) namingEnum.next();
                Attributes attrs = result.getAttributes();
                jsonObject.addProperty("id", getValueByAttributeName(attrs, CN));
                out.add(jsonObject);
                jsonObject = new JsonObject();
            }
        } catch (NamingException e) {
            logger.error("Error fetching matching groups from ldap for group: " + groupName + " Exception: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error fetching matching groups from ldap for group: " + groupName + " Exception: " + e.getMessage(), e);
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
                logger.error("Error fetching matching groups from ldap for group: " + groupName + " Exception: " + e.getMessage(), e);

            }
        }

        return out;
    }


    public JsonArray searchMatchingUserNames(String username) {
        LdapContext ctx = null;
        NamingEnumeration<?> namingEnum = null;
        JsonArray out = new JsonArray();
        try {
            String query = String.format("(sAMAccountName=%s*)", new Object[]{username});
            ctx = new InitialLdapContext(env, null);
            ctx.setRequestControls(null);
            namingEnum = ctx.search("dc=ad,dc=here,dc=com", query, getSimpleSearchControls(new String[]{}));
            JsonObject jsonObject = new JsonObject();
            while (namingEnum.hasMore()) {
                SearchResult result = (SearchResult) namingEnum.next();
                Attributes attrs = result.getAttributes();
                jsonObject.addProperty("displayname", getValueByAttributeName(attrs, DISPLAYNAME));
                jsonObject.addProperty("sAMAccountName", getValueByAttributeName(attrs, SAM_ACCOUNT_NAME));
                out.add(jsonObject);
                jsonObject = new JsonObject();

            }
        } catch (NamingException e) {
            logger.error("Error fetching matching User Names from ldap for user: " + username + " Exception: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error fetching matching User Names from ldap for user: " + username + " Exception: " + e.getMessage(), e);
        } finally {
            try {
                if (namingEnum != null) {
                    namingEnum.close();
                }
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException e) {
                logger.error("Error fetching matching User Names from ldap for user: " + username + " Exception: " + e.getMessage(), e);
            }
        }
        return out;
    }

    public Map<String, String> getAllUsersInDepartment(String deptNo) {
        Map<String, String> userMap = getUsersByBaseInDepartment(deptNo, true);
        logger.info(userMap.size() + " users found in the OPC Base in department " + deptNo);
        if(userMap.isEmpty())
        {
            userMap = getUsersByBaseInDepartment(deptNo, false);
            logger.info(userMap.size() + " users found in the Internal Base in department " + deptNo);
        }

        return userMap;
    }

    private Map<String, String> getUsersByBaseInDepartment(String deptNo, boolean isOpc) {
        LdapContext ctx = null;
        NamingEnumeration<?> namingEnum = null;
        Map<String, String> userMap = new HashMap<>();

        /* Activate paged results */
        int pageSize = 500;
        byte[] cookie = null;

        try {
            String query = String.format("(departmentNumber=%s)", new Object[]{deptNo});
            ctx = new InitialLdapContext(env, null);
            ctx.setRequestControls(new Control[]{new PagedResultsControl(pageSize, Control.CRITICAL)});

            while(true)
            {
                if(isOpc)
                {
                    namingEnum = ctx.search(OPC_BASE, query, getSimpleSearchControls(new String[]{}));
                }
                else
                {
                    namingEnum = ctx.search(INTERNAL_BASE, query, getSimpleSearchControls(new String[]{}));
                }

                while (namingEnum != null && namingEnum.hasMore()) {
                    SearchResult result = (SearchResult) namingEnum.next();
                    Attributes attrs = result.getAttributes();
                    String dn = getValueByAttributeName(attrs, DISTINGUISHED_NAME);
                    if (!dn.contains("Disabled Users")) {
                        userMap.put(getValueByAttributeName(attrs, SAM_ACCOUNT_NAME), getValueByAttributeName(attrs, DISPLAYNAME));
                    }
                }
                // Examine the paged results control response
                Control[] controls = ctx.getResponseControls();
                if(controls!=null){
                    for(int k = 0; k<controls.length; k++){
                        if(controls[k] instanceof PagedResultsResponseControl){
                            PagedResultsResponseControl prrc = (PagedResultsResponseControl)controls[k];
                            cookie = prrc.getCookie();
                        }
                    }
                }

                if(cookie==null)
                    break;

                // Re-activate paged results
                ctx.setRequestControls(new Control[]{
                        new PagedResultsControl(pageSize, cookie, Control.CRITICAL)});
            }

        } catch (Exception e) {
            logger.error("Error fetching users from ldap for department: " + deptNo + " Exception: " + e.getMessage(), e);
        } finally {
            try {
                if (namingEnum != null) {
                    namingEnum.close();
                }
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException e) {
                logger.error("Error fetching users from ldap for department: " + deptNo + " Exception: " + e.getMessage(), e);
            }

        }
        return userMap;
    }

    public Set<String> getUsersInGroup(String baseDn, String memberOfQuery, boolean includeChildGroup) {
        LdapContext ctx = null;
        NamingEnumeration<?> namingEnum = null;
        Set<String> userSet = new HashSet<>();

        /* Activate paged results */
        int pageSize = 500;
        byte[] cookie = null;

        try {
            String query;

            ctx = new InitialLdapContext(env, null);
            ctx.setRequestControls(new Control[]{new PagedResultsControl(pageSize, Control.CRITICAL)});


                while (true) {
                    namingEnum = ctx.search(baseDn, memberOfQuery, getSimpleSearchControls(new String[]{}));

                    while (namingEnum != null && namingEnum.hasMore()) {
                        SearchResult result = (SearchResult) namingEnum.next();
                        Attributes attrs = result.getAttributes();
                        String dn = getValueByAttributeName(attrs, DISTINGUISHED_NAME);
                        if (!dn.contains("Disabled Users")) {
                            userSet.add(getValueByAttributeName(attrs, SAM_ACCOUNT_NAME));
                        }

                        if(includeChildGroup && dn.contains("Groups"))
                        {
                            userSet.addAll(getUsersInGroup(getValueByAttributeName(attrs, CN)));
                        }
                    }
                    // Examine the paged results control response
                    Control[] controls = ctx.getResponseControls();
                    if (controls != null) {
                        for (int k = 0; k < controls.length; k++) {
                            if (controls[k] instanceof PagedResultsResponseControl) {
                                PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[k];
                                cookie = prrc.getCookie();
                            }
                        }
                    }

                    if (cookie == null)
                        break;

                    // Re-activate paged results
                    ctx.setRequestControls(new Control[]{
                            new PagedResultsControl(pageSize, cookie, Control.CRITICAL)});
                }

        } catch (Exception e) {
            logger.error("Error fetching users from ldap for group: " + memberOfQuery + " Exception: " +
                    e.getMessage(), e);
        } finally {
            try {
                if (namingEnum != null) {
                    namingEnum.close();
                }
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException e) {
                logger.error("Error fetching users from ldap for group: " + memberOfQuery + " Exception: " +
                        e.getMessage(), e);
            }

        }
        return userSet;
    }


    public Set<String> getUsersInGroup(String groupName) {
        LdapContext ctx = null;
        NamingEnumeration<?> namingEnum = null;
        NamingEnumeration<?> userBaseEnum = null;
        Set<String> userSet = new HashSet<>();

        /* Activate paged results */
        int pageSize = 500;
        byte[] cookie = null;

        try {
            String query;
            String userQuery;

            ctx = new InitialLdapContext(env, null);
            ctx.setRequestControls(null);

            //Try with distribution group dn
            namingEnum = ctx.search(DG_BASE, "CN=" + groupName, getSimpleSearchControls(new String[]{}));

            if(namingEnum == null || !namingEnum.hasMore()) {
                namingEnum = ctx.search(UG_BASE, "CN=" + groupName, getSimpleSearchControls(new String[]{}));

                if(namingEnum == null || !namingEnum.hasMore())
                {
                    return userSet;
                }
                userQuery = String.format("memberOf=CN=%s," + UG_BASE, new Object[]{groupName});
            }
            else
            {
                userQuery = String.format("memberOf=CN=%s," + DG_BASE, new Object[]{groupName});
            }

            String[] dnList = {INTERNAL_BASE, OPC_BASE};


            for(String dn:dnList)
            {
                userSet.addAll(getUsersInGroup(dn, userQuery, false));
            }

        } catch (Exception e) {
            logger.error("Error fetching users from ldap for group: " + groupName + " Exception: " +
                    e.getMessage(), e);
        } finally {
            try {
                if (namingEnum != null) {
                    namingEnum.close();
                }
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException e) {
                logger.error("Error fetching users from ldap for group: " + groupName + " Exception: " +
                        e.getMessage(), e);
            }

        }
        return userSet;
    }



    public String getTenantIdForOpc(String userId)
    {
        UserInfo userInfo = getUserDetails(userId);
        String treatUserAsOpc ="";// PropertyUtil.getInstance().getProperty("treat.user.as.opc");
        if(treatUserAsOpc != null && !treatUserAsOpc.isEmpty())
        {
            return treatUserAsOpc;
        }

        if(userInfo.isOPC())
        {
            return userInfo.getDepartmentNumber();
        }
        return "";
    }


    /**
     * For a given set of user ids, get the corresponding department number only if the user belongs to OPC department.
     * If user is not an OPC user, no entry would be present for the user in the returned map.
     *
     * @param userSet
     * @return
     */
    public Map<String, String> getOPCUsersVsDepartment(Set<String> userSet)
    {
        LdapContext ctx = null;
        NamingEnumeration<?> namingEnum = null;
        Map<String, String> userVsOpcDept = new HashMap<>();


        try {
            String query;
            ctx = new InitialLdapContext(env, null);
            ctx.setRequestControls(null);

            for(String userId:userSet) {
                String filter = String.format("(" + SAM_ACCOUNT_NAME + "=%s)", userId);

                namingEnum = ctx.search(OPC_BASE, filter, getSimpleSearchControls(new String[]{}));

                if (namingEnum != null && namingEnum.hasMore()) {
                    SearchResult result = (SearchResult) namingEnum.next();
                    Attributes attrs = result.getAttributes();
                    String dn = getValueByAttributeName(attrs, DISTINGUISHED_NAME);
                    if (!dn.contains("Disabled Users")) {
                        userVsOpcDept.put(getValueByAttributeName(attrs, SAM_ACCOUNT_NAME),
                                getValueByAttributeName(attrs, DEPARTMENT_NUMBER));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching departments for the users from ldap Exception: " +
                    e.getMessage(), e);
        } finally {
            try {
                if (namingEnum != null) {
                    namingEnum.close();
                }
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException e) {
                logger.error("Error fetching departments for the users from ldap Exception: " +
                        e.getMessage(), e);
            }

        }

        return userVsOpcDept;
    }
}