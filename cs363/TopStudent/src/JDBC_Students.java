import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;

/**
 * Project #2 
 * @author Tumsa Musa , James Eenhuis

 */
public class JDBC_Students {

	//set to true to see how fast this runs
	private static final boolean DEBUG = true;

	//optional database reset
	private static final boolean RESET = true;

	//set to appropriate filepaths
	private static final String person = "C:/Users/tmusa/Desktop/cs363//Person.xml";
	private static final String course = "C:/Users/tmusa/Desktop/cs363//Course.xml";
	private static final String instructor = "C:/Users/tmusa/Desktop/cs363//Instructor.xml";
	private static final String student = "C:/Users/tmusa/Desktop/cs363//Student.xml";
	private static final String offering = "C:/Users/tmusa/Desktop/cs363//Offering.xml";
	private static final String enrollment = "C:/Users/tmusa/Desktop/cs363//Enrollment.xml";


	public static void main(String[] args) {
		/*
		 * Solution is mostly SQL
		 */

		try {
			// Load the driver (registers itself)
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception E) {
			System.err.println("Unable to load driver.");
			E.printStackTrace();
		}
		try {
			// Connect to the database
			Connection conn;
			String dbUrl = "jdbc:mysql://csdb.cs.iastate.edu:3306/db363tmusa";
			String user = "dbu363tmusa";
			String password = "LxlU5136";
			conn = DriverManager.getConnection(dbUrl, user, password);
			System.out.println("*** Connected to the database ***");

			// Create Statement and ResultSet variables to use throughout the project
			Statement statement = conn.createStatement();
			ResultSet rsTop;

			//resets the database to origin state if flag is set
			long start = System.nanoTime();

			if(RESET) {
				reset(statement);
				statement.executeBatch();
			}

			start = (System.nanoTime() - start);
			if(DEBUG) 
				System.out.printf("\nReset the database in %s seconds!\n\n", ""+start/1000000000.0);

			//query string for top student
			String topQuery = queryTopStudent();

			start = System.nanoTime();

			statement.executeUpdate(update());

			rsTop = statement.executeQuery(topQuery);
			processTopStudents(rsTop);

			start = System.nanoTime() - start;
			if(DEBUG)
				System.out.printf("(Update + Query + Processing) Time: %s" ,start/1000000000.0);

			//cleans up
			statement.close();
			rsTop.close();
			conn.close();

		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		}
	}




	private static void processTopStudents(ResultSet rs) throws SQLException {
		String out = "";
		out+= String.format("%-20s| %-20s| %-20s\n", "Student", "Mentor", "GPA");
		double gpa = 0;
		while(rs.next()) {
			gpa = round(rs.getDouble(3));
			out+= String.format("%-20s| %-20s| %.2f\n", rs.getString(1), rs.getString(2), gpa);
		}

		if(DEBUG)
			System.out.println(out);

		try {
			File f = new File("JDBC_StudentsOutput.txt");
			FileWriter fw = new FileWriter(f);
			fw.write(out);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	public static double round(double x) {
		return Math.round(x*100.0)/100.0;
	}

	private static void reset(Statement s) throws SQLException {
		dropTables(s);
		createTables(s);
		loadTables(s);
	}
	
	/*
	 * The limit 4,1 takes the first record after the 4th row. 
	 * Guarantees all returned 
	 * records have a GPA >= the 5th best GPA
	 */
	private static String queryTopStudent() {
		return "select q.stdName, w.Name, q.GPA from (\r\n" + 
				"select distinct p.Name as stdName, s.MentorID as mentorID, s.GPA as GPA\r\n" + 
				"from \r\n" + 
				"Student s , Person p \r\n" + 
				"where p.ID = s.StudentID \r\n" + 
				"and s.Classification = 'Senior'\r\n" + 
				"and s.GPA >=\r\n" + 
				"(select p.GPA \r\n" + 
				"from Student p\r\n" + 
				"where p.Classification = 'Senior'\r\n" + 
				"order by p.GPA desc\r\n" + 
				"limit 4, 1) ) as q , Person w\r\n" + 
				"where w.ID = q.mentorID\r\n" + 
				"order by q.GPA desc ";
	}
	
	/*
	 * It's a bit of a mess but it works. 
	 * The case statement coverts the letter grades into number.
	 * runs much faster than the best java solution I could come up with. 
	 * probably not best sql solution
	 * 
	 * It updates the Student table's gpa, credit hours, and 
	 * classificaiton in one statement
	 */
	private static String update() {
		return "update Student q "
				
				//GPA update
				+ "set q.GPA = \r\n" + 
				"(select newGPA from \r\n" + 
				"( select StudentID, (SumGrade + OldGrade)/(CreditHours +3*CountGrade)  as newGPA from\r\n" + 
				"( select StudentID , sum(NumGrade) as SumGrade,  count(NumGrade) as CountGrade   from \r\n" + 
				"( select StudentID , \r\n" + 
				"3* (CASE when Grade = 'A' then 4 \r\n" + 
				"	when Grade = 'A-' then 3.66 \r\n" + 
				"	when Grade = 'B+' then 3.33 \r\n" + 
				"	when Grade = 'B' then 3.00 \r\n" + 
				"	when Grade = 'B-' then 2.66 \r\n" + 
				"	when Grade = 'C+' then 2.33 \r\n" + 
				"	when Grade = 'C' then 2.00 \r\n" + 
				"	when Grade = 'C-' then 1.66 \r\n" + 
				"	when Grade = 'D+' then 1.33 \r\n" + 
				"	when Grade = 'D' then 1.00 \r\n" + 
				"	else 0 end) as NumGrade from \r\n" + 
				"( select e.StudentID , e.Grade from Enrollment e) \r\n" + 
				"as b ) \r\n" + 
				"as c group by StudentID) \r\n" + 
				"as d,  (select s.StudentID as StdID, s.GPA*s.CreditHours  as OldGrade, CreditHours   from Student \r\n" + 
				"s) as f where StudentID = StdID ) as p where p.StudentID = q.StudentID)"+
				
				//credit hours update
				", q.CreditHours = q.CreditHours + 3 *(\r\n" + 
				"select count(e.StudentID) from Enrollment e\r\n" + 
				"where e.StudentID = q.StudentID)" +
				
				//classification update
				", q.Classification = CASE\r\n" + 
				"	when q.CreditHours < 30  then 'Freshman'\r\n" + 
				"    when q.CreditHours >29 \r\n" + 
				"		and q.CreditHours < 60  then 'Sophomore'\r\n" + 
				"	when q.CreditHours >59 \r\n" + 
				"		and q.CreditHours <90  then 'Junior'\r\n" + 
				"	else 'Senior'\r\n" + 
				"end";
	}

	private static void loadTables(Statement s) throws SQLException {
		s.addBatch(String.format("load xml local infile '%s' " + 
				"into table Person " + 
				"rows identified by '<Person>' ", person)); 

		s.addBatch(String.format("load xml local infile '%s' " + 
				"into table Course " + 
				"rows identified by '<Course>' " , course));

		s.addBatch(String.format("load xml local infile '%s' " + 
				"into table Instructor " + 
				"rows identified by '<Instructor>'  " , instructor)); 

		s.addBatch(String.format("load xml local infile '%s' " + 
				"into table Student " + 
				"rows identified by '<Student>'  ", student)) ; 

		s.addBatch(String.format("load xml local infile '%s' " + 
				"into table Offering " + 
				"rows identified by '<Offering>' ", offering));

		s.addBatch(String.format("load xml local infile '%s' " + 
				"into table Enrollment " + 
				"rows identified by '<Enrollment>' ", enrollment));

	}
	private static void createTables(Statement s) throws SQLException {
		s.addBatch("create table Person ( "+ 
				"Name char (20), " + 
				"ID char (9) not null, " + 
				"Address char (30), " + 
				"DOB date, " + 
				"Primary key (ID)) ");

		s.addBatch("create table Instructor ( " + 
				"InstructorID char(9) not null references Person(ID), " + 
				"Rank char(12), " + 
				"Salary int, " + 
				"primary key (InstructorID) " + 
				") ");
		
		s.addBatch(" create table Student ( " + 
				"StudentID char(9) not null references Person(ID), " + 
				"Classification char(10), " + 
				"GPA double, " + 
				"MentorID char(9) references Instructor(InstructorID) , " + 
				"CreditHours int, " + 
				"primary key (StudentID) " + 
				") " );
		
		s.addBatch("create table Course ( " + 
				"CourseCode char(6) not null, " + 
				"CourseName char(50), " + 
				"PreReq char(6), " + 
				"primary key (CourseCode, PreReq) " + 
				") ");
		
		s.addBatch( "create table Offering ( " + 
				"CourseCode char(6) not null, " + 
				"SectionNo int not null, " + 
				"InstructorID char(9) not null references Instructor(InstructorID) , " + 
				"primary key (CourseCode, SectionNo) " + 
				") ");
		s.addBatch("create table Enrollment ( " + 
				"CourseCode char(6) NOT NULL, " + 
				"SectionNo int NOT NULL, " + 
				"StudentID char(9) NOT NULL references Student, " + 
				"Grade char(4) NOT NULL, " + 
				"primary key (CourseCode, StudentID), " + 
				"foreign key (CourseCode, SectionNo) references Offering(CourseCode, SectionNo)) " ); 
		;
	}
	private static void dropTables(Statement s) throws SQLException {
		s.addBatch("drop table Enrollment ");  
		s.addBatch("drop table Offering ");
		s.addBatch("drop table Course ");
		s.addBatch("drop table Student "); 
		s.addBatch("drop table Instructor ");
		s.addBatch("drop table Person ");
	}
}

