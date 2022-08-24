/*********************************************************** {COPYRIGHT-TOP} ***
 * IBM Confidential
 * OCO Source Materials
 * (C) Copyright IBM Corp. 2013. All Rights Reserved.
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 ************************************************************ {COPYRIGHT-END} **/

package com.ibm.tivoli.unity.util;

import java.security.SecureRandom;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.core.NewCookie;

import javax.xml.bind.DatatypeConverter;


public class CSRFUtil {
    /** The name of the CSRF token cookie. */
    public static final String CSRF_TOKEN_COOKIE_NAME = "CSRFToken";
    
    /** The name of the CSRF token parameter. */
    public static final String CSRF_TOKEN_PARAMETER_NAME = "CSRFToken";

    /** The path for the CSRF token cookie. */
    private static final String CSRF_TOKEN_COOKIE_PATH = "/Unity";
    
    /** The max age of the CSRF token cookie. */
    private static final int CSRF_TOKEN_COOKIE_MAX_AGE = -1;
    
    /** The default Cookie version     */
    private static final int DEFAULT_CSRF_COOKIE_VERSION=0;
    public static final Pattern CSRF_TOKEN_PATTERN = Pattern.compile("[A-F0-9]{32}");
    
    
    /**
     * Generates and adds a new CSRF token as a cookie to the specified response object if it does not already
     * appear in the specified request. 
     * 
     * @param request The request object to check for a pre-existing CSRF token.
     * @param response The response object to add the token to.
     * @return The CSRF token that was added to the response, or the pre-existing CSRF token
     */
    public static final String addCSRFTokenToResponseIfNecessary(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // If we find an existing CSRFToken cookie and it's the right format, return it
                if (CSRF_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    String value = cookie.getValue();
                    if (CSRF_TOKEN_PATTERN.matcher(value).matches()) {
                        return value;
                    }
                    
                    break;
                }
            }
        }
        
        String token = generateCSRFToken();
        
        Cookie cookie = new Cookie(CSRF_TOKEN_COOKIE_NAME, token);
        cookie.setPath(CSRF_TOKEN_COOKIE_PATH);
        cookie.setMaxAge(CSRF_TOKEN_COOKIE_MAX_AGE);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
        
        return token;
    }

    
    /**
     * Returns a cookie suitable for use with Http responses. 
     */
    public static Cookie generateCSRFTokenCookie() {
    	/**
    	 * commented below code as it needs jaxrs jar upgrade which giving conflicts during API access
    	 * Instead we remove the REST API reference and created normal servlet to serve the same functionality using regular cookie API
    	 */ 
        //return new NewCookie(CSRF_TOKEN_COOKIE_NAME, generateCSRFToken(), CSRF_TOKEN_COOKIE_PATH, null, 0, null, CSRF_TOKEN_COOKIE_MAX_AGE, true);
    	//return new NewCookie(CSRF_TOKEN_COOKIE_NAME, generateCSRFToken(), CSRF_TOKEN_COOKIE_PATH, null, 0, null, CSRF_TOKEN_COOKIE_MAX_AGE, null, true, true);
    	
    	String token = CSRFUtil.generateCSRFToken();
        Cookie cookie =  new Cookie(CSRFUtil.CSRF_TOKEN_COOKIE_NAME, token);;
        cookie.setPath(CSRF_TOKEN_COOKIE_PATH);
        cookie.setMaxAge(CSRF_TOKEN_COOKIE_MAX_AGE);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
    	return cookie;
    }
    
    
    /**
     * Returns whether the specified request is valid, i.e., if its CSRF Token cookie and parameter values are equal. 
     * @param request The request to validate
     * @return Whether the request is valid
     */
    public static final boolean validateRequest(HttpServletRequest request) {
        // Get the parameter value. If none was provided, the request was invalid
        String parameterValue = request.getParameter(CSRF_TOKEN_PARAMETER_NAME);
        if (parameterValue == null) return false;
        
        // Get the cookies for the request. If there were none, the request was invalid
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return false;
        
        // Iterate over the cookies. If we find a cookie whose name matches the one we're looking for,
        // then the request is valid if and only if the parameter value and the cookie value match.
        for (Cookie cookie : cookies) {
            if (CSRF_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return parameterValue.equals(cookie.getValue());
            }
        }
                
        // Otherwise, the request is invalid
        return false;
    }
    
    
    /**
     * Generates and returns a new CSRF token, which is created a hexadecimal string representation of 128-bits of random data.
     * @return A newly generated CSRF token. 
     */
    public static final String generateCSRFToken() {
        SecureRandom randomNumberGenerator = new SecureRandom();
        byte bytes[] = new byte[16];
        randomNumberGenerator.nextBytes(bytes);        
        return DatatypeConverter.printHexBinary(bytes);
    }
}
