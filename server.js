// server.js
// https://scotch.io/tutorials/build-a-restful-api-using-node-and-express-4

// BASE SETUP
// =============================================================================

// call the packages we need
var express    = require('express');        // call express
var app        = express();                 // define our app using express

// to get raw body
// from http://stackoverflow.com/questions/18710225/node-js-get-raw-request-body-using-express
/*app.use(function(req, res, next) {
  req.rawBody = '';
  req.setEncoding('utf8');

  req.on('data', function(chunk) { 
    req.rawBody += chunk;
  });

  req.on('end', function() {
    next();
  });
});*/

var bodyParser = require('body-parser');

var mongoose   = require('mongoose');
mongoose.connect('mongodb://localhost:27017/portal'); // connect to our database

var Session = require('./app/models/session');

// configure app to use bodyParser()
// this will let us get the data from a POST
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

var port = process.env.PORT || 8080;        // set our port

// ROUTES FOR OUR API
// =============================================================================
var router = express.Router();              // get an instance of the express Router

// middleware to use for all requests
router.use(function(req, res, next) {
    // do logging
    //console.log(req.rawBody);
    console.log(req.data);
    console.log(req.body);
    next(); // make sure we go to the next routes and don't stop here
});

// routes for our API will happen here
// on routes that end in /sessions
// ----------------------------------------------------
router.route('/sessions')

    // create a session (accessed at POST http://localhost:8080/api/sessions)
    .post(function(req, res) {
        
        var session = new Session();      // create a new instance of the Session model
        session.data = req.body;

        // save the session and check for errors
        session.save(function(err) {
            if (err)
                res.send(err);

            res.json({ message: 'Session created' });
        });
        
    })

    // get all the sessions (accessed at GET http://localhost:8080/api/sessions)
    // TODO is it ok to return 'null' when no session found?
    .get(function(req, res) {
        Session.find(function(err, sessions) {
            if (err)
                res.send(err);

            res.json(sessions);
        });
    });

// on routes that end in /sessions/:session_id
// ----------------------------------------------------
router.route('/sessions/:session_id')

    // get the session with that id (accessed at GET http://localhost:8080/api/sessions/:session_id)
    // TODO is it ok to return 'null' when no session found?
    // really you are supposed to return 404 with json encoded error message in body, right?
    // http://stackoverflow.com/questions/26845631/is-it-correct-to-return-404-when-a-rest-resource-is-not-found/26845991#26845991
    .get(function(req, res) {
        Session.findById(req.params.session_id, function(err, session) {
            if (err)
                res.send(err);
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

            session.data = req.body;

            // save the session
            session.save(function(err) {
                if (err)
                    res.send(err);

                res.json({ message: 'Session updated!' });
            });

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

// START THE SERVER
// =============================================================================
app.listen(port);
console.log('Magic happens on port ' + port);
