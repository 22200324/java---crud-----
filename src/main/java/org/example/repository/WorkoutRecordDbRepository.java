package org.example.repository;

import org.example.exception.DataAccessException;
import org.example.model.WorkoutRecord;
import org.example.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkoutRecordDbRepository implements WorkoutRecordRepository {

    @Override
    public WorkoutRecord save(WorkoutRecord record) {
        String sql = """
                INSERT INTO workout_records
                (exercise_name, weight, reps, sets, workout_date, memo)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, record.getExerciseName());
            statement.setDouble(2, record.getWeight());
            statement.setInt(3, record.getReps());
            statement.setInt(4, record.getSets());
            statement.setDate(5, Date.valueOf(record.getWorkoutDate()));
            statement.setString(6, record.getMemo());

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new DataAccessException("운동 기록이 저장되지 않았습니다.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    record.setId(generatedKeys.getLong(1));
                } else {
                    throw new DataAccessException("저장된 운동 기록의 id를 가져오지 못했습니다.");
                }
            }

            return record;

        } catch (SQLException e) {
            throw new DataAccessException("운동 기록을 DB에 저장하지 못했습니다.", e);
        }
    }

    @Override
    public List<WorkoutRecord> findAll() {
        String sql = """
                SELECT id, exercise_name, weight, reps, sets, workout_date, memo
                FROM workout_records
                ORDER BY id
                """;

        List<WorkoutRecord> records = new ArrayList<>();

        try (
                Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                WorkoutRecord record = mapRowToWorkoutRecord(resultSet);
                records.add(record);
            }

            return records;

        } catch (SQLException e) {
            throw new DataAccessException("운동 기록을 DB에서 조회하지 못했습니다.", e);
        }
    }

    @Override
    public Optional<WorkoutRecord> findById(Long id) {
        String sql = """
                SELECT id, exercise_name, weight, reps, sets, workout_date, memo
                FROM workout_records
                WHERE id = ?
                """;

        try (
                Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    WorkoutRecord record = mapRowToWorkoutRecord(resultSet);
                    return Optional.of(record);
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new DataAccessException("운동 기록을 DB에서 조회하지 못했습니다.", e);
        }
    }

    @Override
    public List<WorkoutRecord> findByExerciseNameContaining(String keyword) {
        String sql = """
                SELECT id, exercise_name, weight, reps, sets, workout_date, memo
                FROM workout_records
                WHERE LOCATE(LOWER(?), LOWER(exercise_name)) > 0
                ORDER BY workout_date DESC, id DESC
                """;

        List<WorkoutRecord> records = new ArrayList<>();

        try (
                Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, keyword);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapRowToWorkoutRecord(resultSet));
                }
            }

            return records;

        } catch (SQLException e) {
            throw new DataAccessException("운동 이름으로 기록을 검색하지 못했습니다.", e);
        }
    }

    @Override
    public boolean update(WorkoutRecord record) {
        String sql = """
                UPDATE workout_records
                SET exercise_name = ?,
                    weight = ?,
                    reps = ?,
                    sets = ?,
                    workout_date = ?,
                    memo = ?
                WHERE id = ?
                """;

        try (
                Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, record.getExerciseName());
            statement.setDouble(2, record.getWeight());
            statement.setInt(3, record.getReps());
            statement.setInt(4, record.getSets());
            statement.setDate(5, Date.valueOf(record.getWorkoutDate()));
            statement.setString(6, record.getMemo());
            statement.setLong(7, record.getId());

            int result = statement.executeUpdate();

            return result > 0;

        } catch (SQLException e) {
            throw new DataAccessException("운동 기록을 DB에서 수정하지 못했습니다.", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = """
                DELETE FROM workout_records
                WHERE id = ?
                """;

        try (
                Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id);

            int result = statement.executeUpdate();

            return result > 0;

        } catch (SQLException e) {
            throw new DataAccessException("운동 기록을 DB에서 삭제하지 못했습니다.", e);
        }
    }

    private WorkoutRecord mapRowToWorkoutRecord(ResultSet resultSet) throws SQLException {
        return new WorkoutRecord(
                resultSet.getLong("id"),
                resultSet.getString("exercise_name"),
                resultSet.getDouble("weight"),
                resultSet.getInt("reps"),
                resultSet.getInt("sets"),
                resultSet.getDate("workout_date").toLocalDate(),
                resultSet.getString("memo")
        );
    }
}
