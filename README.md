# Workout Record Console CRUD

Java를 공부하면서 만든 콘솔 기반 운동 기록 관리 프로젝트입니다.

이 프로젝트의 목표는 단순히 CRUD 기능을 완성하는 것뿐만 아니라, 하나의 요청이 `View → Service → Repository → 저장소`로 전달되는 흐름과 각 클래스가 역할을 나누는 이유를 직접 이해하는 것이었습니다.

Spring 같은 프레임워크는 사용하지 않았습니다. 순수 Java, JDBC, 파일 입출력만 사용하여 기본 동작을 직접 구현했습니다.

## 목차

1. [프로젝트에서 할 수 있는 일](#1-프로젝트에서-할-수-있는-일)
2. [사용 기술](#2-사용-기술)
3. [프로젝트 구조](#3-프로젝트-구조)
4. [각 계층의 역할](#4-각-계층의-역할)
5. [전체 실행 흐름](#5-전체-실행-흐름)
6. [파일 저장 방식](#6-파일-저장-방식)
7. [MariaDB 저장 방식](#7-mariadb-저장-방식)
8. [검색과 날짜 조회](#8-검색과-날짜-조회)
9. [통계](#9-통계)
10. [오류 처리](#10-오류-처리)
11. [실행 방법](#11-실행-방법)
12. [DB 초기화](#12-db-초기화)
13. [개발 순서](#13-개발-순서)
14. [이 프로젝트를 통해 배운 점](#14-이-프로젝트를-통해-배운-점)
15. [진행하면서 어려웠던 점과 해결 방법](#15-진행하면서-어려웠던-점과-해결-방법)
16. [현재 한계와 다음 학습 목표](#16-현재-한계와-다음-학습-목표)
17. [자주 발생할 수 있는 문제](#17-자주-발생할-수-있는-문제)

## 1. 프로젝트에서 할 수 있는 일

- 운동 기록 추가
- 전체 운동 기록 조회
- ID로 운동 기록 상세 조회
- 운동 기록 수정
- 운동 기록 삭제 및 삭제 전 확인
- 운동 이름 부분 검색
- 시작일과 종료일을 이용한 날짜 범위 조회
- 전체 운동 통계 조회
- 운동별 기록 수, 최대 무게, 총 세트 수, 누적 볼륨 확인
- 프로그램 실행 시 MariaDB 또는 파일 저장 방식 선택

프로그램을 실행하면 먼저 저장 방식을 선택합니다.

```text
===== 저장 방식 선택 =====
1. MariaDB
2. 파일(data/record.txt)
선택:
```

그다음 운동 기록 관리 메뉴가 표시됩니다.

```text
===== 운동 기록 관리 시스템 =====
1. 운동 기록 추가
2. 전체 기록 조회
3. 운동 기록 상세 조회
4. 운동 기록 수정
5. 운동 기록 삭제
6. 운동 이름으로 검색
7. 날짜 범위로 조회
8. 운동 통계
0. 종료
```

> MariaDB와 파일은 서로 다른 저장소입니다. 한 번의 실행에서는 선택한 저장소 하나만 사용하며, 두 저장소의 데이터가 자동으로 동기화되지는 않습니다.

## 2. 사용 기술

| 구분 | 사용 기술 | 사용 목적 |
|---|---|---|
| 언어 | Java 21 | 애플리케이션 구현 |
| 빌드 | Maven Wrapper | 동일한 Maven 버전으로 컴파일 |
| DB 연결 | JDBC | Java에서 MariaDB에 SQL 실행 |
| 데이터베이스 | MariaDB 12.3.2 | 운동 기록 영구 저장 |
| 로컬 DB 환경 | Docker Compose | MariaDB와 테이블을 쉽게 재현 |
| 파일 처리 | Java NIO | `record.txt` 읽기와 쓰기 |

사용하지 않은 기술:

- Spring / Spring Boot
- JPA / Hibernate
- Lombok
- 웹 서버 및 REST API

## 3. 프로젝트 구조

```text
untitled/
├─ db/
│  └─ init.sql                         # workout_records 테이블 생성 SQL
├─ data/
│  └─ record.txt                       # 파일 저장 모드의 실제 데이터
├─ src/main/java/org/example/
│  ├─ Main.java                        # 저장소 선택, 객체 조립, 프로그램 시작
│  ├─ exception/
│  │  └─ DataAccessException.java      # 파일 및 DB 처리 예외
│  ├─ model/
│  │  ├─ WorkoutRecord.java            # 운동 기록 한 건
│  │  ├─ WorkoutStatistics.java        # 전체 통계 결과
│  │  └─ ExerciseStatistics.java       # 운동별 통계 결과
│  ├─ repository/
│  │  ├─ WorkoutRecordRepository.java  # 저장소 공통 기능 정의
│  │  ├─ WorkoutRecordDbRepository.java
│  │  └─ WorkoutRecordFileRepository.java
│  ├─ service/
│  │  └─ WorkoutRecordService.java     # 입력 검증과 통계 계산
│  ├─ util/
│  │  └─ DBConnection.java             # DB 설정 로딩 및 Connection 생성
│  └─ view/
│     └─ ConsoleView.java              # 콘솔 입력, 메뉴, 결과 출력
├─ src/main/resources/
│  └─ db.properties.example            # DB 접속 설정 예시
├─ docker-compose.yml                  # MariaDB 실행 환경
├─ pom.xml                             # Java 버전과 JDBC 의존성
├─ mvnw                                # Linux/macOS용 Maven Wrapper
└─ mvnw.cmd                            # Windows용 Maven Wrapper
```

## 4. 각 계층의 역할

### Model

`WorkoutRecord`는 운동 기록 한 건을 표현합니다.

```text
ID
운동 이름
무게
반복 횟수
세트 수
운동 날짜
메모
```

운동 볼륨은 다음 공식으로 계산합니다.

```text
운동 볼륨 = 무게 × 반복 횟수 × 세트 수
```

이 계산은 운동 기록 자체의 정보만 사용하므로 `WorkoutRecord.calculateVolume()`에 두었습니다.

### Repository

Repository는 데이터를 저장하고 조회하는 역할을 담당합니다.

```java
WorkoutRecord save(WorkoutRecord record);
List<WorkoutRecord> findAll();
Optional<WorkoutRecord> findById(Long id);
boolean update(WorkoutRecord record);
boolean deleteById(Long id);
```

`WorkoutRecordRepository` 인터페이스에 필요한 기능을 먼저 정의하고, 저장 방식별 구현체를 만들었습니다.

```text
WorkoutRecordRepository
├─ WorkoutRecordDbRepository
└─ WorkoutRecordFileRepository
```

이 구조 덕분에 Service와 ConsoleView는 현재 저장 방식이 DB인지 파일인지 알 필요가 없습니다.

### Service

Service는 프로그램의 규칙을 담당합니다.

- 운동 이름이 비어 있는지 확인
- 무게가 0 이상인지 확인
- 반복 횟수와 세트 수가 1 이상인지 확인
- ID가 1 이상인지 확인
- 검색어가 비어 있는지 확인
- 시작 날짜가 종료 날짜보다 늦지 않았는지 확인
- 전체 및 운동별 통계 계산

입력 검증을 View나 Repository에 흩어놓지 않고 Service에 모아 동일한 규칙을 재사용할 수 있게 했습니다.

### View

`ConsoleView`는 사용자와 직접 대화합니다.

- 메뉴 출력
- 사용자 입력
- Service 호출
- 결과 출력
- 날짜 및 숫자 입력 재시도
- 삭제 전 `y/n` 확인

목록 출력과 상세 출력을 별도의 메서드로 나누어 모든 화면이 같은 형식을 사용합니다.

### Main

`Main`은 각 객체를 만들고 연결하는 시작점입니다.

```text
Scanner 생성
→ DB 또는 파일 Repository 선택
→ WorkoutRecordService 생성
→ ConsoleView 생성
→ view.run() 실행
```

새 기능의 실제 로직은 Main에 작성하지 않습니다. Main은 어떤 구현체를 사용할지 결정하고 객체를 조립하는 역할만 담당합니다.

## 5. 전체 실행 흐름

운동 기록을 추가할 때의 흐름입니다.

```text
사용자 입력
→ ConsoleView.addRecord()
→ WorkoutRecord 객체 생성
→ WorkoutRecordService.addRecord()
→ 입력값 검증
→ WorkoutRecordRepository.save()
→ 선택한 저장소에 저장
→ 저장 결과를 ConsoleView에서 출력
```

DB 모드를 선택했다면 다음 경로를 사용합니다.

```text
ConsoleView
→ WorkoutRecordService
→ WorkoutRecordDbRepository
→ DBConnection
→ MariaDB
```

파일 모드를 선택했다면 다음 경로를 사용합니다.

```text
ConsoleView
→ WorkoutRecordService
→ WorkoutRecordFileRepository
→ data/record.txt
```

## 6. 파일 저장 방식

파일 모드는 `data/record.txt`에 한 줄당 하나의 기록을 저장합니다.

```text
1|Bench Press|60.0|10|3|2026-07-04|컨디션 좋음
```

필드 순서:

```text
id | exerciseName | weight | reps | sets | workoutDate | memo
```

### 저장

새 기록은 파일 마지막에 추가합니다.

```text
기존 기록 조회
→ 가장 큰 ID 확인
→ 다음 ID 생성
→ 한 줄 문자열로 변환
→ 파일 마지막에 추가
```

### 수정과 삭제

텍스트 파일 중간의 가변 길이 문자열을 직접 수정하기는 어렵습니다. 따라서 다음 방식으로 구현했습니다.

```text
파일 전체 읽기
→ List<WorkoutRecord>로 변환
→ 리스트에서 수정 또는 삭제
→ 기존 파일 내용을 비움
→ 변경된 리스트 전체를 다시 저장
```

파일 입출력 문자셋은 운영체제 설정과 관계없이 한글이 유지되도록 UTF-8을 명시했습니다.

운동 이름이나 메모에 `|`가 포함되더라도 필드 구분자로 오해하지 않도록 `\|` 형태로 escaping합니다. 빈 메모도 정상적으로 저장하고 다시 읽을 수 있습니다.

## 7. MariaDB 저장 방식

DB Repository는 JDBC의 `PreparedStatement`를 사용합니다.

```text
Java 객체
→ SQL 작성
→ PreparedStatement에 값 설정
→ MariaDB 실행
→ ResultSet을 WorkoutRecord로 변환
```

`PreparedStatement`를 사용한 이유:

- 사용자 입력과 SQL 문장을 분리할 수 있음
- 문자열 연결 실수를 줄일 수 있음
- SQL Injection 위험을 줄일 수 있음
- 날짜와 숫자 타입을 명확하게 전달할 수 있음

Connection, PreparedStatement, ResultSet은 try-with-resources로 닫습니다.

```java
try (
    Connection connection = ...;
    PreparedStatement statement = ...
) {
    // SQL 실행
}
```

블록이 끝나면 자원이 자동으로 정리되므로 `close()` 호출을 빠뜨릴 가능성이 줄어듭니다.

## 8. 검색과 날짜 조회

### 운동 이름 검색

검색은 대소문자를 구분하지 않는 부분 일치 방식입니다.

```text
검색어: bench
검색 결과: Bench Press, Incline Bench Press
```

- 파일: 소문자로 변환한 뒤 `contains()` 사용
- DB: `LOCATE(LOWER(?), LOWER(exercise_name))` 사용

파일과 DB가 가능한 한 같은 검색 결과를 반환하도록 규칙을 맞췄습니다.

### 날짜 범위 조회

시작 날짜와 종료 날짜를 모두 포함합니다.

```text
시작 날짜 ≤ 운동 날짜 ≤ 종료 날짜
```

- 파일: `LocalDate.isBefore()`와 `isAfter()` 사용
- DB: SQL `BETWEEN` 사용

조회 결과는 운동 날짜 최신순, 같은 날짜에서는 ID가 큰 순서로 정렬됩니다.

## 9. 통계

Service에서 전체 기록을 가져온 뒤 Java Stream으로 계산합니다.

전체 통계:

- 전체 기록 수
- 전체 세트 수
- 전체 운동 볼륨

운동별 통계:

- 기록 수
- 최대 무게
- 총 세트 수
- 누적 운동 볼륨

통계는 파일과 DB에 공통으로 적용되는 프로그램 규칙이므로 Repository별로 중복 구현하지 않고 Service에서 한 번만 계산합니다.

`WorkoutStatistics`와 `ExerciseStatistics`는 계산 후 변경할 필요가 없는 결과 객체이므로 Java `record`로 만들었습니다.

## 10. 오류 처리

오류를 크게 두 종류로 구분했습니다.

```text
IllegalArgumentException
→ 사용자가 잘못된 값을 입력한 경우

DataAccessException
→ 파일 또는 DB 처리에 실패한 경우
```

ConsoleView의 메뉴 반복문에서 두 예외를 처리하므로 한 번의 잘못된 입력이나 DB 오류 때문에 프로그램 전체가 바로 종료되지 않습니다.

```text
입력 오류: 반복 횟수는 1 이상이어야 합니다.
데이터 처리 오류: 운동 기록을 DB에서 조회하지 못했습니다.
```

## 11. 실행 방법

### 준비물

- JDK 21 이상
- IntelliJ IDEA 또는 다른 Java IDE
- Docker Desktop: MariaDB 모드를 사용할 때만 필요

Maven은 별도로 설치하지 않아도 됩니다. 프로젝트에 포함된 Maven Wrapper가 필요한 Maven 버전을 준비합니다.

### 1) 프로젝트 열기

IntelliJ IDEA에서 프로젝트 루트 폴더를 엽니다.

```text
java-crud/untitled
```

Project SDK는 Java 21 이상으로 설정합니다.

### 2) 프로젝트 컴파일

Windows PowerShell:

```powershell
.\mvnw.cmd clean compile
```

macOS/Linux:

```bash
./mvnw clean compile
```

### 3-A) 파일 모드 실행

파일 모드는 Docker 없이 실행할 수 있습니다.

IntelliJ에서 다음 파일의 `main()` 메서드를 실행합니다.

```text
src/main/java/org/example/Main.java
```

저장 방식에서 `2`를 선택합니다.

```text
2. 파일(data/record.txt)
```

프로그램의 작업 디렉터리는 프로젝트 루트여야 `data/record.txt`를 올바르게 찾을 수 있습니다. IntelliJ의 기본 실행 설정에서는 일반적으로 프로젝트 루트가 사용됩니다.

### 3-B) MariaDB 모드 실행

DB 접속 설정 예시 파일을 실제 설정 파일로 복사합니다.

Windows PowerShell:

```powershell
Copy-Item `
  src/main/resources/db.properties.example `
  src/main/resources/db.properties
```

macOS/Linux:

```bash
cp src/main/resources/db.properties.example \
   src/main/resources/db.properties
```

MariaDB를 실행합니다.

```powershell
docker compose up -d
```

상태를 확인합니다.

```powershell
docker compose ps
```

정상적으로 준비되면 `healthy` 상태가 표시됩니다.

IntelliJ에서 `Main.java`를 실행하고 저장 방식 `1`을 선택합니다.

```text
1. MariaDB
```

MariaDB를 중지하려면 다음 명령을 사용합니다.

```powershell
docker compose down
```

> `docker compose down -v`는 DB 볼륨과 저장된 운동 기록까지 삭제합니다. 데이터를 유지하려면 `-v`를 붙이지 않습니다.

## 12. DB 초기화

Docker Compose는 `db/init.sql`을 컨테이너의 초기화 폴더에 연결합니다.

```text
db/init.sql
→ /docker-entrypoint-initdb.d/01-schema.sql
```

새로운 DB 볼륨이 처음 만들어질 때 다음 테이블이 자동 생성됩니다.

```sql
CREATE TABLE IF NOT EXISTS workout_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    exercise_name VARCHAR(100) NOT NULL,
    weight DOUBLE NOT NULL,
    reps INT NOT NULL,
    sets INT NOT NULL,
    workout_date DATE NOT NULL,
    memo TEXT NULL,
    PRIMARY KEY (id)
);
```

초기화 SQL은 기존 데이터 볼륨이 아니라 빈 볼륨의 최초 실행 시 적용됩니다.

## 13. 개발 순서

이 프로젝트는 다음 순서로 발전시켰습니다.

```text
1. 프로젝트 구조 생성
2. WorkoutRecord 모델 설계
3. Repository 인터페이스 정의
4. 파일 Repository CRUD 구현
5. Service 입력 검증 구현
6. ConsoleView 메뉴 구현
7. MariaDB 연결 및 DB Repository 구현
8. 파일/DB 저장 방식 선택 기능 추가
9. 기존 CRUD 안정화와 오류 처리
10. Docker DB 초기화 환경 구성
11. 운동 이름 검색 추가
12. 날짜 범위 조회 추가
13. 운동 통계 추가
14. 콘솔 출력 개선
```

기능을 추가할 때 사용한 공통 순서:

```text
필요한 기능 정의
→ Repository 계약 확인 또는 추가
→ 파일/DB 구현
→ Service 검증 및 규칙
→ ConsoleView 입력·출력
→ 두 저장 방식에서 실행 확인
```

## 14. 이 프로젝트를 통해 배운 점

### 역할을 나누는 이유

처음에는 한 클래스에 입력, 검증, 파일 처리, SQL을 모두 작성하는 것이 더 빠르게 느껴질 수 있었습니다. 하지만 기능이 늘어날수록 코드가 어디에 있어야 하는지 판단하기 어려워졌습니다.

Model, Repository, Service, View로 역할을 나누면서 다음 기준을 배웠습니다.

```text
데이터 자체의 정보와 계산 → Model
데이터 저장과 조회 → Repository
입력 검증과 프로그램 규칙 → Service
사용자 입력과 출력 → View
객체 생성과 연결 → Main
```

### 인터페이스의 실제 사용법

인터페이스가 단순히 메서드 이름만 선언하는 문법이 아니라, 서로 다른 구현을 같은 방식으로 사용할 수 있게 하는 계약이라는 점을 이해했습니다.

```java
WorkoutRecordRepository repository;

repository = new WorkoutRecordDbRepository();
// 또는
repository = new WorkoutRecordFileRepository();
```

Service 코드를 변경하지 않고 저장 방식만 교체할 수 있었습니다.

### CRUD의 전체 흐름

CRUD가 단순한 SQL 네 개가 아니라 다음 과정 전체라는 것을 배웠습니다.

```text
입력
→ 객체 생성
→ 값 검증
→ 저장소 호출
→ 결과 확인
→ 사용자에게 출력
```

### JDBC 자원 관리

Connection, PreparedStatement, ResultSet을 열었다면 반드시 닫아야 하며, try-with-resources를 사용하면 안전하게 정리할 수 있다는 점을 배웠습니다.

### 파일 저장과 DB 저장의 차이

DB에서는 조건에 맞는 행만 SQL로 조회하거나 수정할 수 있지만, 단순 텍스트 파일은 전체 내용을 읽고 다시 써야 하는 경우가 많았습니다.

같은 Repository 인터페이스를 구현하더라도 내부 동작은 저장 기술에 따라 크게 다를 수 있다는 점을 알게 됐습니다.

### 검증과 예외 처리

잘못된 값이 Repository까지 내려간 뒤 실패하게 두는 것보다 Service에서 먼저 의미 있는 메시지로 차단하는 것이 사용자와 개발자 모두에게 이해하기 쉽다는 점을 배웠습니다.

### 작은 기능도 계층 전체를 통과한다는 점

운동 이름 검색 하나를 추가해도 다음 계층을 모두 생각해야 했습니다.

```text
Repository 인터페이스
→ 파일 검색 구현
→ DB 검색 SQL
→ Service 검색어 검증
→ ConsoleView 메뉴와 출력
```

기능을 세로 방향으로 끝까지 연결하는 개발 흐름을 경험했습니다.

## 15. 진행하면서 어려웠던 점과 해결 방법

### 파일 수정과 삭제가 성공한 것처럼 보이지만 저장되지 않던 문제

초기 파일 Repository의 `writeAll()`이 비어 있어서 메모리의 리스트만 변경되고 실제 파일은 그대로였습니다.

해결 방법:

```text
전체 파일 읽기
→ 리스트 수정 또는 삭제
→ 파일을 비움
→ 전체 리스트 다시 저장
```

반환값만 확인하는 것이 아니라 실제 저장 결과까지 확인해야 한다는 점을 배웠습니다.

### 한글과 파일 인코딩

운영체제의 기본 문자셋에 의존하면 한글이 깨질 수 있었습니다. 파일을 읽고 쓸 때 `StandardCharsets.UTF_8`을 명시해 동작 환경에 따른 차이를 줄였습니다.

### 구분자가 데이터에 포함되는 문제

파일에서 `|`를 필드 구분자로 사용했기 때문에 운동 이름이나 메모에도 `|`가 들어가면 파싱이 깨질 수 있었습니다.

저장할 때 `|`와 `\`를 escaping하고, 읽을 때 다시 복원하도록 구현했습니다. 빈 메모도 마지막 필드로 유지되도록 처리했습니다.

### 수정할 때 운동 날짜가 오늘로 바뀌던 문제

초기 수정 기능은 날짜를 항상 `LocalDate.now()`로 만들었습니다. 운동 내용만 수정해도 원래 운동 날짜가 사라지는 문제가 있었습니다.

수정 화면에서 새 날짜를 입력받고, 엔터를 누르면 기존 날짜를 기본값으로 사용하도록 변경했습니다.

### 파일과 DB 검색 결과를 같게 만드는 문제

파일은 Java `contains()`를 사용하고 DB는 SQL을 사용하므로 대소문자나 `%`, `_` 같은 특수문자의 동작이 달라질 수 있었습니다.

파일에서는 소문자 변환 후 `contains()`를 사용하고, DB에서는 `LOCATE(LOWER(?), LOWER(exercise_name))`를 사용해 가능한 한 같은 부분 검색 규칙을 만들었습니다.

### DB 환경을 다른 컴퓨터에서 재현하는 문제

로컬 DB에 수동으로 테이블을 만든 상태에서는 다른 사람이 프로젝트를 실행할 수 없습니다.

Docker Compose와 `db/init.sql`을 추가해 다음 명령으로 같은 MariaDB와 테이블을 만들 수 있게 했습니다.

```powershell
docker compose up -d
```

### 사용자 출력과 디버깅 출력의 차이

`WorkoutRecord.toString()`은 개발자가 객체를 확인하기에는 편하지만 사용자가 읽기에는 불편했습니다.

목록용 `printRecordList()`와 상세용 `printRecordDetails()`를 View에 만들어 사용자 화면과 디버깅 표현을 분리했습니다.

## 16. 현재 한계와 다음 학습 목표

이 프로젝트는 콘솔 CRUD와 계층 분리를 익히기 위한 학습 프로젝트입니다. 실제 서비스 수준에서는 다음 부분을 더 발전시킬 수 있습니다.

- JUnit을 이용한 자동화 테스트
- CSV 내보내기
- 운동 이름과 날짜를 동시에 사용하는 복합 검색
- 데이터가 많을 때 사용할 페이지 단위 조회
- `double` 대신 `BigDecimal`을 이용한 정확한 소수 처리
- JDBC Connection Pool 적용
- 파일 저장 중 프로그램이 종료될 때를 대비한 임시 파일 및 원자적 교체
- Docker Compose 비밀번호를 `.env`로 분리
- 로깅 프레임워크 적용
- Spring Boot와 REST API로 확장

자동화 테스트는 현재 프로젝트 범위에서는 작성하지 않았습니다. 기능은 Maven 컴파일과 파일/DB 모드의 직접 실행으로 확인했습니다.

## 17. 자주 발생할 수 있는 문제

### `db.properties 파일을 찾을 수 없습니다`

예시 파일을 실제 설정 파일로 복사합니다.

```powershell
Copy-Item `
  src/main/resources/db.properties.example `
  src/main/resources/db.properties
```

### DB 연결에 실패함

컨테이너 상태를 확인합니다.

```powershell
docker compose ps
docker compose logs mariadb
```

`db.properties`의 URL, 사용자 이름, 비밀번호가 `docker-compose.yml`과 같은지 확인합니다.

### 3306 포트를 사용할 수 없음

다른 MariaDB 또는 MySQL이 이미 3306 포트를 사용 중일 수 있습니다. 기존 서비스를 중지하거나 Docker Compose의 호스트 포트를 변경해야 합니다.

```yaml
ports:
  - "3307:3306"
```

이 경우 JDBC URL도 함께 변경합니다.

```properties
db.url=jdbc:mariadb://localhost:3307/crud_db
```

### `init.sql`을 수정했지만 기존 DB에 반영되지 않음

MariaDB 공식 이미지의 초기화 SQL은 빈 데이터 볼륨의 최초 실행 시 적용됩니다. 기존 볼륨에는 자동으로 다시 적용되지 않습니다.

볼륨을 삭제하면 기존 데이터도 모두 사라지므로 필요한 데이터를 먼저 백업해야 합니다.

### 파일 모드에서 `record.txt`를 찾는 위치가 이상함

프로그램을 프로젝트 루트에서 실행했는지 확인합니다. 파일 경로는 현재 작업 디렉터리를 기준으로 `data/record.txt`를 사용합니다.

---

이 프로젝트는 작은 콘솔 프로그램이지만 모델 설계, 인터페이스, 다형성, 파일 입출력, JDBC, SQL, 예외 처리, Docker 환경 구성까지 Java 백엔드 개발의 기본 흐름을 직접 경험하는 것을 목표로 만들었습니다.
