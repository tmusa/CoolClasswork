-- drops all talbes for testing

drop table Enrollment;
drop table Offering;
drop table Course;
drop table Student;
drop table Instructor;
drop table Person;

-- Item 1 - Creates a person table (Name, ID, Address, DOB)
create table Person (
Name char (20),
ID char (9) not null,
Address char (30),
DOB date,
Primary key (ID));

-- Item 2 -Creates a Instructor table (InstructorID, Rank, Salary)
create table Instructor (
InstructorID char(9) not null references Person(ID),
Rank char(12),
Salary int,
primary key (InstructorID)
);

-- Item 3- Creates a Student table (StudentID, Classification, GPA, MentorID, CreditHours)
create table Student (
StudentID char(9) not null references Person(ID),
Classification char(10),
GPA double,
MentorID char(9) references Instructor(InstructorID) ,
CreditHours int,
primary key (StudentID)
);

-- Item 4- Creates a Course table (CourseCode, CourseName, PreReq)
create table Course (
CourseCode char(6) not null,
CourseName char(50),
PreReq char(6),
primary key (CourseCode, PreReq)
);

-- Item 5- Creates a Offering table (CourseCode, SectionNo, InstructorID)
create table Offering (
CourseCode char(6) not null,
SectionNo int not null,
InstructorID char(9) not null references Instructor(InstructorID) ,
primary key (CourseCode, SectionNo)
);

-- Item 6 -Creates a Enrollment table (CourseCode, SectionNo, StudentID, Grade)
create table Enrollment (
CourseCode char(6) NOT NULL,
SectionNo int NOT NULL,
StudentID char(9) NOT NULL references Student,
Grade char(4) NOT NULL,
primary key (CourseCode, StudentID),
foreign key (CourseCode, SectionNo) references Offering(CourseCode, SectionNo));

