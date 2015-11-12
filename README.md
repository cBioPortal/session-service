# session-service

RESTful API to cBioPortal/cbioportal sessions in MongoDB.  

Session information is stored in JSON, so this API generalizes to any JSON objects.

### Requirements

Node: https://nodejs.org/en/

MongoDB: https://docs.mongodb.org/manual/

### Installation and setup

Create database 'portal' using the 'mongo' shell interface to MongoDB:

```
$ mongo

> use portal
```
Clone repository, install dependencies, and run node server:

```
$ git clone https://github.com/cBioPortal/session-service.git

$ cd session-service

session-service$ npm install

session-service$ node server.js
```

Assumptions: 

* Assumes MongoDB is running on localhost, default port 27017, 
that the database name is 'portal' and that no username and password are required.
If any of these things are not true (hopefully the database is password protected)
modify this section of server.js:
```
mongoose.connect('mongodb://localhost:27017/portal');
```
* Assumes you want to run the server on port 8080.  This can be overridden by
setting the process's PORT environment variable.


## API

### Create

#### POST http://localhost:8080/api/sessions
Creates a session.  Returns { _id: session._id } on success.
If no JSON data passed in request body returns status 404 with 
{error: 'Some JSON data required.'} in body.
Example body for POST http://localhost:8080/api/sessions:
```
{"portal-session": "my session information"}
```
Example response:
```
{
  "_id": "5644c497faf8ba7906000001",
  "message": "Session created"
}
```

### Read

#### GET http://localhost:8080/api/sessions/
Returns all sessions.  Returns "[]" if no sessions.  Example response:
```
[
  {
    "_id": "564372de341cc1380e000001",
    "data": {
      "test": {
        "title": "my portal session"
      }
    },
    "__v": 0
  },
  {
    "_id": "5644c497faf8ba7906000001",
    "data": {
      "portal-session": "my session information"
    },
    "__v": 0
  }
]
```

#### GET http://localhost:8080/api/sessions/:session_id
Returns single session with id :session_id.  If :session_id does 
not exist returns status 404 with {error: 'Invalid URL'} in body.
Example response for http://localhost:8080/api/sessions/564372de341cc1380e000001:
```
{
  "_id": "564372de341cc1380e000001",
  "data": {
    "test": {
      "title": "my portal session"
    }
  },
  "__v": 0
}
```

### Update

#### PUT http://localhost:8080/api/sessions/:session_id
Updates a session with id :session_id.  Returns { _id: session._id } 
on success. If no JSON data passed in request body returns status 404 with 
{error: 'Some JSON data required.'} in body.  
Example body for http://localhost:8080/api/sessions/564372de341cc1380e000001:
```
{"portal-session": "my UPDATED session information"}
```
Example response:
```
{
  "_id": "564372de341cc1380e000001",
  "message": "Session updated"
}
```

### Delete

#### DELETE http://localhost:8080/api/sessions/:session_id
Deletes a session with id :session_id.
Example response for http://localhost:8080/api/sessions/564372de341cc1380e000001:
```
{
  "message": "Successfully deleted"
}
```




