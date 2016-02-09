# session-service

RESTful API to cBioPortal/cbioportal sessions in MongoDB.  

Session information is stored in JSON, so this API generalizes to any JSON objects.

### Requirements

JDK 1.8 or later: http://www.oracle.com/technetwork/java/javase/downloads/index.html

Maven 3.0+: http://maven.apache.org/download.cgi

MongoDB: https://docs.mongodb.org/manual/

### Installation and setup

Create database 'session_service' using the 'mongo' shell interface to MongoDB:

```
$ mongo

> use session_service
```
Clone repository, compile, run tests, and start server:

```
$ git clone https://github.com/cBioPortal/session-service.git

$ cd session-service

session-service$ mvn package && java -jar target/session_service-0.1.0.jar

```

Assumptions: 

* Assumes MongoDB is running on localhost, default port 27017, 
that the database name is 'session_service' and that no username and password are required.
If any of these things are not true (hopefully the database is password protected)
modify this section of src/main/resources/application.properties:
```
spring.data.mongodb.uri=mongodb://localhost:27017/session_service
```
* Assumes you want to run the server on port 8080.  This can be overridden by
setting the process's SERVER_PORT environment variable.
```
session-service$ export set SERVER_PORT=8090; mvn package && java -jar target/session_service-0.1.0.jar
```


## API

### Create

#### POST http://localhost:8080/api/sessions/
Creates a session.  Returns status 200 and session in response body
on success. 
Example body for POST http://localhost:8080/api/sessions/:
```
{"portal-session": "my session information"}
```
Example response:
```
{
  "id": "56ba6a91ef860b0c66eaef89",
  "data": {
    "portal-session": "my session information"
  }
}
```
If no JSON data passed in request body returns 400 status
with something like the following in the body:
```
{
  "timestamp": 1455057448927,
  "status": 400,
  "error": "Bad Request",
  "exception": "org.springframework.http.converter.HttpMessageNotReadableException",
  "message": "Required request body is missing: public org.cbioportal.session_service.domain.Session org.cbioportal.session_service.web.SessionServiceController.addSession(java.lang.String)",
  "path": "/api/sessions/"
}
```
Sending invalid JSON in the request body returns a 500 status
with something like the following in the body:
```
{
  "timestamp": 1455057586500,
  "status": 500,
  "error": "Internal Server Error",
  "exception": "com.mongodb.util.JSONParseException",
  "message": "\n{\"portal-session\": blah blah blah}\n                   ^",
  "path": "/api/sessions/"
}
```

### Read

#### GET http://localhost:8080/api/sessions/
Returns all sessions.  Returns "[]" if no sessions.  Example response:
```
[
  {
    "id": "56ba6a91ef860b0c66eaef89",
    "data": {
      "portal-session": "my session information"
    }
  },
  {
    "id": "56ba6b40ef860b0c66eaef8a",
    "data": {
      "title": "my portal session",
      "description": "anything goes"
    }
  }
]
```

#### GET http://localhost:8080/api/sessions/:session_id
Returns single session with id :session_id.  If :session_id does 
not exist returns empty string in body.
Example response for http://localhost:8080/api/sessions/56ba6a91ef860b0c66eaef89:
```
{
  "id": "56ba6a91ef860b0c66eaef89",
  "data": {
    "portal-session": "my session information"
  }
}
```

### Update

#### PUT http://localhost:8080/api/sessions/:session_id
Updates a session with id :session_id.  Returns updated session
on success. 
Example body for http://localhost:8080/api/sessions/564372de341cc1380e000001:
```
{"portal-session": "my UPDATED session information"}
```
Example response:
```
{
  "id": "56ba6a91ef860b0c66eaef89",
  "data": {
    "portal-session": "my UPDATED session information"
  }
}
```
If no JSON data passed in request body return status 400 with a request
body like this:
```
{
  "timestamp": 1455058048734,
  "status": 400,
  "error": "Bad Request",
  "exception": "org.springframework.http.converter.HttpMessageNotReadableException",
  "message": "Required request body is missing: public org.cbioportal.session_service.domain.Session org.cbioportal.session_service.web.SessionServiceController.updateSession(java.lang.String,java.lang.String)",
  "path": "/api/sessions/56ba6a91ef860b0c66eaef89"
}
```

### Delete

#### DELETE http://localhost:8080/api/sessions/:session_id
Deletes a session with id :session_id.
Returns 200 status on success with empty request body. 
Example: http://localhost:8080/api/sessions/56ba6a91ef860b0c66eaef89




