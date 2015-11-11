// app/models/session.js

var mongoose     = require('mongoose');
var Schema       = mongoose.Schema;

var SessionSchema   = new Schema({data: Schema.Types.Mixed});

module.exports = mongoose.model('Session', SessionSchema);
