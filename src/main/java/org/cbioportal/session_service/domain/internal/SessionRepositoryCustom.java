/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.session_service.domain.internal;

import org.cbioportal.session_service.domain.Session;
import org.cbioportal.session_service.domain.SessionType;

import java.util.List;

/**
 * @author Manda Wilson 
 */
public interface SessionRepositoryCustom {
    /**
     * Inserts or updates the given {@link Session} in the corresponding MongoDB collection.
     * <p>
     * If the collection for the session type does not exist, it will be created along with
     * appropriate compound indexes on the fields: <code>source</code>, <code>type</code>, and <code>checksum</code>.
     * <p>
     * If a session with the same identifier already exists, it will be updated (replaced).
     *
     * @param session the {@link Session} object to be saved or updated in the database.
     */
    void upsertSession(Session session);

    /**
     * Inserts a new {@link Session} into the corresponding MongoDB collection.
     * <p>
     * If the collection for the session type does not exist, it will be created along with
     * appropriate compound indexes on the fields: <code>source</code>, <code>type</code>, and <code>checksum</code>.
     * <p>
     * This method will throw an exception if a session with the same identifier already exists.
     *
     * @param session the {@link Session} object to be inserted into the database.
     * @return the inserted {@link Session} instance.
     */
    Session insertSession(Session session);

    Session findOneBySourceAndTypeAndData(String source, SessionType type, Object data);

    Session findOneBySourceAndTypeAndChecksum(String source, SessionType type, String checksum);

    Session findOneBySourceAndTypeAndId(String source, SessionType type, String id);

    List<Session> findBySourceAndType(String source, SessionType type);

    long deleteBySourceAndTypeAndId(String source, SessionType type, String id);

    List<Session> findBySourceAndTypeAndQuery(String source, SessionType type, String query);

}
