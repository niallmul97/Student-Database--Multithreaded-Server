/** @author: Niall Mulcahy */

import java.sql.*;
import java.util.ArrayList;

public class JDBC {

    private static Connection con;
    private static ResultSet allData;

    //Method to connect to the mySQL running with WAMP, using the credentials username:"root", password:""
    public void connectToMySql() {

        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:mysql://localhost/assign2?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT", "root", "");
            if (!connection.isClosed())
                System.out.println("Connected to mysql server!");
        } catch (Exception e) {
            System.err.println("Exception " + e.getMessage());
        }
        con = connection;
    }

    //Method that generates the result set of the database
    //Result set is passed into the stud_list() method
    public String getStudentData(){
        ResultSet stud_rs = null;
        try{
            Statement s = con.createStatement();
            s.executeQuery("SELECT * FROM students");
            stud_rs = s.getResultSet();
        }catch(Exception e){
            e.printStackTrace();
        }
        return stud_list(stud_rs);
    }

    //Parses the result set into a string with commas to separate each parameter
    public String stud_list(ResultSet rs){
        String student = "";
        try{
            while (rs.next()){
                student += rs.getString("SID")+",";
                student += rs.getString("STUD_ID")+",";
                student += rs.getString("FNAME")+",";
                student += rs.getString("SNAME")+",";
            }
            return student;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //method that finds a user via a certain uid
    public ResultSet getUserData(String UID){
        ResultSet user_rs = null;
        try{
            PreparedStatement psmnt = con.prepareStatement("SELECT * FROM users WHERE UID = ?");
            psmnt.setString(1, UID);
            user_rs = psmnt.executeQuery();
            System.out.println(psmnt.toString());

            if (user_rs.next()){
                return user_rs;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //Method that uses a preparedStatement to find a student based on the last_name
    //Result set is passed into the stud_list() method
    public String sqlSearch(String sname){
        ResultSet search_rs = null;
        try{
            PreparedStatement psmnt = con.prepareStatement("SELECT * FROM students WHERE SNAME = ?");
            psmnt.setString(1,sname);
            search_rs = psmnt.executeQuery();

        }catch (Exception e){
            e.printStackTrace();
        }
        return stud_list(search_rs);
    }
}
