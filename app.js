const express = require('express')
const app = express()

var http = require('http'),
    fs = require('fs');

app.engine('.html', require('ejs').__express);
app.set('view engine', 'html');

app.use(express.static('public'));

app.get('/', function(req, res){
	res.render("index.ejs");
});

app.listen(8080);
console.log("Server is running. Now open 'http://localhost:8080' in a browser.");