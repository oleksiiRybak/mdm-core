/*
 * Copyright 2020 University of Oxford
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package uk.ac.ox.softeng.maurodatamapper.core.session


import javax.servlet.ServletContext
import javax.servlet.http.HttpSession
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener

/**
 * List to all sessions being created and destroyed and make sure they're in the active list
 * This way we can see all users, both logged in and not logged in
 */
class SessionService implements HttpSessionListener {
    public static final String CONTEXT_PROPERTY_NAME = 'activeSessionMap'

    HttpSession setUserEmailAddress(HttpSession session, String emailAddress) {
        session.setAttribute('emailAddress', emailAddress)
        storeSession(session)
    }

    HttpSession setLastAccessedUrl(HttpSession session, String lastAccessedUrl) {
        session.setAttribute('lastUrl', lastAccessedUrl)
        storeSession(session)
    }

    boolean isAuthenticatedSession(HttpSession session) {
        session.getAttribute('emailAddress') && retrieveSession(session).getAttribute('emailAddress')
    }

    String getSessionEmailAddress(HttpSession session) {
        session.getAttribute('emailAddress')
    }

    void destroySession(HttpSession session) {
        session.invalidate()
        getActiveSessionMap(session.servletContext).remove(session.id)
    }

    /**
     * Will store or update an existing stored version
     *
     * @param session
     */
    HttpSession storeSession(HttpSession session) {
        getActiveSessionMap(session.servletContext).put(session.id, session)
    }

    /**
     * Retrieve the stored session from the activeSession map. Will probably only be used by admin services as all controllers and interceptors
     * have the current session available
     * @param session
     * @return
     */
    HttpSession retrieveSession(HttpSession session) {
        retrieveSession(session.servletContext, session.id)
    }

    HttpSession retrieveSession(ServletContext servletContext, String id) {
        getActiveSessionMap(servletContext).get(id)
    }

    /**
     * Create a map to store the active sessions and add this service as a listener.
     * Should be called in bootstrap
     * @param servletContext
     */
    void initialiseToContext(ServletContext servletContext) {
        servletContext.setAttribute(CONTEXT_PROPERTY_NAME, new HashMap<String, HttpSession>())
    }

    HashMap<String, HttpSession> getActiveSessionMap(ServletContext servletContext) {
        servletContext.getAttribute(CONTEXT_PROPERTY_NAME) as HashMap<String, HttpSession>
    }

    @Override
    void sessionCreated(HttpSessionEvent event) {
        // Make sure all sessions created are stored even if they're for unlogged in users
        storeSession(event.session)
    }

    @Override
    void sessionDestroyed(HttpSessionEvent event) {
        // This will be called by either our destroySession or by the servlet context auto destroying a session
        getActiveSessionMap(event.session.servletContext).remove(event.session.id)
    }
}
