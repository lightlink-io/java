package io.lightlink.dao;

/*
 * #%L
 * lightlink-core
 * %%
 * Copyright (C) 2015 Vitaliy Shevchuk
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


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
