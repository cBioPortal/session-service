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

package org.cbioportal.session_service.web;

import org.cbioportal.session_service.domain.*;
import org.cbioportal.session_service.domain.exception.*;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DuplicateKeyException;

import javax.validation.ConstraintViolationException;
import javax.validation.ConstraintViolation;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import java.io.IOException;

/**
 * @author Manda Wilson 
 */
@RestController // shorthand for @Controller, @ResponseBody
@RequestMapping(value = "/api/sessions/")
public class SessionServiceController
{

    private final SessionRepository sessionRepository;

    @Autowired
    public SessionServiceController(SessionRepository sessionRepository)
    {
        this.sessionRepository = sessionRepository;
    }
    
    @RequestMapping(method = RequestMethod.POST, value="/{source}/{type}")
    public Map<String, String> addSession(@PathVariable String source, 
        @PathVariable String type, 
        @RequestBody String data) throws SessionInvalidException
    { 
        Session session = new Session(source, type, data); 
        try {
            sessionRepository.saveSession(session); 
        } catch (DuplicateKeyException e) {
            // find session and return it
            // need the JSON object data, not the string passed
            session = sessionRepository.findOneBySourceAndTypeAndData(source, 
                type, 
                session.getData()); 
        } catch (ConstraintViolationException e) {
            throw new SessionInvalidException(buildConstraintViolationExceptionMessage(e));
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", session.getId());
        return map;
    }

    @RequestMapping(method = RequestMethod.GET, value="/{source}/{type}")
    public Iterable<Session> getSessions(@PathVariable String source, 
        @PathVariable String type)
    {
        return sessionRepository.findBySourceAndType(source, type);
    }

    @RequestMapping(method = RequestMethod.GET, value="/{source}/{type}/query")
    public Iterable<Session> getSessionsByQuery(@PathVariable String source, 
        @PathVariable String type, 
        @RequestParam(name="field") String field,
        @RequestParam(name="value") String value) throws SessionNotFoundException
    {
        List<Session> sessions = sessionRepository.findBySourceAndTypeAndQuery(source, type, field, value);
        if (sessions.size() != 0) {
            return sessions;
        }
        throw new SessionNotFoundException("field=" + field + "&value=" + value);
    }

    @RequestMapping(value = "/{source}/{type}/{id}", method = RequestMethod.GET)
    public Session getSession(@PathVariable String source, 
        @PathVariable String type,
        @PathVariable String id) throws SessionNotFoundException
    {
        Session session = sessionRepository.findOneBySourceAndTypeAndId(source, type, id);
        if (session != null) {
            return session;
        }
        throw new SessionNotFoundException(id);
    }

    @RequestMapping(value = "/{source}/{type}/{id}", method = RequestMethod.PUT)
    public void updateSession(@PathVariable String source, 
        @PathVariable String type,
        @PathVariable String id, 
        @RequestBody String data) throws SessionNotFoundException
    {
        Session savedSession = sessionRepository.findOneBySourceAndTypeAndId(source, type, id);
        if (savedSession != null) {
            savedSession.setData(data);
            try {
                sessionRepository.saveSession(savedSession);
            } catch (ConstraintViolationException e) {
                throw new SessionInvalidException(buildConstraintViolationExceptionMessage(e));
            }
            return;
        }
        throw new SessionNotFoundException(id);
    }

    @RequestMapping(value = "/{source}/{type}/{id}", method = RequestMethod.DELETE)
    public void deleteSession(@PathVariable String source, 
        @PathVariable String type,
        @PathVariable String id) throws SessionNotFoundException
    {
        int numberDeleted = sessionRepository.deleteBySourceAndTypeAndId(source, type, id);
        if (numberDeleted != 1) { // using unique id so never more than 1 
            throw new SessionNotFoundException(id);
        }
    } 

    @ExceptionHandler
    void handleSessionInvalid(SessionInvalidException e, HttpServletResponse response) throws IOException
    {
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    @ExceptionHandler
    void handleSessionNotFound(SessionNotFoundException e, HttpServletResponse response) throws IOException 
    {
        response.sendError(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

    private String buildConstraintViolationExceptionMessage(ConstraintViolationException e) 
    {
        StringBuffer errors = new StringBuffer();
        for (ConstraintViolation violation : e.getConstraintViolations()) { 
            errors.append(violation.getMessage());
            errors.append(";");
        }
        return errors.toString();
    }
}
