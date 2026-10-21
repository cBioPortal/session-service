/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.session_service.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.bson.Document;
import org.springframework.data.annotation.Id;
import org.springframework.util.DigestUtils;

/**
 * @author Manda Wilson
 */
@JsonInclude(Include.NON_NULL)
public class Session {
    @Id
    private String id;
    @NotNull
    private String checksum;
    @NotNull
    private Object data;
    @NotNull
    @Size(min=3, message="source has a minimum length of 3")
    private String source;
    @NotNull
    private SessionType type;


    @JsonView(Session.Views.IdOnly.class)
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setData(Object data) {
        if(data instanceof String) {
            this.data = Document.parse((String)data);
        } else {
            this.data = data;
        }
        this.checksum = DigestUtils.md5DigestAsHex(this.data.toString().getBytes());
    }

    @JsonView(Session.Views.Full.class)
    public Object getData() {
        return data;
    }

    public void setType(SessionType type) {
        this.type = type;
    }

    @JsonView(Session.Views.Full.class)
    public SessionType getType() {
        return type;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @JsonView(Session.Views.Full.class)
    public String getSource() {
        return source;
    }

    public static final class Views {
        // show only id
        public interface IdOnly {}

        // show all data
        public interface Full extends IdOnly {}
    }
}
