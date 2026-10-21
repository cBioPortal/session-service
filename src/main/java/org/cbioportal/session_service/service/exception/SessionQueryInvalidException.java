/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.session_service.service.exception;

public class SessionQueryInvalidException extends RuntimeException {
    public SessionQueryInvalidException(String message) {
        super(message);
    }   
}   
