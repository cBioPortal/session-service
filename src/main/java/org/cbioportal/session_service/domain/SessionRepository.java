/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.session_service.domain;

import org.cbioportal.session_service.domain.internal.SessionRepositoryCustom;
 
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * @author Manda Wilson 
 */
public interface SessionRepository 
    extends MongoRepository<Session, String>, SessionRepositoryCustom {}
