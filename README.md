PrimeCoin-Remote-Manager
========================

#App Server(Node.js)
##Install
1. install node.js
2. cd ./server
3. npm install socket.io express ejs
4. sudo npm install -g supervisor

##Run
1. supervisor app.js or
2. supervisor -- app.js 4000 (port : 4000)

##Usage
1. connect **http://apphost:port/**


-----

#Connector(Java)
##Building
1. install maven
2. maven build with goals **"clean assembly:assembly -DdescriptorId=jar-with-dependencies"**(Make executable jar)

##Usage
*Run each connector with your primecoind. It communicate with Primecoin Remote Manager.*

1. Run your primecoind daemon("./primecoind -daemon")
2. for window just run **"connector.bat"** with options
3. for linux/osx run **"connector.sh"** with option
4. if you need help **"connector.sh -help"**

##Connector Example 
**./connector.sh -name node1 -sleep 10 -server http://localhost:3000 -path /home/primecoind**