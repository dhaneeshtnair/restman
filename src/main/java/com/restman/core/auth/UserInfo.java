package com.restman.core.auth;

import java.util.List;

public class UserInfo {
    private boolean isOPC;
    private String dn;
    private String email;
    private String displayName;
    private String departmentNumber;
    private String sAMAccountName;
    private String manager;
    private List<String> userGroups;
    private List<String> directReportees;

    public UserInfo() {
    }

    public boolean isOPC() {
        return isOPC;
    }

    public void setIsOPC(boolean isOPC) {
        this.isOPC = isOPC;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDepartmentNumber() {
        return departmentNumber;
    }

    public void setDepartmentNumber(String departmentNumber) {
        this.departmentNumber = departmentNumber;
    }

    public List<String> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<String> userGroups) {
        this.userGroups = userGroups;
    }

    public List<String> getDirectReportees() {
        return directReportees;
    }

    public void setDirectReportees(List<String> directReportees) {
        this.directReportees = directReportees;
    }

    public String getsAMAccountName() {
        return sAMAccountName;
    }

    public void setsAMAccountName(String sAMAccountName) {
        this.sAMAccountName = sAMAccountName;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "isOPC=" + isOPC +
                ", dn='" + dn + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", departmentNumber='" + departmentNumber + '\'' +
                ", sAMAccountName='" + sAMAccountName + '\'' +
                ", manager='" + manager + '\'' +
                ", userGroups=" + userGroups +
                ", directReportees=" + directReportees +
                '}';
    }
}