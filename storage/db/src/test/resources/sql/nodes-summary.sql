UPDATE unique_ids SET current_id = 10000 WHERE index_type = 'general';

INSERT INTO binaries VALUES
('666b88fc2a043c2479a6de676a2f8179e9ea2777', '902a360ecad98a34b59863c1e65bcf71', 3),
('acab88fc2a043c2479a6de676a2f8179e9ea2167', '302a360ecad98a34b59863c1e65bcf71', 78),
('bbbb88fc2a043c2479a6de676a2f8179e9eabbbb', '402a360ecad98a34b59863c1e65bcf71', 33),
('dddd88fc2a043c2479a6de676a2f8179e9eadddd', '502a360ecad98a34b59863c1e65bcf71', 333),
('dddd88fc2a043c2479a6de676a2f7179e9eaddac', '502a360ecad98a34b59863c1e6accf71', 500),
('dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', 666);

INSERT INTO nodes VALUES
(1, 0, 'repo1', '.', '.', 0, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 0, NULL, NULL, NULL, NULL),
(2, 0, 'repo1', '.', 'ant', 1, 1340283204448, 'yossis', 1340283205448,'yossis', 1340283205448, 0, NULL, NULL, NULL, NULL),
(3, 0, 'repo1', 'ant', 'ant', 2, 1340283204450, 'yossis', 1340283204450,'yossis-3', 1340283214450, 0, NULL, NULL, NULL, NULL),
(4, 0, 'repo1', 'ant/ant', '1.5', 3, 1340283204448, 'yossis', 1340283204448,'yossis', 1340283204448, 0, NULL, NULL, NULL, NULL),
(5, 1, 'repo1', 'ant/ant/1.5', 'ant-1.5.jar', 4, 1340283204448, 'yossis', 1340283204448,'yossis', 1340283204448, 4, '666b88fc2a043c2479a6de676a2f8179e9ea2777', '666b88fc2a043c2479a6de676a2f8179e9ea2777', '902a360ecad98a34b59863c1e65bcf71', '902a360ecad98a34b59863c1e65bcf71'),
(6, 0, 'repo1', '.', 'org', 1, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 0, NULL, NULL, NULL, NULL),
(7, 0, 'repo1', 'org', 'yossis', 2, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 0, NULL, NULL, NULL, NULL),
(8, 0, 'repo1', 'org/yossis', 'tools', 3, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 0, NULL, NULL, NULL, NULL),
(9, 1, 'repo1', 'org/yossis/tools', 'test.bin', 4, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 1, 'acab88fc2a043c2479a6de676a2f8179e9ea2167', 'acab88fc2a043c2479a6de676a2f8179e9ea2167', '302a360ecad98a34b59863c1e65bcf71', '302a360ecad98a34b59863c1e65bcf71'),
(10, 1, 'repo1', 'org/yossis/tools', 'file2.bin', 4, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 2, 'bbbb88fc2a043c2479a6de676a2f8179e9eabbbb', 'bcab88fc2a043c2479a6de676a2f8179e9ea2167', '402a360ecad98a34b59863c1e65bcf71', '402a360ecad98a34b59863c1e65bcf71'),
(11, 1, 'repo1', 'org/yossis/tools', 'file3.bin', 4, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 3, 'dddd88fc2a043c2479a6de676a2f8179e9eadddd', 'ccab88fc2a043c2479a6de676a2f8179e9ea2167', '502a360ecad98a34b59863c1e65bcf71', '502a360ecad98a34b59863c1e65bcf71'),
(12, 0, 'repo2', '.', '.', 0, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 0, NULL, NULL, NULL, NULL),
(13, 0, 'repo2', '.', 'a', 1, 1340283204448, 'yossis', 1340283205448,'yossis', 1340283205448, 0, NULL, NULL, NULL, NULL),
(14, 0, 'repo2', 'a', 'b', 2, 1340283204450, 'yossis', 1340283204450,'yossis-3', 1340283214450, 0, NULL, NULL, NULL, NULL),
(15, 1, 'repo2', 'a', 'ant-1.5.jar', 4, 1340283204448, 'yossis', 1340283204448,'yossis', 1340283204448, 10, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71'),
(16, 1, 'repo2', 'a/b', 'ant-1.5.jar', 4, 1340283204448, 'yossis', 1340283204448,'yossis', 1340283204448, 20, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71'),
(17, 0, 'repo2', '.', 'aa', 1, 1340283204448, 'yossis', 1340283205448,'yossis', 1340283205448, 0, NULL, NULL, NULL, NULL),
(18, 0, 'repo2', 'aa', 'b', 2, 1340283204450, 'yossis', 1340283204450,'yossis', 1340283214450, 0, NULL, NULL, NULL, NULL),
(19, 1, 'repo2', 'aa', 'ant-1.5.jar', 4, 1340283204448, 'yossis', 1340283204448,'yossis', 1340283204448, 30, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71'),
(20, 1, 'repo2', 'aa/b', 'ant-1.5.jar', 4, 1340283204448, 'yossis', 1340283204448,'yossis', 1340283204448, 40, 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', 'dddd89fc2a043c2479a6de676a2f7179e9eaddac', '503a360ecad98a34b59863c1e6accf71', '503a360ecad98a34b59863c1e6accf71'),
(21, 0, 'repo3', '.', '.', 0, 1340283204448, 'yossis', 1340283204448, 'yossis', 1340283204448, 0, NULL, NULL, NULL, NULL);