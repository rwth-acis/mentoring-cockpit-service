Mentoring Cockpit Service
===========================================
This repository is part of the bachelor thesis 'A Multimodal Mentoring Cockpit for Tutor Support'.
The Mentoring Cockpit Service is a las2peer service, which enables the connection between Learning Locker and Mentoring Cockpit.
The service manages tutors' access rights and provides aggregated data for permitted Learning Management System (LMS) courses.
Other related repositories for the bachelor thesis can be found here: [Mentoring Cockpit](https://github.com/rwth-acis/Mentoring-Cockpit)

Database
---------
Access rights are stored in a MySQL database. You can find an sql file for setting up the database in the [etc](etc) folder. You'll have to fill and mange the database yourself. For example:

| SUB  | COURSELINK | COURSENAME |
| ------------- | ------------- | ------------- |
| tutor1-sub  | http://example-lms/example-course1 | Course1 |
| tutor1-sub  | http://example-lms/example-course2  | Course2 |
| tutor2-sub  | http://example-lms/example-course2  | Course2 |
|  ... | ...  | ... |


Learning Locker Aggregation HTTP interface
-------------------------------------
Statements in Learning Record Stores (LRS) are very rich and half of the information you don't need when displaying the data. Therefore, the [Learning Locker Aggregation HTTP interface](http://docs.learninglocker.net/http-aggregation) allows you to access MongoDB’s powerful Aggregation API for more custom filtration of statements.


Service setup
-------------
To set up the service configure the [property file](etc/i5.las2peer.services.mentoringCockpitService.MentoringCockService.properties) file with your database credentials and Learning Locker authentication.
```INI
lrsDomain = http://exampleDomain/api/statements/aggregate?
lrsAuth = Basic exampleauth
mysqlUser = exampleuser
mysqlPassword = examplepass
mysqlHost = localhost
mysqlPort = 3306
mysqlDatabase = exampledb
```

Build
--------
Execute the following command on your shell:

```shell
ant jar 
```

Start
--------

To start the moodle-data-proxy service, follow the [Starting-A-las2peer-Network tutorial](https://github.com/rwth-acis/las2peer-Template-Project/wiki/Starting-A-las2peer-Network).


Getting the data in the Front-end application
-----------------------

To get the student data to the Front-end, a RESTful GET requests are offered by the service. 

To get a JSON list of courses where a tutor has access rights:
```
GET <service-address>/mentoring/<tutor-sub>/courseList.
```

To get a JSON list of students enrolled in a course:
```
GET <service-address>/mentoring/<tutor-sub>/<encoded-courselink>/students.
```
 
To get a JSON list of students' results in a course:
```
GET <service-address>/mentoring/<tutor-sub>/<encoded-courselink>/results.
```

Therefore, replace *service-address* with your service address and *turor-sub* with the tutor sub specified in the MySQL database.



How to run using Docker
-------------------

First build the image:
```bash
docker build . -t mentoring-cockpit-service
```

Then you can run the image like this:

```bash
docker run -e LRS_DOMAIN=lrsDomain -e LRS_AUTH=lrsAuth -e MYSQL_USER=mysqlUser -e MYSQL_PASSWORD=mysqlPassword -e MYSQL_HOST=mysqlHost -e MYSQL_PORT=mysqlPort -e MYSQL_DATABASE=mysqlDatabase -p 9011:9011 mentoring-cockpit-service
```

Replace *lrsDomain* with your Learning Locker domain, *lrsAuth* with the corresponding authentication, *mysqlUser* with the MySQL user name, *mysqlPassword* with the MySQL password, *mysqlHost* with the MySQL host, *mysqlPort* with the MySQL port, and *mysqlDatabase* with the MySQL database name. 

*Do not forget to persist you database data*