UPDATE unique_ids SET current_id = 10000 WHERE index_type = 'general';

INSERT INTO binaries VALUES
('dcab88fc2a043c2479a6de676a2f8179e9ea2167', '902a360ecad98a34b59863c1e65bcf71', 3),
('acab88fc2a043c2479a6de676a2f8179e9ea2167', '302a360ecad98a34b59863c1e65bcf71', 78),
('bbbb88fc2a043c2479a6de676a2f8179e9eabbbb', '402a360ecad98a34b59863c1e65bcf71', 33),
('dddd89fc2a043c2479a6de676a2f8179e9eadddd', '502a360ecad98a34b59863c1e65bcf71', 333);

INSERT INTO nodes VALUES
(1, 1, 'repo1', '.', 'a', 4, 1340283204448, 'jon', 1340283204448,'jon', 1340283204448, 716139, 'dcab88fc2a043c2479a6de676a2f8179e9ea2167', 'dcab88fc2a043c2479a6de676a2f8179e9ea2167', '902a360ecad98a34b59863c1e65bcf71', '902a360ecad98a34b59863c1e65bcf71'),
(2, 1, 'repo1', '.', 'b', 4, 1340283204448, 'jon', 1340283204448, 'jon', 1340283204448, 43434, 'acab88fc2a043c2479a6de676a2f8179e9ea2167', 'acab88fc2a043c2479a6de676a2f8179e9ea2167', '302a360ecad98a34b59863c1e65bcf71', '302a360ecad98a34b59863c1e65bcf71'),
(3, 1, 'repo1', '.', 'c', 4, 1340283204448, 'jon', 1340283204448, 'jon', 1340283204448, 43434, 'bbbb88fc2a043c2479a6de676a2f8179e9eabbbb', 'bcab88fc2a043c2479a6de676a2f8179e9ea2167', '402a360ecad98a34b59863c1e65bcf71', '402a360ecad98a34b59863c1e65bcf71'),
(4, 1, 'repo1', '.', 'd', 4, 1340283204448, 'jon', 1340283204448, 'jon', 1340283204448, 43434, 'dddd89fc2a043c2479a6de676a2f8179e9eadddd', 'dddd89fc2a043c2479a6de676a2f8179e9eadddd', '502a360ecad98a34b59863c1e65bcf71', '502a360ecad98a34b59863c1e65bcf71');

INSERT INTO node_props VALUES
(1, 1, 'license', 'GPL'),
(2, 1, 'version', '1.1.1'),
(3, 2, 'license', 'LGPL'),
(4, 2, 'version', '1.1.1'),
(5, 3, 'license', 'GPL'),
(6, 3, 'version', '1.1.1'),
(7, 4, 'license', 'GPL'),
(8, 4, 'version', '1.1.2');

INSERT INTO node_meta_infos VALUES
(1, 1340286103555, 'yossis'),
(2, 1340286803666, 'yoyo');

INSERT INTO watches VALUES
(1, 1, 'jon', 1340286203555),
(2, 2, 'jon', 1340286203666),
(3, 3, 'jon', 1340286203433),
(4, 4, 'jon', 1340286203433);

INSERT INTO stats VALUES
(1, 1, 1340283207850, 'jon'),
(2, 2, 1340283207850, 'jon'),
(3, 3, 1340283207850, 'jon'),
(4, 4, 1340283207850, 'jon');
