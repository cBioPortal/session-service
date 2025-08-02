/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal Session Service.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.cbioportal.session_service.service.internal;

import java.util.List;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.bson.BSONException;
import org.bson.json.JsonParseException;
import org.cbioportal.session_service.domain.Session;
import org.cbioportal.session_service.domain.SessionRepository;
import org.cbioportal.session_service.domain.SessionType;
import org.cbioportal.session_service.service.SessionService;
import org.cbioportal.session_service.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;

/**
 * @author Manda Wilson
 */
@Service
public class SessionServiceImpl implements SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    /**
     * Adds a session to the repository.
     * @param source - catalog of the session
     * @param type - type of the session, e.g. "virtual_study", "settings", etc.
     * @param data - actual payload of the session
     * @return the session that was added, or an existing session with the same source, type and data (detected by checksum)
     * @throws SessionInvalidException if the session data is invalid, e.g. does not conform to the schema
     */
    @Override
    public Session addSession(String source, SessionType type, String data) throws SessionInvalidException {
        Session session = null;
        try {
            session = new Session();
            session.setSource(source);
            session.setType(type);
            session.setData(data);

            sessionRepository.upsertSession(session);
        } catch (DuplicateKeyException e) {
            session = sessionRepository.findOneBySourceAndTypeAndChecksum(source,
                    type,
                    session.getChecksum());
        } catch (ConstraintViolationException e) {
            throw new SessionInvalidException(buildConstraintViolationExceptionMessage(e));
        } catch (JsonParseException e) {
            throw new SessionInvalidException(e.getMessage());
        } catch (HttpMessageNotReadableException e) {
            throw new SessionInvalidException(e.getMessage());
        }
        return session;
    }

    /**
     * Creates a new session in the repository.
     * @param id the custom unique identifier for the session, can be null (automatically generated)
     * @param source - catalog of the session
     * @param type - type of the session, e.g. "virtual_study"
     * @param data - actual payload of the session
     * @throws SessionAlreadyExists if a session with the same id or source, type and data already exists
     * @throws SessionInvalidException if the session data is invalid, e.g. does not conform to the schema
     * This method is different from {@link #addSession(String, SessionType, String)} in the following ways:
     *             - it does not look up and return an existing session with identical data (detected by checksum). In such scenario this method would just throw SessionAlreadyExistsException.
     *               - Note (historical context): This behavior is not the primary intent of the method, but rather a side effect of the unique index on the source, type, and checksum fields, which prevents duplicate sessions (see {@link #addSession(String, SessionType, String)}).
     *               - Although this constraint wasn't by design, it might be useful, particularly for published virtual studies, ensuring that no two are identical. Which is unlikely to happen, because the data often contains a timestamp.
     *             - it does not overwrite an existing session with the same id. In such scenario this method would throw SessionAlreadyExistsException. Only really possible when custom id is provided.
     */
    @Override
    public Session createNewSession(String id, String source, SessionType type, String data) throws SessionInvalidException {
        try {
            Session session = new Session();
            if (id != null) {
                session.setId(id);
            }
            session.setSource(source);
            session.setType(type);
            session.setData(data);

            sessionRepository.insertSession(session);
            return session;
        } catch (DuplicateKeyException e) {
            throw new SessionAlreadyExists(e.getMessage());
        } catch (RuntimeException e) {
            throw new SessionInvalidException(e.getMessage());
        }
    }

    @Override
    public List<Session> getSessions(String source, SessionType type) {
        return sessionRepository.findBySourceAndType(source, type);
    }

    @Override
    public List<Session> getSessionsByQuery(String source, SessionType type, String query)
        throws SessionQueryInvalidException {
        try {
            return sessionRepository.findBySourceAndTypeAndQuery(source, type, query);
        } catch (IllegalArgumentException | JsonParseException | BSONException e) {
            throw new SessionQueryInvalidException(e.getMessage());
        } catch (UncategorizedMongoDbException e) {
            throw new SessionQueryInvalidException(e.getMessage());
        }
    }

    @Override
    public Session getSession(String source, SessionType type, String id) throws SessionNotFoundException {
        Session session = sessionRepository.findOneBySourceAndTypeAndId(source, type, id);
        if (session != null) {
            return session;
        }
        throw new SessionNotFoundException(id);
    }

    @Override
    public void updateSession(String source, SessionType type, String id, String data) throws SessionInvalidException,
        SessionNotFoundException {
        Session savedSession = sessionRepository.findOneBySourceAndTypeAndId(source, type, id);
        if (savedSession != null) {
            try {
                savedSession.setData(data);
                sessionRepository.upsertSession(savedSession);
            } catch (ConstraintViolationException e) {
                throw new SessionInvalidException(buildConstraintViolationExceptionMessage(e));
            } catch (JsonParseException e) {
                throw new SessionInvalidException(e.getMessage());
            }
            return;
        }
        throw new SessionNotFoundException(id);
    }

    @Override
    public void deleteSession(String source, SessionType type, String id) throws SessionNotFoundException {
        long numberDeleted = sessionRepository.deleteBySourceAndTypeAndId(source, type, id);
        if (numberDeleted != 1) { // using unique id so never more than 1
            throw new SessionNotFoundException(id);
        }
    }

    private String buildConstraintViolationExceptionMessage(ConstraintViolationException e) {
        StringBuffer errors = new StringBuffer();
        for (ConstraintViolation violation : e.getConstraintViolations()) {
            errors.append(violation.getMessage());
            errors.append(";");
        }
        return errors.toString();
    }

}
