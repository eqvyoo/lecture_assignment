# 애플리케이션 실행 가이드

## 1. 필수 요구사항
### 1.1 소프트웨어 설치
- **Docker**: [Docker 설치 가이드](https://docs.docker.com/get-docker/)
- **Docker Compose**: Docker 설치 시 함께 제공됨.
- **Java 17 이상**: [Java 설치 가이드](https://adoptopenjdk.net/)
- **Gradle**: [Gradle 설치 가이드](https://gradle.org/install/)
- **Postman** (Optional): [Postman 설치 가이드](https://www.postman.com/downloads/)
- **IntelliJ** :[jetbrains](https://www.jetbrains.com/idea/)
---

## 2. 프로젝트 구조
```plaintext
/project-root
├── src/                    # 소스 코드
├── docker-compose.yml      # Docker Compose 설정 파일
├── redis/                  # Redis 관련 파일
│   └── data/               # Redis 데이터 저장 디렉토리
├── build.gradle            # Gradle 빌드 파일
├── application.yml         # 애플리케이션 설정
└── README.md               # 이 문서
```
## 3.GitHub 프로젝트 클론

IntelliJ를 사용하여 프로젝트를 GitHub에서 클론하는 방법:

1. IntelliJ를 실행하고 Welcome 화면에서 Get from VCS 버튼을 클릭합니다.

2. Version Control 창에서:

- Git을 선택합니다.

- URL 입력란에 ```https://github.com/eqvyoo/lecture-assignment``` 을 입력합니다.

- 로컬 저장 경로를 설정하고 Clone 버튼을 클릭합니다.

3. IntelliJ가 프로젝트를 가져오고 초기 설정을 자동으로 진행합니다.

4. Gradle을 사용 중이며, IntelliJ가 자동으로 프로젝트를 로드하며 필요한 의존성을 설치합니다.

## 4. IntelliJ 프로젝트 설정
(1) Java SDK 설정

1. 메뉴에서 File > Project Structure를 선택합니다.
2.	SDK 탭에서 Add SDK 버튼을 클릭하고 Java 17+ 버전을 선택합니다.

(2) Lombok 플러그인 설치

1.	File > Settings > Plugins로 이동합니다.
2. Marketplace에서 Lombok을 검색하고 설치한 뒤 IntelliJ를 재시작합니다.

(3) Gradle/Maven 프로젝트 동기화

1. IntelliJ가 자동으로 build.gradle파일을 읽고 의존성을 설치합니다.
2. 상단의 Refresh Gradle 프로젝트 버튼을 클릭하여 동기화 상태를 확인합니다.


## 5. Redis 실행
(1) docker-compose.yml 파일이 다음과 같은지 확인합니다
```plaintext
version: '3.8'
services:
  redis:
    image: redis:latest
    container_name: weolbu-cyj-redis-container
    ports:
      - "6379:6379"
    volumes:
      - weolbu-cyj-redis-data:/data
    command: ["redis-server", "--appendonly", "yes"]

volumes:
  weolbu-cyj-redis-data:
```
(2) IntelliJ에서 Docker 플러그인을 활성화:
1. File > Settings > Plugins에서 Docker 플러그인을 설치합니다.
2. Docker Compose를 IntelliJ에서 관리하려면 Services 탭에서 Docker를 설정합니다.


(3) Redis Container 실행
터미널을 열고 ``` docker-compose up -d``` 명령어를 실행합니다.

(4) Redis 상태 확인
``` docker ps``` 명령어를 통해 Redis가 정상적으로 실행 중인 지 확인합니다. 아래와 같은 형태라면 잘 실행 중입니다.
```
(base) mac@macui-MacBookPro-2 assignment % docker ps
CONTAINER ID   IMAGE          COMMAND                  CREATED         STATUS         PORTS                    NAMES
f75ea615ac98   redis:latest   "docker-entrypoint.s…"   6 seconds ago   Up 5 seconds   0.0.0.0:6379->6379/tcp   weolbu-cyj-redis-container
```

## 6. 프로젝트 실행
(1) 환경변수 준비

상단바의 Run > Edit Configuration 에서 Name : AssignmentApplication, Build and run에는 java 17이 설정되어 있는 지 확인합니다.

이후 Modify options > Environment variables 을 클릭한 후 + 버튼을 눌러 Name은 JWT_SECRET_KEY, Value는 원하는 비밀 키값을 넣습니다.
이때, 비밀 키의 값은 256 bits 이상이어야합니다.

이제 어플리케이션을 실행할 준비가 완료되었습니다!

(2) 프로젝트 실행

AssignmentApplication을 Run하여 프로젝트를 시작할 수 있습니다.

## 7. h2 console 접속
http://localhost:8080/h2-console/ 에 접속해 db에 연결해 데이터를 확인할 수 있습니다.

- Settings : Generic H2(Server)
- Setting Name : Generic H2(Server)
- Driver Class : org.h2.Driver
- JDBC URL : jdbc:h2:mem:testdb
- User Name : sa
- Password : 입력하지 않습니다.

connect 버튼을 눌러 h2에 접속할 수 있습니다.

```
예시
SELECT * FROM LECTURES 
SELECT * FROM USERS 
```

