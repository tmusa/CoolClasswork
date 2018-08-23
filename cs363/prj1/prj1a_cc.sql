create table Person (
Name char (20),
ID char (9) not null,
Address char (30),
DOB date,
Primary key (ID));

create table Instructor (
InstructorID char(9) not null,
Rank char(12),
Salary int
);

create table Student (
StudentID char(9) not null,
Rank char(12),
Salary int,
Classification char(10),
GPA double,
MentorID char(9) ,
CreditHours int
);

create table Course (
CourseCode char(6) not null,
CourseName char(50),
PreReq char(6)
);

create table Offering (
CourseCode char(6) not null,
SectionNo int not null,
InstructorID char(9) not null
);

create table Enrollment (
CourseCode char(6) NOT NULL,
SectionNo int NOT NULL,
StudentID char(9) NOT NULL references Student,
Grade char(4) NOT NULL,
primary key (CourseCode, StudentID),
foreign key (CourseCode, SectionNo) references Offering(CourseCode, SectionNo));

drop table Person;