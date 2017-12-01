const request = require('request');
const express = require('express');
const app = express();
const hbs = require('express-handlebars').create({defaultLayout:'main'});
const session = require('express-session');
const http = require('http');
const https = require('https');
const fs = require('fs');
const path = require('path');
//var util = request('util');
const router = express.Router();

app.engine('handlebars', hbs.engine);
app.set('view engine', 'handlebars');
app.set('port', 3000);
app.use(express.static('public'));

app.get('/', function (req,res) {
    var context = {'active': {'aboutme': true}};
	res.render('aboutme', context);
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

