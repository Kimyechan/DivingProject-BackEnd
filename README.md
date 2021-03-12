# 프리 다이빙 강의 예약 프로젝트 
`프리 다이빙 강사와 수강생들을 온라인으로 쉽게 매칭시켜주는 모바일 웹 프로젝트`

## 사용 기술
- Application 개발
    - Java 11
    - Spring Boot
    - Spring Data JPA
    - Spring Security
    - Spring Cloud AWS
    - Spring HATEOAS
- Test
    - Junit5
- 문서화
    - Spring REST DOCS
    - asciidoctor
- DATABASE
    - Postgresql
    - Redis
    - H2
- 그 외
    - REST API
    - JWT

## ERD
![Untitled Diagram (2)](https://user-images.githubusercontent.com/12459864/110901937-688eb900-8348-11eb-9c13-0c93bc320e10.png)

- 각 테이블에 대한 설명
    - Account : 계정에 관련된 테이블로 강사, 수강생 정보 둘 다 포함한다
    - Instructor_Img : 강사 프로필 이미지에 대한 테이블
    - Account_Roles : 해당 계정 권한에 대한 테이블로 강사나 수강생 권한 혹은 둘 다 가질 수 있다
    - Lecture : 프리 다이빙 강의에 대한 테이블
    - Lecture_Image : 강의 이미지들에 대한 테이블
    - Equipment : 강의시 대여할 수 있는 장비에 대한 테이블
    - Schedule : 강의 일정에 대한 정보 (기간, 최대 인원)
    - Schedule_Detail : 강의 일정중 한 날짜에 대한 정보
    - Schedule_Time : 강의 일정 한 날짜중에 한 타임에 대한 정보
    - Reservation : 수강생이 예약한 강의에 대한 정보
    - Reservation_Date : 예약한 강의 일정 중 한 날짜에 대한 정보
    - Reservation_Equipments : 예약한 강의에서 대여한 장비 이름 정보

## 구현 기능 설명
- 계정 관련 기능
    - 회원 가입
    - 로그인
    - 로그아웃
    - 강사 권한 추가
- 강의 관련 기능
    - 강의 생성
    - 강의 상세 조회
    - 지역별 강의 리스트 조회
    - 강의 수정
    - 강의 삭제
- 강의 일정 관련 기능
    - 강의 일정 생성
    - 강의 일정 조회
- 강의 예약
    - 강의 예약하기


    
