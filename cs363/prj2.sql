select distinct e.StudentID, count(e.StudentID), s.CreditHours
, s.Classification, s.GPA
from Enrollment e, Student s 
where e.StudentID = s.StudentID
group by e.StudentID;

select e.StudentID, e.Grade
from Enrollment e
order by e.StudentID;


select distinct s.StudentID, s.MentorID, s.GPA
from Student s order by s.GPA desc
;
update Student s
set s.GPA = 3, s.Classification = "Freshman", 
s.CreditHours = 0
where s.StudentID = 1;

update Student s 
set s.GPA = (GPA * s.CreditHours +
4.00 * 3 * (select count(e.StudentID) from Enrollment e 
where e.StudentID = s.StudentID and e.Grade ='A') +
3.66 * 3 * (select count(e.StudentID) from Enrollment e 
where e.StudentID = s.StudentID and e.Grade ='A-')	+
3.33 * 3 * (select count(e.StudentID) from Enrollment e 
where e.StudentID = s.StudentID and e.Grade ='B+') +
3.00 * 3 * (select count(e.StudentID) from Enrollment e 
where e.StudentID = s.StudentID and e.Grade ='B') +
2.66 * 3 * (select count(e.StudentID) from Enrollment e 
where e.StudentID = s.StudentID and e.Grade ='B-') +
2.33 * 3 * (select count(e.StudentID) from Enrollment e 
where e.StudentID = s.StudentID and e.Grade ='C+') +
2.00 * 3 * (select count(e.StudentID) from Enrollment e 
where e.StudentID = s.StudentID and e.Grade ='C') +
1.66 * 3 * (select count(e.StudentID) from Enrollment e 
where e.StudentID = s.StudentID and e.Grade ='C-') +
1.33 * 3 * (select count(e.StudentID) from Enrollment e 
where e.StudentID = s.StudentID and e.Grade ='D+') +
1.00 * 3 * (select count(e.StudentID) from Enrollment e 
where e.StudentID = s.StudentID and e.Grade ='D') )
/ (s.CreditHours + 3 *(
select count(e.StudentID) from Enrollment e
where e.StudentID = s.StudentID)
)
;
update Student q
set q.GPA = (select newGPA from (
select StudentID, (SumGrade + OldGrade)/(CreditHours +3*CountGrade) 
as newGPA
from(
select StudentID , sum(NumGrade) as SumGrade, 
count(NumGrade) as CountGrade  
from (
select StudentID , 3*
(CASE
when Grade = 'A' then 4
when Grade = 'A-' then 3.66
when Grade = 'B+' then 3.33
when Grade = 'B' then 3.00
when Grade = 'B-' then 2.66
when Grade = 'C+' then 2.33
when Grade = 'C' then 2.00
when Grade = 'C-' then 1.66
when Grade = 'D+' then 1.33
when Grade = 'D' then 1.00
else 0
end) as NumGrade
from (
select e.StudentID , e.Grade
from Enrollment e) as b ) as c
group by StudentID) as d, 
(select s.StudentID as StdID, s.GPA*s.CreditHours 
as OldGrade, CreditHours  
from Student s) as f where StudentID = StdID
) as p where p.StudentID = q.StudentID)
;
select e.StudentID , e.Grade
from Enrollment e;

update Student s 
set s.CreditHours = s.CreditHours + 3 *(
select count(e.StudentID) from Enrollment e
where e.StudentID = s.StudentID);

update Student s 
set s.Classification = CASE
	when s.CreditHours < 30  then 'Freshman'
    when s.CreditHours >29 
		and s.CreditHours < 60  then 'Sophomore'
	when s.CreditHours >59 
		and s.CreditHours <90  then 'Junior'
	else 'Senior'
end
;

select q.stdName, w.Name, q.GPA from (
select distinct p.Name as stdName, s.MentorID as mentorID, s.GPA as GPA
from 
Student s , Person p 
where p.ID = s.StudentID 
and s.Classification	 = 'Senior'
and s.GPA >=
(select p.GPA 
from Student p
where p.Classification = 'Senior'
order by p.GPA desc
limit 4, 1) ) as q , Person w
where w.ID = q.mentorID
order by q.GPA desc;

select distinct e.StudentID, count(e.StudentID), s.CreditHours,
s.Classification , s.GPA
from Enrollment e, Student s 
where e.StudentID = s.StudentID
group by e.StudentID;