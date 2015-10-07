UPDATE unique_ids SET current_id = 10000 WHERE index_type = 'general';

INSERT INTO binaries VALUES
('dcab88fc2a043c2479a6de676a2f8179e9ea2167', '902a360ecad98a34b59863c1e65bcf71', 3),
('acab88fc2a043c2479a6de676a2f8179e9ea2167', '302a360ecad98a34b59863c1e65bcf71', 78),
('bbbb88fc2a043c2479a6de676a2f8179e9eabbbb', '402a360ecad98a34b59863c1e65bcf71', 33),
('dddd88fc2a043c2479a6de676a2f8179e9eadddd', '502a360ecad98a34b59863c1e65bcf71', 333),
('dddd88fc2a043c2479a6de676a2f7179e9eaddac', '502a360ecad98a34b59863c1e6accf71', 500),
('dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', 666);

INSERT INTO nodes VALUES
(1, 0, 'repo1', '.', '.', 0, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL),
(2, 0, 'repo1', '.', 'ant', 1, 1340283204448, 'yossis-1', 1340283205448,'yossis-2', 1340283205448, 0, NULL, NULL, NULL, NULL),
(3, 0, 'repo1', 'ant', 'ant', 2, 1340283204450, 'yossis-1', 1340283204450,'yossis-3', 1340283214450, 0, NULL, NULL, NULL, NULL),
(4, 0, 'repo1', 'ant/ant', '1.5', 3, 1340283204448, 'yossis-9614', 1340283204448,'yossis-5612', 1340283204448, 0, NULL, NULL, NULL, NULL),
(5, 1, 'repo1', 'ant/ant/1.5', 'ant-1.5.jar', 4, 1340283204448, 'yossis-2201', 1340283204448,'yossis-3274', 1340283204448, 716139, 'dcab88fc2a043c2479a6de676a2f8179e9ea2167', 'dcab88fc2a043c2479a6de676a2f8179e9ea2167', '902a360ecad98a34b59863c1e65bcf71', '902a360ecad98a34b59863c1e65bcf71'),
(6, 0, 'repo1', '.', 'ant-launcher', 1, 1340223204457, 'yossis-2', 1340283204448,'yossis-2', 1340283204448, 0, NULL, NULL, NULL, NULL),
(7, 0, 'repo1', 'ant-launcher', 'ant-launcher', 2, 1340223204457, 'yossis-2', 1340283204448,'yossis-2', 1340283204448, 0, NULL, NULL, NULL, NULL),
(8, 0, 'repo1', '.', 'org', 1, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL),
(9, 0, 'repo1', 'org', 'yossis', 2, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL),
(10, 0, 'repo1', 'org/yossis', 'tools', 3, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL),
(11, 1, 'repo1', 'org/yossis/tools', 'test.bin', 4, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 43434, 'acab88fc2a043c2479a6de676a2f8179e9ea2167', 'acab88fc2a043c2479a6de676a2f8179e9ea2167', '302a360ecad98a34b59863c1e65bcf71', '302a360ecad98a34b59863c1e65bcf71'),
(12, 1, 'repo1', 'org/yossis/tools', 'file2.bin', 4, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 43434, 'bbbb88fc2a043c2479a6de676a2f8179e9eabbbb', 'bcab88fc2a043c2479a6de676a2f8179e9ea2167', '402a360ecad98a34b59863c1e65bcf71', '402a360ecad98a34b59863c1e65bcf71'),
(13, 1, 'repo1', 'org/yossis/tools', 'file3.bin', 4, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 43434, 'dddd88fc2a043c2479a6de676a2f8179e9eadddd', 'ccab88fc2a043c2479a6de676a2f8179e9ea2167', '502a360ecad98a34b59863c1e65bcf71', '502a360ecad98a34b59863c1e65bcf71'),
(15, 1, 'repo-copy', 'org/yossis/tools', 'file3.bin', 4, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 43434, 'dddd88fc2a043c2479a6de676a2f8179e9eadddd', 'ccab88fc2a043c2479a6de676a2f8179e9ea2167', '502a360ecad98a34b59863c1e65bcf71', '502a360ecad98a34b59863c1e65bcf71'),
(14, 0, 'repo1', 'org/yossis', 'empty', 3, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL),
(16, 1, 'repo-copy', 'org/shayy/trustme', 'trustme.jar', 4, 1340283204447, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 43434, 'dddd88fc2a043c2479a6de676a2f7179e9eaddac', 'NO_ORIG', '502a360ecad98a34b59863c1e6accf71', 'NO_ORIG'),
(17, 1, 'repo-copy', 'org/shayy/badmd5', 'badmd5.jar', 4, 1340283204447, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 43434, 'dddd88fc2a043c2479a6de676a2f7179e9eaddac', 'NO_ORIG', '502a360ecad98a34b59863c1e6accf71', '502a360ecad98a34b59863c1e65bcf32'),
(18, 0, 'repo2', '.', '.', 0, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL),
(19, 0, 'repo2', '.', 'a', 1, 1340283204448, 'yossis-1', 1340283205448,'yossis-2', 1340283205448, 0, NULL, NULL, NULL, NULL),
(20, 0, 'repo2', 'a', 'b', 2, 1340283204450, 'yossis-1', 1340283204450,'yossis-3', 1340283214450, 0, NULL, NULL, NULL, NULL),
(21, 1, 'repo2', 'a', 'ant-1.5.jar', 4, 1340283204448, 'yossis-2201', 1340283204448,'yossis-3274', 1340283204448, 716139, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71'),
(22, 1, 'repo2', 'a/b', 'ant-1.5.jar', 4, 1340283204448, 'yossis-2201', 1340283204448,'yossis-3274', 1340283204448, 716139, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71'),
(23, 0, 'repo2', '.', 'aa', 1, 1340283204448, 'yossis-1', 1340283205448,'yossis-2', 1340283205448, 0, NULL, NULL, NULL, NULL),
(24, 0, 'repo2', 'aa', 'b', 2, 1340283204450, 'yossis-1', 1340283204450,'yossis-3', 1340283214450, 0, NULL, NULL, NULL, NULL),
(25, 1, 'repo2', 'aa', 'ant-1.5.jar', 4, 1340283204448, 'yossis-2201', 1340283204448,'yossis-3274', 1340283204448, 716139, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71'),
(26, 1, 'repo2', 'aa/b', 'ant-1.5.jar', 4, 1340283204448, 'yossis-2201', 1340283204448,'yossis-3274', 1340283204448, 716139, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71'),
(27, 0, 'repo3', '.', '.', 0, 1340283204448, 'admin', 1340283204448,'yossis-3274', 1340283204448, 716139, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71'),
(28, 0, 'repo3', '.', 'a', 1, 1340283204448, 'admin', 1340283204448,'yossis-3274', 1340283204448, 716139, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71'),
(29, 0, 'repo3', 'a', 'b', 2, 1340283204448, 'admin', 1340283204448,'yossis-3274', 1340283204448, 716139, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71'),
(30, 0, 'repo3', 'a/b', 'c', 3, 1340283204448, 'admin', 1340283204448,'yossis-3274', 1340283204448, 716139, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71'),
(31, 1, 'repo3', 'a/b/c', 'g.txt', 4, 1340283204448, 'admin', 1340283204448,'yossis-3274', 1340283204448, 716139, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71'),
(32, 1, 'repo3', 'a/B/C', 'f.txt', 4, 1340283204448, 'admin', 1340283204448,'yossis-3274', 1340283204448, 716139, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71'),
(33, 0, 'repo3', 'a/B', 'C', 4, 1340283204448, 'admin', 1340283204448,'yossis-3274', 1340283204448, 716139, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71'),
(34, 1, 'repo3', 'B', 'test.txt', 2, 1340283204448, 'admin', 1340283204448,'yossis-3274', 1340283204448, 716139, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71');

INSERT INTO node_props VALUES
(1, 5, 'build.name', 'ant'),
(2, 5, 'build.number', '67'),
(3, 9, 'yossis', 'value1'),
(4, 9, 'jungle', 'value2'),
(5, 9, 'trance', 'me'),
(6, 14, 'empty.val', ''),
(7, 14, 'null.val', NULL);

INSERT INTO node_meta_infos VALUES
(5, 1340286103555, 'yossis'),
(9, 1340286803666, 'yoyo');

INSERT INTO watches VALUES
(1, 4, 'scott', 1340286203555),
(2, 4, 'amy', 1340286203666),
(3, 5, 'scott', 1340286203555),
(4, 9, 'yossis', 1340286203432),
(5, 9, 'ariel', 1340286203433),
(6, 10, 'dodo', 1340286203433),
(7, 10, 'momo', 1340286203433),
(8, 11, 'momo', 1340286203433);

INSERT INTO stats VALUES
(6, 15, 1340283207850, 'yossis');

INSERT INTO indexed_archives VALUES
('dcab88fc2a043c2479a6de676a2f8179e9ea2167', 6000),
('bbbb88fc2a043c2479a6de676a2f8179e9eabbbb', 6001);

INSERT INTO archive_paths VALUES
(8001, 'META-INF'),
(8002, 'org/apache/tools/ant/filters'),
(8003, 'org/apache/tools/mail'),
(8004, '.'),
(8005, 'another');

INSERT INTO archive_names VALUES
(9001, 'LICENSE.txt'),
(9002, 'MANIFEST.MF'),
(9003, 'BaseFilterReader.class'),
(9004, 'BaseParamFilterReader.class'),
(9005, 'MailMessage.class'),
(9006, 'Test'),
(9007, 'test.me');

INSERT INTO indexed_archives_entries VALUES
(6000, 8001, 9001),
(6000, 8001, 9002),
(6000, 8002, 9003),
(6000, 8002, 9004),
(6000, 8003, 9005),
(6001, 8004, 9006),
(6001, 8005, 9007);

INSERT INTO tasks VALUES
('INDEX', 'repo1:ant/ant/1.5/ant-1.5.jar'),
('INDEX', 'reponone:test'),
('MMC', 'this/is/a/test');