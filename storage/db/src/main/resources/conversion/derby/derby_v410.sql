CREATE TABLE user_props (
  user_id    BIGINT      NOT NULL,
  prop_key   VARCHAR(64) NOT NULL,
  prop_value VARCHAR(2048),
  CONSTRAINT user_props_pk PRIMARY KEY (user_id, prop_key),
  CONSTRAINT user_props_users_fk FOREIGN KEY (user_id) REFERENCES users (user_id)
);
CREATE TABLE stats_remote (
  node_id            BIGINT NOT NULL,
  origin             varchar(64),
  download_count     BIGINT,
  last_downloaded    BIGINT,
  last_downloaded_by VARCHAR(64),
  CONSTRAINT stats_remote_pk PRIMARY KEY (node_id),
  CONSTRAINT stats_remote_nodes_fk FOREIGN KEY (node_id) REFERENCES nodes (node_id)
);
