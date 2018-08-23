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

