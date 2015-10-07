INSERT INTO binaries VALUES
('b60d121b438a380c343d5ec3c2037564b82ffef3', '302a360ecad98a34b59863c1e65bcf71', 3),
('f0d381ab0e057d4f835d639f6330a7c3e81eb6af', '902a360ecad98a34b59863c1e65bcf71', 2725),
('74239116da1def240fe1d366eb535513efc1c40b', '402a360ecad98a34b59863c1e65bcf71', 33670080),
('356a192b7913b04c54574d18c28d46e6395428ab', '502a360ecad98a34b59863c1e65bcf71', 1),
('da39a3ee5e6b4b0d3255bfef95601890afd80709', '602a360ecad98a34b59863c1e65bcf71', 0);

INSERT INTO nodes VALUES
(1, 0, 'repo1', '.', '.', 0, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL),
(8, 0, 'repo1', '.', 'org', 1, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL),
(9, 0, 'repo1', 'org', 'yossis', 2, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL),
(10, 0, 'repo1', 'org/yossis', 'tools', 3, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL),
(11, 1, 'repo1', 'org/yossis/tools', 'test.bin', 4, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 43434, 'b60d121b438a380c343d5ec3c2037564b82ffef3', 'acab88fc2a043c2479a6de676a2f8179e9ea2167', '302a360ecad98a34b59863c1e65bcf71', '302a360ecad98a34b59863c1e65bcf71'),
(12, 1, 'repo1', 'org/yossis/tools', 'file2.bin', 4, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 43434, 'b60d121b438a380c343d5ec3c2037564b82ffef3', 'bcab88fc2a043c2479a6de676a2f8179e9ea2167', '302a360ecad98a34b59863c1e65bcf71', '402a360ecad98a34b59863c1e65bcf71'),
(13, 1, 'repo1', 'org/yossis/tools', 'file3.bin', 4, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 43434, 'f0d381ab0e057d4f835d639f6330a7c3e81eb6af', 'ccab88fc2a043c2479a6de676a2f8179e9ea2167', '902a360ecad98a34b59863c1e65bcf71', '502a360ecad98a34b59863c1e65bcf71'),
(14, 0, 'repo1', 'org/yossis', 'empty', 3, 1340283204448, 'yossis-1', 1340283204448, 'yossis-1', 1340283204448, 0, NULL, NULL, NULL, NULL);
