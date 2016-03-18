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

import java.util.List;

import org.cbioportal.session_service.domain.Session;
import org.cbioportal.session_service.domain.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Manda Wilson 
 */
@RestController // shorthand for @Controller, @ResponseBody
@CrossOrigin(origins = "*") // Added to accessing from iViz application
@RequestMapping(value = "/api/sessions/")
public class SessionServiceController
{

    private final SessionRepository sessionRepository;

    @Autowired
    public SessionServiceController(SessionRepository sessionRepository)
    {
        this.sessionRepository = sessionRepository;
    }
    
    @RequestMapping(method = RequestMethod.POST)
    public Session addSession(@RequestBody String data) 
    {
        return sessionRepository.save(new Session(data)); 
    }

    @RequestMapping(method = RequestMethod.GET)
    public Iterable<Session> getSessions()
    {
        return sessionRepository.findAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Session getSession(@PathVariable String id) 
    {
        Session session = sessionRepository.findOne(id);
        if (session != null) {
            return session;
        }
        throw new SessionNotFoundException(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Session updateSession(@PathVariable String id, @RequestBody String data)
    {
        Session savedSession = sessionRepository.findOne(id);
        if (savedSession != null) {
            savedSession.setData(data);
            return sessionRepository.save(savedSession);
        }
        throw new SessionNotFoundException(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteSession(@PathVariable String id)
    {
        Session session = sessionRepository.findOne(id);
        if (session != null) {
            sessionRepository.delete(session);
        } else {
            throw new SessionNotFoundException(id);
        }
    }
    
    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public List<Session> getSessionByUserID(@RequestParam(name="userid") String userid) 
    {
        List<Session> sessions = sessionRepository.findVCByUserID(userid);
        if (sessions.size()!=0) {
            return sessions;
        }
        throw new UserSessionNotFoundException(userid);
    }
    

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class SessionNotFoundException extends RuntimeException {

        public SessionNotFoundException(String id) {
            super("could not find session '" + id + "'.");
        }
    }
    
    @ResponseStatus(HttpStatus.NOT_FOUND)
    class UserSessionNotFoundException extends RuntimeException {

        public UserSessionNotFoundException(String userid) {
            super("could not find session(s) for the user '" + userid + "'.");
        }
    }
}
