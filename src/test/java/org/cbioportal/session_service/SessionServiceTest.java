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

package org.cbioportal.session_service;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.net.URL;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SessionService.class)
@WebAppConfiguration
// pick random port for testing
@IntegrationTest({"server.port=0"})
// use application-test.properties config file
@ActiveProfiles("test")
public class SessionServiceTest {

    // get randomly assigned port
    @Value("${local.server.port}")
    private int port;

    private URL base;
    private RestTemplate template;

    @Before
    public void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port + "/api/sessions/");
        template = new TestRestTemplate();
    }

    @After
    public void tearDown() throws Exception {
        // get all and delete them
        ResponseEntity<String> response = template.getForEntity(base.toString(), String.class);
        Pattern idPattern = Pattern.compile("\"id\":\"([^\"]+)\"");
        Matcher idMatcher = idPattern.matcher(response.getBody());
        System.out.println("MEW: " + response.getBody());
        while (idMatcher.find()) {
            System.out.println("MEW: " + idMatcher.group(1));
            String id = idMatcher.group(1);
            template.delete(base.toString() + id);
        }
    }

    @Test
    public void getSessionsNoData() throws Exception {
        ResponseEntity<String> response = template.getForEntity(base.toString(), String.class);
        assertThat(response.getBody(), equalTo("[]"));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void getSessionsData() throws Exception {
        // first add data
        String data = "\"portal-session\":\"my session information\"";
        ResponseEntity<String> response = addData(data);

        // now test data is returned by GET /api/sessions/
        response = template.getForEntity(base.toString(), String.class);
        assertThat(expectedResponse(response.getBody(), data, true), equalTo(true)); 
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

    }
    
    @Test
    public void addSession() throws Exception {
        // add data
        String data = "\"portal-session\":\"my session information\"";
        ResponseEntity<String> response = addData(data);

        // test that we get the db record back and that the status was 200 
        assertThat(expectedResponse(response.getBody(), data), equalTo(true)); 
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void addSessionNoData() throws Exception {
        // add {} actually works TODO decide if it should
        String data = "";
        ResponseEntity<String> response = addData(data);

        // test that we get the db record back and that the status was 200 
        assertThat(expectedResponse(response.getBody(), data), equalTo(true)); 
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
       
        response = addData(null); 
        assertThat(response.getBody(), containsString("Required request body is missing"));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
    }


    @Test
    public void addSessionInvalidData() throws Exception {
        ResponseEntity<String> response = addData("\"portal-session\": blah blah blah"); 
        assertThat(response.getBody(), containsString("com.mongodb.util.JSONParseException"));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR));

    }

    private ResponseEntity<String> addData(String data) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (data != null) {
            data = "{" + data + "}";
        }
        HttpEntity<String> entity = new HttpEntity<String>(data, headers);
        return template.exchange(base.toString(), HttpMethod.POST, entity, String.class);
    }

    /*
     * plural is false.
     */
    private boolean expectedResponse(String responseBody, String data) throws Exception {
        return expectedResponse(responseBody, data, false);
    }

    private boolean expectedResponse(String responseBody, String data, boolean plural) throws Exception {
        String pattern = "\\{\"id\":\"([^\"]+)\",\"data\":\\{" + data + "\\}\\}";
        if (plural) {
            pattern = "\\[" + pattern + "\\]";
        }
        pattern = "^" + pattern + "$";
        System.out.println("MEW: pattern = " + pattern);
        System.out.println("MEW: responseBody = " + responseBody);
        Pattern expectedResponsePattern = Pattern.compile(pattern);
        Matcher responseMatcher = expectedResponsePattern.matcher(responseBody);
        return responseMatcher.matches();
    }

};
