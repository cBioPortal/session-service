package org.cbioportal.session_service.web;

import org.springframework.web.bind.annotation.*;

/**
 * @author Hongxin Zhang
 */
@RestController
@RequestMapping(value = "/info")
public class InfoController {
    // This is supposed to in sync with the pom version
    String version = "0.1.0";

    public String getVersion() {
        return version;
    }

    @RequestMapping(method = RequestMethod.GET, value = "")
    public String getInfo() {
        return this.getVersion();
    }
}
