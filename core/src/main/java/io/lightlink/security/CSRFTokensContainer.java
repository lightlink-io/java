package io.lightlink.security;

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


import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CSRFTokensContainer implements Serializable {

    public static final String CSRF_TOKEN_CONTAINER_KEY = "CSRF-Token-Container";
    public static final String CSRF_TOKEN_NAME = "CSRF_Token";
    private Set<String> tokens = Collections.synchronizedSet(new HashSet<String>());

    public synchronized static CSRFTokensContainer getInstance(HttpSession session) {
        CSRFTokensContainer tokenContainer = (CSRFTokensContainer) session.getAttribute(CSRF_TOKEN_CONTAINER_KEY);
        if (tokenContainer == null)
            session.setAttribute(CSRF_TOKEN_CONTAINER_KEY, tokenContainer = new CSRFTokensContainer());
        return tokenContainer;
    }

    public boolean isValid(String token) {
        return tokens.contains(token);
    }

    public static String getToken(Map<String, Object> inputParams) {
        return ""+ inputParams.get(CSRF_TOKEN_NAME);
    }

    public String createNewToken() {
        try {

            String token = "" + SecureRandom.getInstance("SHA1PRNG").nextLong();
            if (tokens.size() > 1000)
                tokens.clear(); // completely unreasonable number of tokens per session. Preventing OutOfMemory attack

            tokens.add(token);

            return token;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }


    public String validate(Map<String, Object> inputParams) {
        String token = getToken(inputParams);
        return isValid(token)?token:null;
    }

    public void sendCsrfError(HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.write("{\"success\":false,\"csrf_error\":true}");
        writer.close();
    }

}
