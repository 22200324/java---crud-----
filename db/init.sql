CREATE TABLE IF NOT EXISTS workout_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    exercise_name VARCHAR(100) NOT NULL,
    weight DOUBLE NOT NULL,
    reps INT NOT NULL,
    sets INT NOT NULL,
    workout_date DATE NOT NULL,
    memo TEXT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET utf8mb4;
