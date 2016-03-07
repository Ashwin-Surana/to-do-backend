TodoBackend
===========
A backend for [TodoBackend](http://www.todobackend.com) implemented in Java 8 with Vert.X. 

##Running Application Locally##
To run the application locally,
* Pull the source to a directory
* Run the application with the JVM option -Dhttp.port=8000
* Go to [TodoBackend Test](http://todobackend.com/specs/index.html) and paste http://localhost:8000/todo and run the tests.

##Deploying Application to Heroku##
* git clone https://github.com/Ashwin-Surana/to-do-backend
* heroku create
* mvn package heroku:deploy

