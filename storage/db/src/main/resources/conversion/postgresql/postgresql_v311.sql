ALTER TABLE node_props ALTER prop_value TYPE VARCHAR(4000);
DROP INDEX node_props_prop_value_idx;
CREATE INDEX node_props_prop_value_idx ON node_props (substr(prop_value, 1, 255));
