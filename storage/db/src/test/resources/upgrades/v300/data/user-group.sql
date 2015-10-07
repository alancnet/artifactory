UPDATE unique_ids SET current_id = 10000 WHERE index_type = 'general';

INSERT INTO users VALUES
(1, 'u1','apass',NULL,'e@mail.com',NULL,0,1,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(2, 'u2','bpass',NULL,'f@mail.com',NULL,1,1,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(3, 'u3',NULL,'','','',1,0,1,'','','',NULL,'',NULL,'',''),
(15, 'anonymous',NULL,NULL,NULL, NULL,0,1,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),
(16, 'admin', 'password',NULL,NULL, NULL,1,1,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);

INSERT INTO groups VALUES
(1, 'g1',NULL,0,NULL,NULL),
(2, 'g2','is default',1,'default realm','default att'),
(3, 'g3','no one',0,'artifactory',NULL),
(15, 'readers','readers',1,'artifactory',NULL);

INSERT INTO users_groups VALUES
(1,1,NULL),
(1,2,NULL),
(2,2,'ldap'),
(15,2,NULL),
(15,15,NULL);

