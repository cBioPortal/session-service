/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.session_service.service;

import org.cbioportal.session_service.domain.Session;
import org.cbioportal.session_service.domain.SessionType;
import org.cbioportal.session_service.service.exception.*;

import java.util.List;

/**
 * @author Manda Wilson 
 */
public interface SessionService {
    Session upsertSession(String source, SessionType type, String data) throws SessionInvalidException;
    Session insertSession(String id, String source, SessionType type, String data) throws SessionInvalidException;
    List<Session> getSessions(String source, SessionType type);
    List<Session> getSessionsByQuery(String source, SessionType type, String query);
    Session getSession(String source, SessionType type, String id) throws SessionNotFoundException;
    void updateSession(String source, SessionType type, String id, String data) throws SessionInvalidException, 
        SessionNotFoundException;
    void deleteSession(String source, SessionType type, String id) throws SessionNotFoundException;
}
