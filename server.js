// server.js
// Author: Manda Wilson, wilson@cbio.mskcc.org
// Note: default bodyParser.limit is 100kb for request body size
//   See: https://github.com/expressjs/body-parser#bodyparserjsonoptions

// BASE SETUP
// =============================================================================

// call the packages we need
var express    = require('express');        // call express
var app        = express();                 // define our app using express
var bodyParser = require('body-parser');

var mongoose   = require('mongoose');
mongoose.connect('mongodb://localhost:27017/portal'); // connect to our database

var Session = require('./app/models/session');

// configure app to use bodyParser()
// this will let us get the data from a POST
app.use(bodyParser.json());

var port = process.env.PORT || 8080;        // set our port

// ROUTES FOR OUR API
// =============================================================================
var router = express.Router();              // get an instance of the express Router

// middleware to use for all requests
router.use(function(req, res, next) {
    // do logging
    console.log(req.body);
    next(); // make sure we go to the next routes and don't stop here
});

// routes for our API will happen here
// on routes that end in /sessions
// ----------------------------------------------------
router.route('/sessions')

    // create a session (accessed at POST http://localhost:8080/api/sessions)
    // returns { _id: session._id } on success
    .post(function(req, res, next) {
        
        var session = new Session();      // create a new instance of the Session model

        // note this does not catch something like [{}]
        if (Object.keys(req.body).length == 0) {
            console.log("no data in body")
            var err = new Error("Some JSON data required.");
            err.status = 404;
            return next(err);
        }
        session.data = req.body;
   
        // save the session and check for errors
        session.save(function(err) {
            if (err)
                res.send({ error: err });
    
            res.json({ _id: session._id, message: "Session created" });
        });
    })

    // get all the sessions (accessed at GET http://localhost:8080/api/sessions)
    // returns empty set [] if no sessions
    .get(function(req, res) {
        Session.find(function(err, sessions) {
            if (err)
                res.send(err);

            if (sessions == null)
                res.json([]);
            else
                res.json(sessions);
        });
    });

// on routes that end in /sessions/:session_id
// ----------------------------------------------------
router.route('/sessions/:session_id')

    // get the session with that id (accessed at GET http://localhost:8080/api/sessions/:session_id)
    // if session_id does not exist returns status 404 with {error: 'Invalid URL'} in body
    .get(function(req, res) {
        Session.findById(req.params.session_id, function(err, session) {
            if (err)
                res.send(err);
    
            if (session == null)
                res.status(404).send({error: 'Invalid URL'});

            res.json(session);
        });
    })

    // update the session with this id (accessed at PUT http://localhost:8080/api/sessions/:session_id)
    // TODO tries to update a null object and fails - fix
    .put(function(req, res) {

        // use our session model to find the session we want
        Session.findById(req.params.session_id, function(err, session) {

            if (err)
                res.send(err);

            // note this does not catch something like [{}]
            if (Object.keys(req.body).length == 0) {
                console.log("no data in body")
                var err = new Error("Some JSON data required.");
                err.status = 404;
                return next(err);
            }
            if (session == null) {
                res.status(404).send({error: 'Invalid URL'});
            }
            else {
                session.data = req.body;

                // save the session
                session.save(function(err) {
                    if (err)
                        res.send(err);

                    res.json({ _id: session._id, message: "Session updated" });
                });
            }
        });
    })

    // delete the session with this id (accessed at DELETE http://localhost:8080/api/sessions/:session_id)
    .delete(function(req, res) {
        Session.remove({
            _id: req.params.session_id
        }, function(err, session) {
            if (err)
                res.send(err);

            res.json({ message: 'Successfully deleted' });
        });
    });

// REGISTER OUR ROUTES -------------------------------
// all of our routes will be prefixed with /api
app.use('/api', router);

// ERROR HANDLING -------------------------------
app.use(function(err, req, res, next) {
    console.error(err.stack);
    res.status(err.status).send({ error: err.message });
});

// START THE SERVER
// =============================================================================
app.listen(port);
console.log('Listening on port ' + port);
