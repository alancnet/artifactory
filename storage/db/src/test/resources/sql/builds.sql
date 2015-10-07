UPDATE unique_ids SET current_id = 10000 WHERE index_type = 'general';

INSERT INTO builds VALUES
(1, 'ba', '1', 1349000000000, NULL, 1350000000000, 'me', 1350000000001, 'not-me'),
(2, 'bb', '1', 1349001000000, 'http://myserver/jenkins/bb/1', 1350001000000, 'me', NULL, NULL),
(3, 'ba', '2', 1349002000000, NULL, 1350002000000, 'me', NULL, NULL),
(4, 'bb', '2', 1349003000000, 'http://myserver/jenkins/bb/2', 1350003000000, 'me', NULL, NULL),
(5, 'ba', '3', 1349004000000, NULL, 1350004000000, 'me', NULL, NULL);

INSERT INTO build_props VALUES
(1, 1, 'start', '0'),
(2, 1, 'status', 'bad'),
(3, 2, 'start', '1'),
(4, 2, 'status', 'not-too-bad'),
(5, 5, 'start', '4'),
(6, 5, 'status', 'good');

-- Inserting date in oposite order to check re-ordering
INSERT INTO build_promotions VALUES
(3, 1350012000000, 'me', 'dead', NULL, 'bad stuff', NULL),
(5, 1350034000000, 'promoter', 'Released', 'public', 'Full release', 'rel'),
(5, 1350024000000, NULL, 'staging', NULL, NULL, 'jenkins'),
(4, 1350003000000, NULL, 'staging', NULL, NULL, NULL),
(4, 1350023000000, 'tester', 'rollback', 'lost-local', 'Refused by QA', NULL),
(4, 1350013000000, 'promoter', 'promoted', 'qa-local', 'sending to QA', 'me');

INSERT INTO build_modules VALUES
  (1, 1, 'ba:moda1'),
  (2, 2, 'bb:modb1'),
  (3, 2, 'bb:modb2'),
  (4, 3, 'ba:moda1');

INSERT INTO build_artifacts VALUES
(2001, 1, 'ba1mod1-art1', 'dll', 'acab88fc2a043c2479a6de676a2f8179e9ea2167', 'a02a360ecad98a34b59863c1e65bcf71'),
(2002, 2, 'bb1mod2-art1', 'dll', 'acab88fc2a043c2479a6de676a2f8179e9ea2167', 'a02a360ecad98a34b59863c1e65bcf71'),
(2003, 3, 'bb1mod3-art1', 'dll', 'acab88fc2a043c2479a6de676a2f8179e9ea2167', 'a02a360ecad98a34b59863c1e65bcf71'),
(2004, 4, 'ba2mod4-art1', 'dll', 'acab88fc2a043c2479a6de676a2f8179e9ea2167', 'a02a360ecad98a34b59863c1e65bcf71'),
(2005, 1, 'ba1mod1-art2', 'dll', 'bcab88fc2a043c2479a6de676a2f8179e9ea2167', 'b02a360ecad98a34b59863c1e65bcf71'),
(2006, 4, 'ba2mod4-art1', 'dll', 'bcab88fc2a043c2479a6de676a2f8179e9ea2167', 'b02a360ecad98a34b59863c1e65bcf71');

INSERT INTO build_dependencies VALUES
(2010, 1, 'bb1mod1-art1', 'compile', 'dll', 'ccab88fc2a043c2479a6de676a2f8179e9ea2167', 'c02a360ecad98a34b59863c1e65bcf71'),
(2011, 2, 'bb1mod2-art1', 'compile', 'dll', 'ccab88fc2a043c2479a6de676a2f8179e9ea2167', 'c02a360ecad98a34b59863c1e65bcf71'),
(2012, 3, 'ba1mod3-art1', 'compile', 'dll', 'ccab88fc2a043c2479a6de676a2f8179e9ea2167', 'c02a360ecad98a34b59863c1e65bcf71'),
(2013, 4, 'ba2mod4-art1', 'compile', 'dll', 'dcab88fc2a043c2479a6de676a2f8179e9ea2167', 'd02a360ecad98a34b59863c1e65bcf71'),
(2014, 3, 'bb1mod3-art1', 'compile', 'dll', 'dcab88fc2a043c2479a6de676a2f8179e9ea2167', 'd02a360ecad98a34b59863c1e65bcf71');