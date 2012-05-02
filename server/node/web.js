var http = require('http');
var server = http.createServer(
    function (request, response) {
        response.writeHead(200, {'Content-Type': 'text/html'});
        response.end('<html><head><title>httpd</title></head><body><h1>httpd</h1>       </body></html>');
    }
).listen(8124);
console.log('Server running at http://localhost:8124/');
