ALTER TABLE dynamic_content ADD CONSTRAINT dynamic_content_fk_users FOREIGN KEY (hochgeladenDurch_id) REFERENCES users(id);