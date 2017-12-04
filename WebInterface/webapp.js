'use strict';
// [START app]
var request = require('request');
var express = require('express');
const app = express();
var hbs = require('express-handlebars').create({defaultLayout:'main'});
var session = require('express-session');
var http = require('http');
var https = require('https');
var fs = require('fs');
const config = require('./config.js');
const oauth = require('./oauth2.js');
var path = require('path');

//var util = request('util');
var router = express.Router();
var SECRET = config.secret;
var MEM_CACH = config.memcach_endpoint;

app.engine('handlebars', hbs.engine);
app.set('view engine', 'handlebars');
app.set('trust proxy', true);
app.use(express.static('public'));


//set up session with memcached for many instances
const sessionConfig = {
  resave: false,
  saveUninitialized: false,
  secret: SECRET,
  signed: true
};

if (config.get('NODE_ENV') === 'production' && MEM_CACH) {
  sessionConfig.store = new MemcachedStore({
    hosts: MEM_CACH
  });
}

app.use(session(sessionConfig));

app.use(oauth2.template);

app.get('/', oauth2.required, (req, res, next) => {
  res.status(200).send('Hello, world!').end();
});

// Start the server
const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
  console.log(`App listening on port ${PORT}`);
  console.log('Press Ctrl+C to quit.');
});
// [END app]
