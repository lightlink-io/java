package io.lightlink.dao;

import io.lightlink.config.ConfigManager;

public class LightLinkDAOFactory {

    private String packageName = ConfigManager.DEFAULT_ROOT_PACKAGE;

    public LightLinkDAOFactory() {
    }

    public LightLinkDAOFactory(String packageName) {
        this.packageName = packageName;
    }

    public LightLinkDAO getDAO(String action){
        return LightLinkDAO.getInstance(packageName, action);
    }

}
