UPDATE unique_ids SET current_id = 10000 WHERE index_type = 'general';

INSERT INTO permission_targets VALUES
(1, 'perm-target-1',NULL,NULL),
(2, 'perm-target-2','com/**,org/**',NULL),
(3, 'perm-target-3',NULL,'apache/**'),
(4, 'perm-target-4','jfrog/**,**/art-*.xml','codehaus/**');

INSERT INTO permission_target_repos VALUES
(1, 'ANY'),
(2, 'ANY LOCAL'),
(3, 'ANY REMOTE'),
(3, 'libs-release-local'),
(3, 'libs-snapshot-local');

INSERT INTO acls VALUES
(10,1,NULL,NULL),
(20,2,NULL,NULL),
(30,3,NULL,NULL);

INSERT INTO aces VALUES
(1,10,1,1,NULL),
(2,10,2,NULL,1),
(3,10,3,2,NULL),
(4,20,3,1,NULL),
(5,20,3,NULL,1);
