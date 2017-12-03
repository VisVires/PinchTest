var request = require('request');
var express = require('express');
var app = express();
var hbs = require('express-handlebars').create({defaultLayout:'main'});
var session = require('express-session');
var http = require('http');
var https = require('https');
var fs = require('fs');
var path = require('path');
var config = require('./config.js');
var google = require('googleapis');
var oauth2Client = google.auth.OAuth2;
var plus = google.plus('v1');


var client_id = config.client_id;
var client_secret = config.client_secret;
var redirect_url =  config.redirect_url
var scopes = [
	'https://www.googleapis.com/auth/plus.login',
	'https://www.googleapis.com/auth/plus.me',
	'https://www.googleapis.com/auth/userinfo.email',
	'https://www.googleapis.com/auth/userinfo.profile'
];

//var url = oauth2Client.generateAuthUrl({
//	access_type: 'offline',
//	scope: scopes
//});

const router = express.Router();

app.engine('handlebars', hbs.engine);
app.set('view engine', 'handlebars');
app.set('port', 8080);
app.use(express.static('public'));

app.get('/', function (req,res) {
    console.log(redirect_url);
});


//check for 404 error
app.use(function (req,res) {
    res.status(404);
    res.render('404');
});

//check for 500 error
app.use(function (err, req, res, next) {
    console.error(err.stack);
    res.type('plain/text');
    res.status(500);
    res.render('500');
});


http.createServer(app).listen(app.get('port'), function(){
    console.log('Express started on http://localhost:' + app.get('port') + '; press Ctrl-C to terminate.');
});

