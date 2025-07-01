package com.queue_it.connector.integrationconfig;

public class IntegrationConfigModel {

    public String Name;
    public String EventId;
    public String CookieDomain;
    public String LayoutName;
    public String Culture;
    public Boolean ExtendCookieValidity;
    public Integer CookieValidityMinute;
    public String QueueDomain;
    public String RedirectLogic;
    public String ForcedTargetUrl;
    public String ActionType;
    public TriggerModel[] Triggers;
    public Boolean IsCookieHttpOnly;
    public Boolean IsCookieSecure;
}
