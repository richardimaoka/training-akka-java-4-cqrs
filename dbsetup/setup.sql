CREATE DATABASE IF NOT EXISTS traininig_akka_java_4_cqrs;
USE traininig_akka_java_4_cqrs;

CREATE TABLE IF NOT EXISTS journal (
  ordering SERIAL,
  persistence_id VARCHAR(255) NOT NULL,
  sequence_number BIGINT NOT NULL,
  deleted BOOLEAN DEFAULT FALSE NOT NULL,
  tags VARCHAR(255) DEFAULT NULL,
  message BLOB NOT NULL,
  PRIMARY KEY(persistence_id, sequence_number)
);


CREATE TABLE IF NOT EXISTS snapshot (
  persistence_id VARCHAR(255) NOT NULL,
  sequence_number BIGINT NOT NULL,
  created BIGINT NOT NULL,
  snapshot BLOB NOT NULL,
  PRIMARY KEY (persistence_id, sequence_number)
);

CREATE TABLE ticket_stocks (
  `ticket_id` INT NOT NULL,
  `quantity` INT NOT NULL,
  PRIMARY KEY(`ticket_id`)
  -- FOREIGN KEY(`ticket_id`) REFERENCES tickets(`id`),
) ENGINE=InnoDB;

CREATE TABLE orders (
  `id` INT NOT NULL AUTO_INCREMENT,
  `ticket_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `quantity` INT NOT NULL,
  PRIMARY KEY(`id`),
  FOREIGN KEY(`ticket_id`) REFERENCES ticket_stocks(`ticket_id`),
  -- 本来ならticketsテーブルがあるはずで、以下のようになる
  -- FOREIGN KEY(`ticket_id`) REFERENCES tickets(`id`)
  -- また、usersテーブルもあるはず
  -- FOREIGN KEY(`user_id`) REFERENCES users(`id`)
) ENGINE=InnoDB;
