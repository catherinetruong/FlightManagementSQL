/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class AirlineManagement {
   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of AirlineManagement
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public AirlineManagement(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end AirlineManagement

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            AirlineManagement.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      AirlineManagement esql = null;
      try {
         // use postgres JDBC driver.
         Class.forName("org.postgresql.Driver").newInstance();

         // instantiate the AirlineManagement object and creates a physical connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new AirlineManagement(dbname, dbport, user, "");

         boolean keepon = true;
         while (keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;

            switch (readChoice()) {
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default: System.out.println("Unrecognized choice!"); break;
            }//end switch

            if (authorisedUser != null) {
               String[] authParts = authorisedUser.split("\\|");
               String username = authParts[0];
               String role = authParts[1];

               boolean usermenu = true;
               while (usermenu) {
                  System.out.println("MAIN MENU");
                  System.out.println("---------");

                  //**the following functionalities should only be able to be used by Management**
                  System.out.println("1. View Flights");
                  System.out.println("2. View Flight Schedule");
                  System.out.println("3. View Flight Seats");
                  System.out.println("4. View Flight Status");
                  System.out.println("5. View Flights of the Day");
                  System.out.println("6. View Flight Passengers");
                  System.out.println("7. View Travelers Using Flight Reservation Number");
                  System.out.println("8. View Plane Stats");
                  System.out.println("9. View Technician Repairs");
                  System.out.println("10. View Flight Repairs");
                  System.out.println("11. View Flight Stats");
                  System.out.println(".........................");
                  System.out.println(".........................");

                  //**the following functionalities should only be able to be used by customers**
                  System.out.println("12. Search Flights");
                  System.out.println("13. View Flight Cost");
                  System.out.println("14. View Plane Type");
                  System.out.println("15. Make a Reservation");
                  System.out.println(".........................");
                  System.out.println(".........................");

                  //**the following functionalities should only be able to be used by Pilots**
                  System.out.println("16. Maintenance Request");
                  System.out.println(".........................");
                  System.out.println(".........................");

                  //**the following functionalities should only be able to be used by Technicians**
                  System.out.println("17. Show Repair Dates");
                  System.out.println("18. Show Maintenance Requests");
                  System.out.println("19. Log Repair");
                  System.out.println(".........................");
                  System.out.println(".........................");

                  System.out.println("20. Log out");

                  int choice = readChoice();
                  switch (choice) {
                     // Management-only features
                     case 1: if (role.equalsIgnoreCase("Management")) feature1(esql); else showDenied(); break;
                     case 2: if (role.equalsIgnoreCase("Management")) feature2(esql); else showDenied(); break;
                     case 3: if (role.equalsIgnoreCase("Management")) feature3(esql); else showDenied(); break;
                     case 4: if (role.equalsIgnoreCase("Management")) feature4(esql); else showDenied(); break;
                     case 5: if (role.equalsIgnoreCase("Management")) feature5(esql); else showDenied(); break;
                     case 6: if (role.equalsIgnoreCase("Management")) feature6(esql); else showDenied(); break;
                     case 7: if (role.equalsIgnoreCase("Management")) feature7(esql); else showDenied(); break;
                     case 8: if (role.equalsIgnoreCase("Management")) feature8(esql); else showDenied(); break;
                     case 9: if (role.equalsIgnoreCase("Management")) feature9(esql); else showDenied(); break;
                     case 10: if (role.equalsIgnoreCase("Management")) feature10(esql); else showDenied(); break;
                     case 11: if (role.equalsIgnoreCase("Management")) feature11(esql); else showDenied(); break;

                     // Customer-only features
                     case 12: if (role.equalsIgnoreCase("Customer")) feature12(esql); else showDenied(); break;
                     case 13: if (role.equalsIgnoreCase("Customer")) feature13(esql); else showDenied(); break;
                     case 14: if (role.equalsIgnoreCase("Customer")) feature14(esql); else showDenied(); break;
                     case 15: if (role.equalsIgnoreCase("Customer")) feature15(esql); else showDenied(); break;

                     // Pilot-only feature
                     case 16: if (role.equalsIgnoreCase("Pilot")) feature16(esql); else showDenied(); break;

                     // Technician-only features
                     case 17: if (role.equalsIgnoreCase("Technician")) feature17(esql); else showDenied(); break;
                     case 18: if (role.equalsIgnoreCase("Technician")) feature18(esql); else showDenied(); break;
                     case 19: if (role.equalsIgnoreCase("Technician")) feature19(esql); else showDenied(); break;

                     case 20: usermenu = false; break;
                     default: System.out.println("Unrecognized choice!"); break;
                  }//end switch
               }//end usermenu
            }//end if authorisedUser
         }//end while
      } catch (Exception e) {
         System.err.println(e.getMessage());
      } finally {
         // make sure to cleanup the created table and close the connection.
         try {
            if (esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup();
               System.out.println("Done\n\nBye !");
            }//end if
         } catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
    public static void CreateUser(AirlineManagement esql){
      try {
         System.out.print("Enter username: ");
         String login = in.readLine();
   
         System.out.print("Enter password: ");
         String password = in.readLine();
   
         System.out.print("Enter user role (Customer, Technician, Pilot, Management): ");
         String userType = in.readLine();
   
         String query = String.format(
            "INSERT INTO Users (login, password, userType) VALUES ('%s', '%s', '%s')",
            login, password, userType
         );
   
         esql.executeUpdate(query);
         System.out.println("User created successfully!");
      } catch(Exception e) {
         System.err.println("Error during user creation: " + e.getMessage());
      }
   } //end CreateUser
   


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
    public static String LogIn(AirlineManagement esql){
      try {
         System.out.print("Enter username: ");
         String login = in.readLine();
   
         System.out.print("Enter password: ");
         String password = in.readLine();
   
         String query = String.format(
            "SELECT userType FROM Users WHERE login = '%s' AND password = '%s'",
            login, password
         );
   
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         if (result.size() > 0) {
            String userType = result.get(0).get(0);
            System.out.println("Login successful. Role: " + userType);
            return login + "|" + userType;  
         } else {
            System.out.println("Login failed. Check your username/password.");
            return null;
         }
      } catch(Exception e) {
         System.err.println("Error during login: " + e.getMessage());
         return null;
      }
   }//end

   public static void showDenied() {
      System.out.println("Access denied. You do not have permission to perform this action.");
   }   

   // Rest of the functions definition go in here

   // management

   public static void feature1(AirlineManagement esql) {
      try {
         String query = "SELECT " +
                        "FlightNumber, " +
                        "PlaneID, " +
                        "DepartureCity, " +
                        "ArrivalCity " +
                        "FROM Flight;";
   
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
   
         System.out.printf("%-15s %-10s %-20s %-20s%n", "FlightNumber", "PlaneID", "DepartureCity", "ArrivalCity");
   
         for (List<String> row : results) {
            System.out.printf("%-15s %-10s %-20s %-20s%n", row.get(0), row.get(1), row.get(2), row.get(3));
         }
   
         System.out.println("total row(s): " + results.size());
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }
   

   // management #1
   public static void feature2(AirlineManagement esql) {
      try {
         System.out.print("\tEnter flight number: ");
         String input = in.readLine();
   
         String query = "SELECT " +
                        "    s.DayOfWeek, " +
                        "    s.DepartureTime, " +
                        "    s.ArrivalTime " +
                        "FROM Schedule s " +
                        "WHERE s.FlightNumber = '" + input + "' " +
                        "ORDER BY CASE s.DayOfWeek " +
                        "    WHEN 'Sunday' THEN 1 " +
                        "    WHEN 'Monday' THEN 2 " +
                        "    WHEN 'Tuesday' THEN 3 " +
                        "    WHEN 'Wednesday' THEN 4 " +
                        "    WHEN 'Thursday' THEN 5 " +
                        "    WHEN 'Friday' THEN 6 " +
                        "    WHEN 'Saturday' THEN 7 " +
                        "END;";
   
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
   
         System.out.printf("%-15s%-17s%-17s\n", "DayOfWeek", "DepartureTime", "ArrivalTime");
   
         for (List<String> row : results) {
            System.out.printf("%-15s%-17s%-17s\n", 
               row.get(0), row.get(1), row.get(2));
         }
   
         System.out.println("total row(s): " + results.size());
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }   

   // management #2
   public static void feature3(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Flight Number: ");
         String input = in.readLine();
         System.out.print("\tEnter Date (YYYY-MM-DD): ");
         String date = in.readLine();
   
         String query = "SELECT " +
                        "    fi.FlightNumber, " +
                        "    fi.FlightDate, " +
                        "    fi.SeatsSold, " +
                        "    (fi.SeatsTotal - fi.SeatsSold) AS SeatsAvailable " +
                        "FROM FlightInstance fi " +
                        "WHERE fi.FlightNumber = '" + input + "' " +
                        "AND fi.FlightDate = '" + date + "';";
   
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
   
         System.out.printf("%-15s%-15s%-15s%-17s\n", "FlightNumber", "FlightDate", "SeatsSold", "SeatsAvailable");
   
         for (List<String> row : results) {
            System.out.printf("%-15s%-15s%-15s%-17s\n", 
               row.get(0), row.get(1), row.get(2), row.get(3));
         }
   
         System.out.println("total row(s): " + results.size());
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }   

   // management #3
   public static void feature4(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Flight Number: ");
         String input = in.readLine();
         System.out.print("\tEnter Date (YYYY-MM-DD): ");
         String date = in.readLine();
   
         String query = "SELECT " +
                        "    f.FlightNumber, " +
                        "    fi.FlightDate, " +
                        "    fi.DepartedOnTime, " +
                        "    fi.ArrivedOnTime " +
                        "FROM FlightInstance fi " +
                        "JOIN Flight f ON fi.FlightNumber = f.FlightNumber " +
                        "WHERE f.FlightNumber = '" + input + "' " +
                        "AND fi.FlightDate = '" + date + "';";
   
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
   
         System.out.printf("%-15s%-15s%-18s%-18s\n", "FlightNumber", "FlightDate", "DepartedOnTime", "ArrivedOnTime");
   
         for (List<String> row : results) {
            System.out.printf("%-15s%-15s%-18s%-18s\n", 
               row.get(0), row.get(1), row.get(2), row.get(3));
         }
   
         System.out.println("total row(s): " + results.size());
   
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   

   // management #4
   public static void feature5(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Date (YYYY-MM-DD): ");
         String input = in.readLine();
   
         String query = "SELECT " +
                        "    fi.FlightInstanceID, " +
                        "    fi.FlightNumber, " +
                        "    fi.FlightDate, " +
                        "    f.DepartureCity, " +
                        "    f.ArrivalCity, " +
                        "    fi.DepartedOnTime, " +
                        "    fi.ArrivedOnTime " +
                        "FROM FlightInstance fi " +
                        "JOIN Flight f ON fi.FlightNumber = f.FlightNumber " +
                        "WHERE fi.FlightDate = '" + input + "' " +
                        "ORDER BY fi.FlightNumber;";
   
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
   
         // prints header with padding
         System.out.printf("%-18s%-15s%-15s%-18s%-18s%-18s%-18s\n", "FlightInstanceID", "FlightNumber", "FlightDate", "DepartureCity", "ArrivalCity", "DepartedOnTime", "ArrivedOnTime");
   
         // prints rows with matching padding
         for (List<String> row : results) {
            System.out.printf("%-18s%-15s%-15s%-18s%-18s%-18s%-18s\n",
               row.get(0), row.get(1), row.get(2),
               row.get(3), row.get(4), row.get(5), row.get(6));
         }
   
         System.out.println("total row(s): " + results.size());
   
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   

   // management #5
   public static void feature6(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Flight Number: ");
         String input = in.readLine();
         System.out.print("\tEnter Date (YYYY-MM-DD): ");
         String date = in.readLine();
  
         String query = "SELECT " +
                         "c.CustomerID, c.FirstName, c.LastName, r.Status, fi.FlightNumber, fi.FlightDate " +
                         "FROM Reservation r " +
                         "JOIN Customer c ON r.CustomerID = c.CustomerID " +
                         "JOIN FlightInstance fi ON r.FlightInstanceID = fi.FlightInstanceID " +
                         "WHERE fi.FlightNumber = '" + input + "' " +
                         "AND fi.FlightDate = '" + date + "' " +
                         "ORDER BY r.Status, c.LastName, c.FirstName;";
  
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
  
         System.out.printf("%-12s %-12s %-12s %-10s %-14s %-12s%n", "CustomerID", "FirstName", "LastName", "Status", "FlightNumber", "FlightDate");

         for (List<String> row : results) {
            System.out.printf("%-12s %-12s %-12s %-10s %-14s %-12s%n", row.get(0), row.get(1), row.get(2), row.get(3), row.get(4), row.get(5));
         }
  
         System.out.println("total row(s): " + results.size());
  
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
  }
  

   // management #6
   public static void feature7(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Reservation Number: ");
         String input = in.readLine();
  
         String query = "SELECT " +
                        "r.ReservationID, c.CustomerID, c.FirstName, c.LastName, c.Gender, " +
                        "c.DOB, c.Address, c.Phone, c.Zip, r.Status, r.FlightInstanceID " +
                        "FROM Reservation r " +
                        "JOIN Customer c ON r.CustomerID = c.CustomerID " +
                        "WHERE r.ReservationID = '" + input + "';";
  
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
  
         System.out.printf("%-15s %-12s %-12s %-12s %-8s %-12s %-20s %-15s %-8s %-10s %-15s%n", "ReservationID", "CustomerID", "FirstName", "LastName", "Gender", "DOB", "Address", "Phone", "Zip", "Status", "FlightInstanceID");
  
         for (List<String> row : results) {
            System.out.printf("%-15s %-12s %-12s %-12s %-8s %-12s %-20s %-15s %-8s %-10s %-15s%n", row.get(0), row.get(1), row.get(2), row.get(3), row.get(4), row.get(5), row.get(6), row.get(7), row.get(8), row.get(9), row.get(10));
         }
  
         System.out.println("total row(s): " + results.size());
  
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
  

   // management #7
   public static void feature8(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Plane Number: ");
         String input = in.readLine();
   
         String query = "SELECT " +
                        "PlaneID, Make, Model, LastRepairDate " +
                        "FROM Plane " +
                        "WHERE PlaneID = '" + input + "';";
   
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
   
         System.out.printf("%-10s %-15s %-15s %-15s%n", "PlaneID", "Make", "Model", "LastRepairDate");
   
         for (List<String> row : results) {
            System.out.printf("%-10s %-15s %-15s %-15s%n", row.get(0), row.get(1), row.get(2), row.get(3));
         }
   
         System.out.println("total row(s): " + results.size());
   
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }
   

   // management #8
   public static void feature9(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Technician ID: ");
         String input = in.readLine();
   
         String query = "SELECT " +
                        "r.RepairID, r.PlaneID, p.Make, p.Model, r.RepairCode, r.RepairDate " +
                        "FROM Repair r " +
                        "JOIN Plane p ON r.PlaneID = p.PlaneID " +
                        "WHERE r.TechnicianID = '" + input + "';";
   
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
   
         System.out.printf("%-10s %-10s %-15s %-15s %-12s %-15s%n", "RepairID", "PlaneID", "Make", "Model", "RepairCode", "RepairDate");
   
         for (List<String> row : results) {
            System.out.printf("%-10s %-10s %-15s %-15s %-12s %-15s%n", row.get(0), row.get(1), row.get(2), row.get(3), row.get(4), row.get(5));
         }
   
         System.out.println("total row(s): " + results.size());
   
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }
   

   // management #9
   public static void feature10(AirlineManagement esql) {
      try{
         System.out.print("\tEnter Plane ID: ");
         String input = in.readLine();
         System.out.print("\tEnter Start Date (YYY-MM-DD): ");
         String start = in.readLine();
         System.out.print("\tEnter End Date (YYYY-MM-DD): ");
         String end = in.readLine();
         String query = "SELECT \n" +
                        "    RepairDate,\n" +
                        "    RepairCode\n" +
                        "FROM \n" +
                        "    Repair\n" +
                        "WHERE \n" +
                        "    PlaneID = '" + input + "'\n" +
                        "    AND RepairDate BETWEEN '" + start + "' AND '" + end + "'\n" +
                        "ORDER BY \n" +
                        "    RepairDate;";

         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println ("total row(s): " + rowCount);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
   }

   // management #10
   public static void feature11(AirlineManagement esql) {
      try {
         System.out.print("\tEnter Flight Number: ");
         String input = in.readLine();
         System.out.print("\tEnter Start Date (YYYY-MM-DD): ");
         String start = in.readLine();
         System.out.print("\tEnter End Date (YYYY-MM-DD): ");
         String end = in.readLine();
   
         String query = "SELECT " +
                        "COUNT(*) AS NumDays, " +
                        "SUM(SeatsSold) AS TotalTicketsSold, " +
                        "SUM(SeatsTotal - SeatsSold) AS TotalTicketsUnsold " +
                        "FROM FlightInstance " +
                        "WHERE FlightNumber = '" + input + "' " +
                        "AND FlightDate BETWEEN '" + start + "' AND '" + end + "';";
   
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
   
         System.out.printf("%-10s %-20s %-20s%n", "NumDays", "TotalTicketsSold", "TotalTicketsUnsold");
   
         for (List<String> row : results) {
            System.out.printf("%-10s %-20s %-20s%n", row.get(0), row.get(1), row.get(2));
         }
   
         System.out.println("total row(s): " + results.size());

      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }   

   // customer

   // customer #1
   public static void feature12(AirlineManagement esql) {
      try {
         System.out.print("\tEnter departure city: ");
         String departure = in.readLine().trim();
         System.out.print("\tEnter destination city: ");
         String destination = in.readLine().trim();
         System.out.print("\tEnter date (YYYY-MM-DD): ");
         String date = in.readLine().trim();
   
         String query = 
            "SELECT f.FlightNumber, " +
            "       s.DepartureTime, s.ArrivalTime, fi.NumOfStops, " +
            "       ROUND(AVG(CASE WHEN fi.DepartedOnTime THEN 1 ELSE 0 END) * 100, 2) AS OnTimeDeparturePercentage, " +
            "       ROUND(AVG(CASE WHEN fi.ArrivedOnTime THEN 1 ELSE 0 END) * 100, 2) AS OnTimeArrivalPercentage " +
            "FROM Flight f " +
            "JOIN FlightInstance fi ON f.FlightNumber = fi.FlightNumber " +
            "JOIN Schedule s ON f.FlightNumber = s.FlightNumber " +
            "WHERE f.DepartureCity = '" + departure + "' " +
            "AND f.ArrivalCity = '" + destination + "' " +
            "AND TRIM(TO_CHAR(DATE '" + date + "', 'Day')) = s.DayOfWeek " +
            "GROUP BY f.FlightNumber, s.DepartureTime, s.ArrivalTime, fi.NumOfStops;";
   
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
   
         System.out.printf("%-15s %-15s %-15s %-15s %-25s %-25s%n",
            "FlightNumber", "DepartureTime", "ArrivalTime", "NumOfStops", "OnTimeDeparture(%)", "OnTimeArrival(%)");
   
         for (List<String> row : results) {
            System.out.printf("%-15s %-15s %-15s %-15s %-25s %-25s%n", row.get(0), row.get(1), row.get(2), row.get(3), row.get(4), row.get(5));
         }
   
         System.out.println("total row(s): " + results.size());
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   // customer #2
   public static void feature13(AirlineManagement esql) {
      try {
         System.out.print("\tEnter flight number: ");
         String flightNumber = in.readLine().trim();
  
         String query = "SELECT TicketCost FROM FlightInstance WHERE FlightNumber = '" + flightNumber + "';";
  
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("total row(s): " + rowCount);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
  }  
   
   // customer #3
   public static void feature14(AirlineManagement esql) {
      try {
         System.out.print("\tEnter flight number: ");
         String input = in.readLine().trim();
   
         String query = "SELECT p.Make, p.Model " +
                        "FROM Flight f, Plane p " +
                        "WHERE f.PlaneID = p.PlaneID AND f.FlightNumber = '" + input + "';";
   
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
   
         System.out.printf("%-20s %-20s%n", "Make", "Model");
   
         for (List<String> row : results) {
            System.out.printf("%-20s %-20s%n", row.get(0), row.get(1));
         }
   
         System.out.println("total row(s): " + results.size());
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   
   
   // customer #4
   public static void feature15(AirlineManagement esql) {
      try {
         System.out.print("Enter flight number: ");
         String flightNumber = in.readLine();
   
         System.out.print("Enter flight date (YYYY-MM-DD): ");
         String flightDate = in.readLine();
   
         System.out.print("Enter customer ID: ");
         String customerID = in.readLine();
   
         // check if the flight instance exists
         String checkFlightQuery = String.format(
            "SELECT FlightInstanceID, SeatsTotal, SeatsSold FROM FlightInstance WHERE FlightNumber = '%s' AND FlightDate = DATE '%s'",
            flightNumber, flightDate
         );
         List<List<String>> flightData = esql.executeQueryAndReturnResult(checkFlightQuery);
   
         if (flightData.isEmpty()) {
            System.out.println("Flight instance not found.");
            return;
         }
   
         int flightInstanceID = Integer.parseInt(flightData.get(0).get(0));
         int seatsTotal = Integer.parseInt(flightData.get(0).get(1));
         int seatsSold = Integer.parseInt(flightData.get(0).get(2));
   
         String status;
         if (seatsSold < seatsTotal) {
            status = "reserved";
   
            // update seats sold
            String updateSeatsQuery = String.format(
               "UPDATE FlightInstance SET SeatsSold = SeatsSold + 1 WHERE FlightInstanceID = %d",
               flightInstanceID
            );
            esql.executeUpdate(updateSeatsQuery);
         } else {
            status = "waitlist";
         }
   
         String reservationID = "R" + System.currentTimeMillis();
   
         // insert the reservation
         String insertReservationQuery = String.format(
            "INSERT INTO Reservation (ReservationID, CustomerID, FlightInstanceID, Status) " +
            "VALUES ('%s', %s, %d, '%s')",
            reservationID, customerID, flightInstanceID, status
         );
         esql.executeUpdate(insertReservationQuery);
   
         System.out.printf("Reservation made with status: %s\n", status);
   
      } catch (Exception e) {
         System.err.println("Error making reservation: " + e.getMessage());
      }
   }
   
   // pilot

   //pilot #1
   public static void feature16(AirlineManagement esql) {
      try {
         System.out.print("\tEnter your Pilot ID: ");
         String pilotID = in.readLine().trim();
         System.out.print("\tEnter plane ID: ");
         String planeID = in.readLine().trim();
         System.out.print("\tEnter repair code: ");
         String repairCode = in.readLine().trim();
         System.out.print("\tEnter date of request (YYYY-MM-DD): ");
         String date = in.readLine().trim();
   
         String getMaxIdQuery = "SELECT MAX(RequestID) FROM MaintenanceRequest;";
         List<List<String>> result = esql.executeQueryAndReturnResult(getMaxIdQuery);
         int newRequestID = (result.get(0).get(0) != null) ? Integer.parseInt(result.get(0).get(0)) + 1 : 1;
   
         String insertQuery = String.format(
            "INSERT INTO MaintenanceRequest (RequestID, PlaneID, RepairCode, RequestDate, PilotID) " +
            "VALUES (%d, '%s', '%s', DATE '%s', '%s');",
            newRequestID, planeID, repairCode, date, pilotID
         );
         esql.executeUpdate(insertQuery);
   
         String confirmQuery = String.format(
            "SELECT PlaneID, RepairCode, RequestDate FROM MaintenanceRequest WHERE RequestID = %d;",
            newRequestID
         );
   
         List<List<String>> confirmResults = esql.executeQueryAndReturnResult(confirmQuery);
   
         System.out.printf("%-12s%-15s%-15s\n", "PlaneID", "RepairCode", "RequestDate");
   
         for (List<String> row : confirmResults) {
            System.out.printf("%-12s%-15s%-15s\n", row.get(0), row.get(1), row.get(2));
         }
   
         System.out.println("Maintenance request submitted.");
         System.out.println("Total rows: " + confirmResults.size());
   
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }   
   
   // technician

   //technician #1
   public static void feature17(AirlineManagement esql) {
      try {
         System.out.print("\tEnter plane ID: ");
         String planeID = in.readLine().trim();
   
         System.out.print("\tEnter start date (YYYY-MM-DD): ");
         String startDate = in.readLine().trim();
   
         System.out.print("\tEnter end date (YYYY-MM-DD): ");
         String endDate = in.readLine().trim();
   
         String query = String.format(
            "SELECT RepairDate, RepairCode " +
            "FROM Repair " +
            "WHERE PlaneID = '%s' " +
            "AND RepairDate BETWEEN DATE '%s' AND DATE '%s';",
            planeID, startDate, endDate
         );
   
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("Total row(s): " + rowCount);
      } catch(Exception e) {
         System.err.println("Error: " + e.getMessage());
      }
   }
   
   
   //technician #2
   public static void feature18(AirlineManagement esql) {
      try {
         System.out.print("\tEnter pilot ID: ");
         String pilotID = in.readLine().trim();
   
         String query = "SELECT RequestID, PlaneID, RepairCode, RequestDate, PilotID FROM MaintenanceRequest WHERE PilotID = '" + pilotID + "';";
   
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
   
         System.out.printf("%-10s%-12s%-15s%-15s%-10s\n", "RequestID", "PlaneID", "RepairCode", "RequestDate", "PilotID");
   
         for (List<String> row : results) {
            System.out.printf("%-10s%-12s%-15s%-15s%-10s\n", row.get(0), row.get(1), row.get(2), row.get(3), row.get(4));
         }
   
         System.out.println("total row(s): " + results.size());
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }
   
   
   //technician #3
   public static void feature19(AirlineManagement esql) {
      try {
         System.out.print("\tEnter technician ID: ");
         String techID = in.readLine().trim();
   
         System.out.print("\tEnter plane ID: ");
         String planeID = in.readLine().trim();
   
         System.out.print("\tEnter repair code: ");
         String repairCode = in.readLine().trim();
   
         System.out.print("\tEnter repair date (YYYY-MM-DD): ");
         String repairDate = in.readLine().trim();
   
         String getMaxIdQuery = "SELECT MAX(RepairID) FROM Repair;";
         List<List<String>> result = esql.executeQueryAndReturnResult(getMaxIdQuery);
         int newID = (result.get(0).get(0) != null) ? Integer.parseInt(result.get(0).get(0)) + 1 : 1;
   
         String insertQuery = String.format(
            "INSERT INTO Repair (RepairID, TechnicianID, PlaneID, RepairCode, RepairDate) " +
            "VALUES (%d, '%s', '%s', '%s', DATE '%s');",
            newID, techID, planeID, repairCode, repairDate
         );
         esql.executeUpdate(insertQuery);

         String confirmQuery = String.format(
            "SELECT PlaneID, RepairCode, RepairDate FROM Repair WHERE RepairID = %d;",
            newID
         );
         int rowCount = esql.executeQueryAndPrintResult(confirmQuery);
   
         System.out.println("Repair logged successfully.");
         System.out.println("Total rows: " + rowCount);
      } catch(Exception e) {
         System.err.println("Error: " + e.getMessage());
      }
   }
  
}//end AirlineManagement

