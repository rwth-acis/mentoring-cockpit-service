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

INSERT INTO moodle_lrs_mapping VALUES ('MC Store', 'askabot@fakemail.de');
INSERT INTO ACCESS VALUES ('LMSBot', 'https://moodle.tech4comp.dbis.rwth-aachen.de/course/view.php?id=18', 'askabot');