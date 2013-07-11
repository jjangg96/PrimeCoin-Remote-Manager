var express = require('express'), fs = require('fs'), app = express(), http = require('http'), server = http.createServer(app), io = require('socket.io').listen(server);
var ejs = require('ejs');
var command = "listsinceblock_1";
var port = 3000;

app.configure(function() {
    app.use(express.bodyParser());
});

app.get('/', function(req, res) {
    // show ejs with server parameter
    var htmlTemplate = fs.readFileSync('app.ejs', 'utf8');
    res.set('Content-Type', 'text/html');
    res.send(ejs.render(htmlTemplate, {
	server : req.protocol + '://' + req.host + ':' + port
    }));
});

app.get('/command', function(req, res) {
    // connector request recent command for primecoind
    res.set('Content-Type', 'text/html');
    res.send(command);
});

app.post('/result', function(req, res) {
    // connector send output from command
    io.sockets.emit('result', req.body);
    res.set('Content-Type', 'text/html');
    res.send('OK');
});

io.sockets.on('connection', function(socket) {
    // update command from web
    socket.on('sendCommand', function(data) {
	command = data;
    });
});

if (process.argv.length > 2) {
    port = process.argv[2];
}

server.listen(port);
console.log('web app started on port ' + port);
