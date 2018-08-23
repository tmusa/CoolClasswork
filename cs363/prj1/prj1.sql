-- Tumsa M (tmusa@iastate.edu)
-- James E (Jeenhuis@iastate.edu)

-- PART a

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

-- Part b
-- Item 7
load xml local infile 'C:/Users/tmusa/Desktop/cs363//Person.xml'
into table Person
rows identified by '<Person>'; 

-- Item 8
load xml local infile 'C:/Users/tmusa/Desktop/cs363//Course.xml'
into table Course
rows identified by '<Course>'; 

-- Item 9
load xml local infile 'C:/Users/tmusa/Desktop/cs363//Instructor.xml'
into table Instructor
rows identified by '<Instructor>'; 

-- Item 10
load xml local infile 'C:/Users/tmusa/Desktop/cs363//Student.xml'
into table Student
rows identified by '<Student>'; 

-- Item 11
load xml local infile 'C:/Users/tmusa/Desktop/cs363//Offering.xml'
into table Offering
rows identified by '<Offering>'; 

-- Item 12
load xml local infile 'C:/Users/tmusa/Desktop/cs363//Enrollment.xml'
into table Enrollment
rows identified by '<Enrollment>'; 

-- PART c

-- Item 13 - ids of students & ids of mentors for students whoare junior or senior having gpa > 3.8
select StudentID, MentorID
from Student s 
where s.Classification in ('Junior', 'Senior')
and s.GPA > 3.8;

-- Item 14 - course code and sections for courses being taken by sophomores
select distinct CourseCode, SectionNo
from Enrollment e, Student s
where e.StudentID = s.StudentID 
and s.Classification = 'Sophomore';  

-- Item 15 - list name and salaries for all mentor of all freshman
select distinct p.Name, i.Salary 
from Person p , Instructor i
where p.ID = i.InstructorID
and  i.InstructorID in (
select MentorID 
from Student s
where s.Classification = 'Freshman'
);

-- Item 16 - Total salary of instructors not offering any course
select sum(s.Salary) as SalarySum
from Instructor s
where s.InstructorID not in (
select distinct i.InstructorID
from Offering i
);

-- Item 17 - tnames & dobsof students who were born in 1976
select p.Name , p.DOB 
from Person p
where Year(p.DOB) = 1976 
and p.ID in (
select s.StudentID 
from Student s 
);

-- Item 18 - name an drank of instructors that neirther offer a course nor mentor
select distinct n.Name, i.InstructorID
from Person n 
inner join Instructor i on n.ID= i.InstructorID
where i.InstructorID not in (select c.InstructorID
from Offering c
union all
select m.MentorID
from Student m);

-- Item 19 - ids, names, dobs of youngest student(s)
select p.Name, p.ID, p.DOB 
from Person p
inner join Student s on s.StudentID = p.ID
where p.DOB >= all (
select r.DOB 
from Person r
inner join Student q on q.StudentID = r.ID);

-- Item 20 - IDs, DOB, and Names of persons who aren't students or instructor
select u.Name, u.ID, u.DOB
from Person u
where u.ID not in (
select i.InstructorID
from Instructor i
union all
select o.StudentID
from Student o);

-- Item 21 - instructors & the number of students they mentor
select p.Name, count(s.StudentID)
from Person p 
inner join Student s on s.MentorID = p.ID
group by s.MentorID;

/* Item 21 but we actually bother to check that mentors are instructors
select p.Name, count(s.StudentID) 
from Student s ,Person p 
inner join Instructor i on i.InstructorID = p.ID 
where s.MentorID = i.InstructorID 
group by s.MentorID */

-- Item 22 - number of students & average GPA 
select count(a.StudentID) , avg(a.GPA)
from Student a
group by a.Classification;

-- Item 23 - report course(s) with lowest enrollments, coursecode & number
select e.CourseCode, count(e.StudentID) as Num
from Enrollment e
group by e.CourseCode
having Num <= all (select count(StudentID)
from Enrollment
group by CourseCode );

-- Item 24 - List IDs an dMentor ID of studentss who take courses offered by their mentor
/*select distinct e.StudentID, e.MentorID from 
(select s.StudentID as StudentID, o.InstructorID as InstructorID
from Enrollment s 
inner join Offering o on s.CourseCode = o.CourseCode) as t1
inner join Student e on e.StudentID = t1.StudentID
where t1.InstructorID = e.MentorID
order by e.StudentID;

select distinct s.StudentID, s.MentorID
from Student s
inner join Enrollment e on e.StudentID = s.StudentID
inner join Offering o on o.InstructorID = s.MentorID
where o.InstructorID = s.MentorID
and e.CourseCode = o.CourseCode
order by s.StudentID;*/

select distinct s.StudentID, s.MentorID
from Student s
inner join Enrollment e on e.StudentID = s.StudentID
inner join Offering o on o.CourseCode = e.CourseCode
where o.InstructorID = s.MentorID;


-- Item 25 
select s.StudentID, p.Name, s.CreditHours
from Student s
inner join Person p on p.ID = s.StudentID
where Year(p.DOB) >= 1976
and s.Classification = 'Freshman';

-- Item 26
insert into Person(Name, ID, Address, DOB)
values('Briggs Jason', 480293439, '215 North Hyland Avenue', DATE '1975-01-15');
insert into Student(StudentID, Classification, GPA, MentorID, CreditHours)
values(480293439, 'Junior', 3.48, 201586985, 75);
insert into Enrollment(CourseCode, SectionNo, StudentID, Grade)
values("CS311", 2, 480293439, 'A'),
('CS330', 1, 480293439, 'A-');

select * from Person p
where p.Name= 'Briggs Jason';

select * from Student s 
where s.StudentID=480293439;

select * from Enrollment e
where e.StudentID = 480293439;

-- Item 27
SET SQL_SAFE_UPDATES = 0;

delete from Enrollment
where StudentID in(
select s.StudentID from Student s
where s.GPA < 0.5);

delete from Student
where GPA < 0.5;

select * from Student s
where s.GPA < 0.5;

select * from Enrollment
where StudentID in(
select s.StudentID from Student s
where s.GPA < 0.5) order by StudentID;

-- Item 28 
Select P.Name, I.Salary
From Instructor I, Person P
Where I.InstructorID = P.ID
and P.Name = 'Ricky Ponting';

update Instructor
set Salary = Salary + (Salary * .1)
where 5 <= (
select count(distinct e.StudentID)
from Enrollment e 
where e.CourseCode in(
	select o.CourseCode
	from Offering o 
	where o.InstructorID = (
		select p.ID
		from Person p
		where p.Name = 'Ricky Ponting'
	)
)
and e.Grade = 'A'
);


Select P.Name, I.Salary
From Instructor I, Person P
Where I.InstructorID = P.ID
and P.Name = 'Ricky Ponting';

-- Item 29 
insert into Person(Name, ID, Address, DOB)
values ('Trevor Horns', '000957303', '23 Canerra Street',DATE'1964-11-23');

Select * from Person n
where n.Name = 'Trevor Horns';

/*
delete from Person 
where Person.Name = 'Trevor Horns';
*/

-- Item 30
Select * from Enrollment n
where n.StudentID = (Select s.ID
	from Person s
	where s.Name = 'Jan Austin');

Select * from Student n
where n.StudentID = (Select s.ID
	from Person s
	where s.Name = 'Jan Austin');

delete from Enrollment
where Enrollment.StudentID = (Select s.ID
	from Person s
	where s.Name = 'Jan Austin');

delete from Student
where Student.StudentID = (Select s.ID
	from Person s
	where s.Name = 'Jan Austin');
    
Select * from Enrollment n
where n.StudentID = (Select s.ID
	from Person s
	where s.Name = 'Jan Austin');

Select * from Student n
where n.StudentID = (Select s.ID
	from Person s
	where s.Name = 'Jan Austin');