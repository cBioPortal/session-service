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
import org.cbioportal.session_service.service.exception.*;
import org.cbioportal.session_service.service.SessionService;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonView;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @author Manda Wilson 
 */
@RestController // shorthand for @Controller, @ResponseBody
@RequestMapping(value = "/api/sessions/")
public class SessionServiceController {

    @Autowired
    private SessionService sessionService;

    @RequestMapping(method = RequestMethod.POST, value="/{source}/{type}")
    @JsonView(Session.Views.IdOnly.class)
    public Session addSession(@PathVariable String source, 
        @PathVariable String type, 
        @RequestBody String data) { 
        return sessionService.addSession(source, type, data);
    }

    @RequestMapping(method = RequestMethod.GET, value="/{source}/{type}")
    @JsonView(Session.Views.Full.class)
    public Iterable<Session> getSessions(@PathVariable String source, 
        @PathVariable String type) {
        return sessionService.getSessions(source, type);
    }

    @RequestMapping(method = RequestMethod.GET, value="/{source}/{type}/query")
    @JsonView(Session.Views.Full.class)
    public Iterable<Session> getSessionsByQuery(@PathVariable String source, 
        @PathVariable String type, 
        @RequestParam(name="field") String field,
        @RequestParam(name="value") String value) {
        return sessionService.getSessionsByQuery(source, type, field, value);
    }

    @RequestMapping(value = "/{source}/{type}/{id}", method = RequestMethod.GET)
    @JsonView(Session.Views.Full.class)
    public Session getSession(@PathVariable String source, 
        @PathVariable String type,
        @PathVariable String id) {
        return sessionService.getSession(source, type, id);
    }

    @RequestMapping(value = "/{source}/{type}/{id}", method = RequestMethod.PUT)
    public void updateSession(@PathVariable String source, 
        @PathVariable String type,
        @PathVariable String id, 
        @RequestBody String data) {
        sessionService.updateSession(source, type, id, data);
    }

    @RequestMapping(value = "/{source}/{type}/{id}", method = RequestMethod.DELETE)
    public void deleteSession(@PathVariable String source, 
        @PathVariable String type,
        @PathVariable String id) {
        sessionService.deleteSession(source, type, id);
    } 

    @ExceptionHandler
    public void handleSessionInvalid(SessionInvalidException e, HttpServletResponse response) 
        throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    @ExceptionHandler
    public void handleSessionQueryInvalid(SessionQueryInvalidException e, HttpServletResponse response) 
        throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }
    
    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Session not found")
    @ExceptionHandler(SessionNotFoundException.class)
    public void handleSessionNotFound() {}
}
