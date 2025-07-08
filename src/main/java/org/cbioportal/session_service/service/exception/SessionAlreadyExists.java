package org.cbioportal.session_service.service.exception;

/**
 * Exception thrown when a session with the specified ID already exists in the repository.
 * This is typically used to prevent undesired overwriting of existing sessions.
 */
public class SessionAlreadyExists extends RuntimeException {
    public SessionAlreadyExists(String message) {
        super(message);
    }
}
