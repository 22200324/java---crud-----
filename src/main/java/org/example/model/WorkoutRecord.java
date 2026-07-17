package org.example.model;

import java.time.LocalDate;

public class WorkoutRecord {
    private Long id;
    private String exerciseName;
    private double weight;
    private int reps;
    private int sets;
    private LocalDate workoutDate;
    private String memo;

    public WorkoutRecord() {
    }

    public WorkoutRecord(String exerciseName, double weight, int reps, int sets, LocalDate workoutDate, String memo) {
        this.exerciseName = exerciseName;
        this.weight = weight;
        this.reps = reps;
        this.sets = sets;
        this.workoutDate = workoutDate;
        this.memo = memo;
    }

    public WorkoutRecord(Long id, String exerciseName, double weight, int reps, int sets, LocalDate workoutDate, String memo) {
        this.id = id;
        this.exerciseName = exerciseName;
        this.weight = weight;
        this.reps = reps;
        this.sets = sets;
        this.workoutDate = workoutDate;
        this.memo = memo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public LocalDate getWorkoutDate() {
        return workoutDate;
    }

    public void setWorkoutDate(LocalDate workoutDate) {
        this.workoutDate = workoutDate;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public double calculateVolume() {
        return weight * reps * sets;
    }

    @Override
    public String toString() {
        return "WorkoutRecord{" +
                "id=" + id +
                ", exerciseName='" + exerciseName + '\'' +
                ", weight=" + weight +
                ", reps=" + reps +
                ", sets=" + sets +
                ", workoutDate=" + workoutDate +
                ", memo='" + memo + '\'' +
                '}';
    }
}
