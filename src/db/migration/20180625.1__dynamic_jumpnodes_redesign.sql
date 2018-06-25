delete from jumpnodes where id in (select jumpnode_id from dynamic_jumpnode);
drop table dynamic_jumpnode;
drop table dynamic_jn_config_zielsystems;
drop table dynamic_jn_config_startsystems;
drop table dynamic_jn_config;

create table dynamic_jn_config (id integer not null auto_increment, maxDistanceToInitialStart integer not null, maxLifetime integer not null, maxNextMovementDelay integer not null, minLifetime integer not null, minNextMovementDelay integer not null, maxDistanceToInitialTarget integer not null, primary key (id)) ENGINE=InnoDB;
create table dynamic_jumpnode (id integer not null auto_increment, dynamic_jn_config_id integer not null, initialTicksUntilMove integer not null, remainingTicksUntilMove integer not null, restdauer integer not null, jumpnode_id integer,  , primary key (id)) ENGINE=InnoDB;
alter table dynamic_jumpnode add index dynamic_jn_fk_jumpnodes (jumpnode_id), add constraint dynamic_jn_fk_jumpnodes foreign key (jumpnode_id) references jumpnodes (id);
alter table dynamic_jumpnode add index dynamic_jn_fk_dynamicjnconfig (dynamic_jn_config_id), add constraint dynamic_jn_fk_dynamicjnconfig foreign key (dynamic_jn_config_id) references dynamic_jn_config (id);