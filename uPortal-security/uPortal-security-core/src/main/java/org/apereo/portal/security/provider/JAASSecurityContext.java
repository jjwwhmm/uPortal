/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.security.provider;

import java.io.Serializable;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.security.IAdditionalDescriptor;
import org.apereo.portal.security.ISecurityContext;
import org.apereo.portal.security.PortalSecurityException;

/** This is an implementation of a SecurityContext that checks a user's credentials using JAAS. */
class JAASSecurityContext extends ChainingSecurityContext
        implements ISecurityContext, Serializable {

    private static final Log log = LogFactory.getLog(JAASSecurityContext.class);

    private final int JAASSECURITYAUTHTYPE = 0xFF05;
    private IAdditionalDescriptor additionalDescriptor;

    /* package-private */ JAASSecurityContext() {}

    @Override
    public int getAuthType() {
        return this.JAASSECURITYAUTHTYPE;
    }

    @Override
    public IAdditionalDescriptor getAdditionalDescriptor() {
        return additionalDescriptor;
    }

    @Override
    public synchronized void authenticate() throws PortalSecurityException {
        this.isauth = false;

        if (this.myPrincipal.UID != null && this.myOpaqueCredentials.credentialstring != null) {

            try {
                // JAAS Stuff

                LoginContext lc = null;

                lc =
                        new LoginContext(
                                "uPortal",
                                new JAASInlineCallbackHandler(
                                        this.myPrincipal.UID,
                                        (new String(this.myOpaqueCredentials.credentialstring))
                                                .toCharArray())); // could not come up w/ a better
                // way to do this

                lc.login();
                additionalDescriptor = new JAASSubject(lc.getSubject());

                // the above will throw an exception if authentication does not succeed

                if (log.isInfoEnabled())
                    log.info("User " + this.myPrincipal.UID + " is authenticated");
                this.isauth = true;

            } catch (LoginException e) {
                if (log.isInfoEnabled())
                    log.info("User " + this.myPrincipal.UID + ": invalid password");
                if (log.isDebugEnabled()) log.debug("LoginException", e);
            }
        } else {
            log.error("Principal or OpaqueCredentials not initialized prior to authenticate");
        }

        // authenticate all subcontexts.
        super.authenticate();

        return;
    }
}
