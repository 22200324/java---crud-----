package org.example;

import org.example.repository.WorkoutRecordDbRepository;
import org.example.repository.WorkoutRecordRepository;
import org.example.service.WorkoutRecordService;
import org.example.view.ConsoleView;

public class Main {
    public static void main(String[] args) {
        WorkoutRecordRepository repository = new WorkoutRecordDbRepository();
        WorkoutRecordService service = new WorkoutRecordService(repository);
        ConsoleView view = new ConsoleView(service);

        view.run();
    }
}
