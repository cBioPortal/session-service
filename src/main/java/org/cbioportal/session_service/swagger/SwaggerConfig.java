/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.session_service.swagger;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("cBioPortal session-service API")
                                .description(
                                        "RESTful API to access cBioPortal/cbioportal sessions in MongoDB.")
                                .version(
                                        "1.0 (beta)")
                                .license(
                                        new License()
                                                .name("Apache-2.0")
                                                .url("https://www.apache.org/licenses/LICENSE-2.0"))
                                .contact(
                                        new Contact()
                                                .name("cbioportal")
                                                .url("https://www.cbioportal.org")
                                                .email("cbioportal@googlegroups.com")));
    }

}
