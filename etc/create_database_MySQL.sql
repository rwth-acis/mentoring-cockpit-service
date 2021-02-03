DROP TABLE IF EXISTS ACCESS;

CREATE TABLE ACCESS (
	COURSENAME VARCHAR(255),
	COURSELINK VARCHAR(255) NOT NULL,
	SUB VARCHAR(255) NOT NULL,
	PRIMARY KEY(SUB, COURSELINK)
);

DROP TABLE IF EXISTS moodle_lrs_mapping;

CREATE TABLE moodle_lrs_mapping (
  moodle_token varchar(255) NOT NULL,
  email varchar(255) NOT NULL,
  PRIMARY KEY (email)
);

DROP TABLE IF EXISTS resources;

CREATE TABLE resources (
  resourceid varchar(255) NOT NULL,
  name varchar(255) NOT NULL,
  url varchar(255) NOT NULL,
  type varchar(255) NOT NULL,
  PRIMARY KEY (resourceid)
);

DROP TABLE IF EXISTS course_themes;

CREATE TABLE course_themes (
  themeid varchar(255) NOT NULL,
  courseid varchar(255) NOT NULL,
  PRIMARY KEY (themeid, courseid)
);

DROP TABLE IF EXISTS theme_assignment;

CREATE TABLE theme_assignment (
  themeid varchar(255) NOT NULL,
  resourceid varchar(255) NOT NULL,
  detail_type varchar(255),
  detail_val varchar(255),
  PRIMARY KEY (themeid, resourceid, detail_type, detail_val)
);

INSERT INTO moodle_lrs_mapping VALUES ('MC Store', 'askabot@fakemail.de');
INSERT INTO ACCESS VALUES ('LMSBot', 'https://moodle.tech4comp.dbis.rwth-aachen.de/course/view.php?id=18', 'askabot');
INSERT INTO ACCESS VALUES ('LMSBot', 'https://moodle.tech4comp.dbis.rwth-aachen.de/course/view.php?id=20', 'askabot');