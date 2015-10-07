UPDATE unique_ids SET current_id = 10000 WHERE index_type = 'general';

INSERT INTO build_modules VALUES
(1, 1, 'ba:moda1'),
(2, 2, 'bb:modb1'),
(3, 2, 'bb:modb2'),
(4, 3, 'ba:moda1'),
(5, 3, 'ba:moda2'),
(6, 4, 'bb:modb1'),
(7, 4, 'bb:modb2'),
(8, 5, 'ba:moda1'),
(9, 5, 'ba:moda2'),
(10, 5, 'ba:moda3');

INSERT INTO module_props VALUES
(1, 1, 'art-name', 'moda1'),
(2, 1, 'status', 'bad'),
(3, 1, 'dummy', 'dumm'),
(4, 2, 'art-name', 'modb1'),
(5, 2, 'status', 'not-too-bad'),
(6, 3, 'art-name', 'modb2'),
(7, 3, 'status', 'quite-good'),
(8, 8, 'art-name', 'moda1'),
(9, 8, 'status-moda1', 'good'),
(10, 9, 'art-name', 'moda2'),
(11, 9, 'status-moda2', 'good'),
(12, 10, 'art-name', 'moda3'),
(13, 10, 'status-moda3', 'good');

