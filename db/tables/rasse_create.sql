create table rasse (
	id integer not null auto_increment,
	description longtext,
	name varchar(255),
	personenNamenGenerator varchar(255),
	playable boolean not null,
	playableext boolean not null,
	schiffsKlassenNamenGenerator varchar(255),
	schiffsNamenGenerator varchar(255),
	head_id integer,
	memberIn_id integer,
	primary key (id)
) ENGINE=InnoDB;