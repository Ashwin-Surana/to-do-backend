TodoBackend
===========
A backend for [TodoBackend](http://www.todobackend.com) implemented in Java 8 with Vert.X. 

##Running Application Locally##
* Pull the source to a directory
* Navigate to the project directory and run the command `mvn package`
* Run the application - `java -Dhttp.port=8000 -jar target/to-do-list-1.0-SNAPSHOT-fat.jar`
* Go to [TodoBackend Test](http://todobackend.com/specs/index.html) and paste `http://localhost:8000/todo` and run the tests.
* If all the test pass, everything seems to be working fine.
* See the application in action by using the todobackend [client](http://www.todobackend.com/client/index.html).


##Deploying Application to Heroku##
* git clone https://github.com/Ashwin-Surana/to-do-backend
* heroku create
* mvn package heroku:deploy


