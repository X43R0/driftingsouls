ALTER TABLE ship_history ADD CONSTRAINT ship_history_fk_ships FOREIGN KEY (id) REFERENCES ships(id); 