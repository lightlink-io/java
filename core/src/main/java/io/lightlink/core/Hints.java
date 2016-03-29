package io.lightlink.core;

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


import javax.servlet.http.HttpServletRequest;

public class Hints {

    private boolean autoDetectDroppedClient;
    private int[] progressive;
    private boolean antiXSS;

    private Hints() {
    }


    public boolean isAutoDetectDroppedClient() {
        return autoDetectDroppedClient;
    }

    public int[] getProgressiveBlockSizes() {
        return progressive;
    }

    public static Hints fromRequest(HttpServletRequest servletRequest) {
        Hints hints = new Hints();
        String progressive = servletRequest.getHeader("lightlink-progressive");

        if (progressive != null && progressive.trim().length()>0) {
            if (progressive.equalsIgnoreCase("true")) {
                hints.progressive = new int[]{100, 1000};
            } else if (progressive.equalsIgnoreCase("false") ) {
                hints.progressive = null;
            } else {
                String[] values = progressive.split(",");
                hints.progressive = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    hints.progressive[i] = Integer.parseInt(values[i].trim());
                }
            }
        }

        String autoDetectDroppedClient = servletRequest.getHeader("lightlink-auto-detect-dropped-client");

        if (autoDetectDroppedClient != null && autoDetectDroppedClient.equalsIgnoreCase("false")) { // true by default
            hints.autoDetectDroppedClient = false;
        }


        String antiXSS = servletRequest.getHeader("lightlink-anti-xss");
        // false by default
        if (antiXSS != null && antiXSS.equalsIgnoreCase("false")) {
            hints.antiXSS = false;
        }

        return hints;
    }

    public boolean isAntiXSS() {
        return antiXSS;
    }

}
