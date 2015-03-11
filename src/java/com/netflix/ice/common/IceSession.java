/*
 *
 *  Copyright 2013 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.ice.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import javax.servlet.http.HttpSession;

/**
* An IceSession is our interfact to an HttpServlet Session 
*/
public class IceSession {
    private static final Logger logger = LoggerFactory.getLogger(IceSession.class);
    private final String USER_NAME = "user_name";
    private final String AUTHENTICATED_SESSION_KEY = "authenticated";
    private final String ADMIN_SESSION_KEY = "admin";
    private final String ALLOWED_ACCOUNT_SESSION_PREFIX_KEY = "allowed_account";
    private final String ALLOWED_ACCOUNTS = "allowed_accounts";
    private final HttpSession session;

    public IceSession(HttpSession session) {
        this.session = session;
    }

    /**
    * Auth or DeAuth this session.
    * @parm authd
    */
    public void authenticate(Boolean authd) { 
       logger.info("authenticate: " + authd);
       session.setAttribute(AUTHENTICATED_SESSION_KEY, authd);
    }

    public String username() { 
       return (String)session.getAttribute(USER_NAME);
    }

    public void setUsername(String username) { 
       session.setAttribute(USER_NAME, username);
    }

    /**
    * Is this session authenticated?
    */ 
    public Boolean isAuthenticated() { 
       logger.debug("isAuthenticated?");
       Boolean authd = (Boolean)session.getAttribute(AUTHENTICATED_SESSION_KEY);

       if (authd == null) {
          logger.error("User has no authentication entry");
          return false;
       } else if (! authd.booleanValue()) {
          logger.error("User has been explicitly denied - " + authd);
          return false;
       }
       return true;
    }

    /**
     * 100% invalidate this session so it cannot be used for login.
     */
    public void voidSession() {
        logger.info("Void Session!");
        authenticate(false);
        session.setAttribute(ADMIN_SESSION_KEY, new Boolean(false));
        List<String> allowedAccounts = (List<String>)session.getAttribute(ALLOWED_ACCOUNTS);
        if (allowedAccounts != null) {
            Iterator<String> iter = allowedAccounts.iterator();
            while (iter.hasNext()) {
                String allowedAccount = iter.next();
                revokeAccount(allowedAccount);
                iter.remove();
            }
        }
    }

    /**
    * Give access to all Account Data.
    */
    public void allowAllAccounts() {
        session.setAttribute(ADMIN_SESSION_KEY,new Boolean(true));
    }

    /**
    * Get a list of Accounts that this session can view
    */
    public List<String> allowedAccounts() {
        List<String> allowedAccounts = (List<String>)session.getAttribute(ALLOWED_ACCOUNTS);
        if (allowedAccounts == null) {
            return new ArrayList<String>();
        }
        return allowedAccounts;
    }

    /**
    * Revoke accountId's data for this session?
    * @param accountId
    */
    public void revokeAccount(String accountId) {
        session.removeAttribute(ALLOWED_ACCOUNT_SESSION_PREFIX_KEY + accountId);
       
    }

    /**
    * Is this an Admin session?
    */
    public boolean isAdmin() {
        return ((Boolean)session.getAttribute(ADMIN_SESSION_KEY)).booleanValue();
    }

    /**
    * Is accountId's data allowed for this session?
    * @param accountId
    */
    public boolean allowedAccount(String accountId) {
        Boolean allowedAll = (Boolean)session.getAttribute(ADMIN_SESSION_KEY);
        if (allowedAll != null && allowedAll.booleanValue())
        {
            return true;
        }

        Boolean allowedAccount = (Boolean)session.getAttribute(ALLOWED_ACCOUNT_SESSION_PREFIX_KEY + accountId);
        if (allowedAccount != null && allowedAccount.booleanValue()) {
            return true;
        }
        return false;
    } 

    /**
    * Revoke accountId's data for this session?
    * @param accountId
    */
    public void allowAccount(String accountId) {
        List<String> allowedAccounts = (List<String>)session.getAttribute(ALLOWED_ACCOUNTS);
        if (allowedAccounts == null) {
            allowedAccounts = new ArrayList<String>();
        }
        allowedAccounts.add(accountId);
        session.setAttribute(ALLOWED_ACCOUNTS, allowedAccounts);
        session.setAttribute(ALLOWED_ACCOUNT_SESSION_PREFIX_KEY + accountId, new Boolean(true));
    }
}
