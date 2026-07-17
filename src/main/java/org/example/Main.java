package org.example;

import org.example.repository.WorkoutRecordDbRepository;
import org.example.repository.WorkoutRecordFileRepository;
import org.example.repository.WorkoutRecordRepository;
import org.example.service.WorkoutRecordService;
import org.example.view.ConsoleView;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        WorkoutRecordRepository repository = selectRepository(scanner);
        WorkoutRecordService service = new WorkoutRecordService(repository);
        ConsoleView view = new ConsoleView(service, scanner);

        view.run();
    }

    private static WorkoutRecordRepository selectRepository(Scanner scanner) {
        while (true) {
            System.out.println("===== 저장 방식 선택 =====");
            System.out.println("1. MariaDB");
            System.out.println("2. 파일(data/record.txt)");
            System.out.print("선택: ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    System.out.println("MariaDB 저장 방식을 사용합니다.");
                    return new WorkoutRecordDbRepository();
                case "2":
                    System.out.println("파일 저장 방식을 사용합니다.");
                    return new WorkoutRecordFileRepository();
                default:
                    System.out.println("1 또는 2를 입력해주세요.");
                    System.out.println();
            }
        }
    }
}
