CREATE TABLE user_props (
  user_id    NUMBER(19, 0) NOT NULL,
  prop_key   VARCHAR(64) NOT NULL,
  prop_value VARCHAR(2048),
  CONSTRAINT user_props_pk PRIMARY KEY (user_id, prop_key),
  CONSTRAINT user_props_users_fk FOREIGN KEY (user_id) REFERENCES users (user_id)
);
CREATE TABLE stats_remote (
  node_id            NUMBER(19, 0) NOT NULL,
  origin             VARCHAR2(64),
  download_count     NUMBER(19, 0),
  last_downloaded    NUMBER(19, 0),
  last_downloaded_by VARCHAR2(64),
  CONSTRAINT stats_remote_pk PRIMARY KEY (node_id),
  CONSTRAINT stats_remote_nodes_fk FOREIGN KEY (node_id) REFERENCES nodes (node_id)
);
