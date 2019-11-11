/** @author: Niall Mulcahy */

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;

public class Client {

    private DataOutputStream toServer;
    private DataInputStream fromServer;

    private static JLabel labelStudentDetails = new JLabel ("Student Details");
    private static JLabel labelStudent_FNAME = new JLabel ("First Name");
    private static JLabel labelStudent_SNAME = new JLabel ("Last Name");
    private static JLabel labelStudent_SID = new JLabel ("SID");
    private static JLabel labelStudent_STUD_ID = new JLabel ("STUD ID");
    private static JLabel labelError = new JLabel();
    private static JTextField tfStudent_FNAME = new JTextField();
    private static JTextField tfStudent_SNAME = new JTextField();
    private static JTextField tfStudent_SID = new JTextField();
    private static JTextField tfStudent_STUD_ID = new JTextField();
    private static JButton btPrevious = new JButton("Previous");
    private static JButton btNext = new JButton("Next");
    private static JButton btClear = new JButton("Clear");
    private static JButton btSearch = new JButton("Search");

    private static JLabel labelLogin = new JLabel ("Login");
    private static JLabel labelUID = new JLabel ("UID");
    private static JLabel labelUNAME = new JLabel ("UNAME");
    private static JButton btLogin = new JButton("Login");
    private static JTextField tfUID = new JTextField();
    private static JTextField tfUNAME = new JTextField();

    private static  JButton btLogout = new JButton("Logout");
    private static JTextArea clientTextArea = new JTextArea();

    private static ArrayList<Student> studentList = new ArrayList();
    private static ArrayList<Student> searchList = new ArrayList();
    private Integer studentIndex = 0;

    private static Socket socket = new Socket();

    //Main function starts thread of client and loads up the GUI
    public static void main(String[] args){
        Client client = new Client();
        client.generateGui();
    }

    //Method that displays the user on the GUI from the student arraylist
    public void displayStudentData(){

        try{
            tfStudent_SID.setText(studentList.get(studentIndex).getSID());
            tfStudent_FNAME.setText(studentList.get(studentIndex).getFNAME());
            tfStudent_SNAME.setText(studentList.get(studentIndex).getSNAME());
            tfStudent_STUD_ID.setText(studentList.get(studentIndex).getSTUD_ID());
        }catch (Exception e){
            labelError.setText("");
        }
    }

    //Method connects the client to the server
    public void connectToServer(){
        try {
            // Create a socket to connect to the server
            Socket socket = new Socket("localhost", 8000);

            // Create an input stream to receive data from the server
            fromServer = new DataInputStream(socket.getInputStream());

            // Create an output stream to send data to the server
            toServer = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException ex) {
            labelError.setText(ex.toString() + '\n');
        }
    }

    //Sends a login request to the server, based on the response from the server it will either fail to login and
    // display a message to the user or will call the isLoggedIn() method
    public void loginToServer(){
        String UID = tfUID.getText();
        String res = "";
        try {
            toServer.writeUTF("Login,"+UID);
            res = fromServer.readUTF();
            System.out.println(res);
            if (!res.equals("Login failed")){
                clientTextArea.append("Log in successful \n");
                clientTextArea.append("Logged in as "+res +"\n");
                isLoggedIn();
            }else labelError.setText("Login failed");
        }catch (Exception e){
            System.out.println(e);
        }
    }

    //Only triggers when login attempt is successful, enables the buttons on the client, as well as the clientTextArea.
    //Calls the getStudentData() method
    public void isLoggedIn(){
        btNext.setEnabled(true);
        btPrevious.setEnabled(true);
        btSearch.setEnabled(true);
        btClear.setEnabled(true);
        btLogin.setVisible(false);
        btLogout.setEnabled(true);
        labelError.setText("");
        clientTextArea.setVisible(true);
        getStudentData();
    }

    //Method that sends a request to get all the students in the database to the server, based on the result, it will either
    //display that no students were found or will return a string containing all the student data, this parseStudentData
    //method is then called with aforementioned string as a parameters.
    public void getStudentData(){
        String res = "";
        try{
            toServer.writeUTF("Student");
            res = fromServer.readUTF();
            System.out.println(res);
            if (res != "No students found"){
                clientTextArea.append("Connected to student database \n");
                clientTextArea.append("Students found \n");
                parseStudentData(res);
            }else clientTextArea.append("No students found \n");
        }catch (Exception e){

        }
    }

    //Method that parses the string (comma separated) containing all the student data into students objects and adds them to the student list
    //then calls the displayStudentData() method
    public void parseStudentData(String data){
        String[] student = data.split(",");
        for (int i = 0; i < student.length; i+=4){
            String SID = student[i];
            String STUD_ID = student[i+1];
            String FNAME = student[i+2];
            String SNAME = student[i+3];
            Student newStudent = new Student(SID, STUD_ID, FNAME, SNAME);
            studentList.add(newStudent);
        }
        displayStudentData();
    }

    //Method that sends a search request to the server, the request begins with "Search," and ends with the "SNAME" from the text field
    //and based off of the response, it either displays that "no students were found"
    //or will once again return a string of the students found and call the parseSearchData() method
    public void searchStudents(){
        clientTextArea.append("Searching for students \n");
        String SNAME = tfStudent_SNAME.getText();
        String res = "";
        try {
            toServer.writeUTF("Search,"+SNAME);
            res = fromServer.readUTF();
            System.out.println(res);
            if (res != "No student found"){
                clientTextArea.append("Students found \n");
                parseSearchData(res);
            }else clientTextArea.append("No students found \n");

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //Method that parses the string (comma separated) containing all the search data into students objects and adds them to the search list
    //then calls the displaySearchData() method
    public void parseSearchData(String data){
        String[] student = data.split(",");
        for (int i = 0; i < student.length; i+=4){
            String SID = student[i];
            String STUD_ID = student[i+1];
            String FNAME = student[i+2];
            String SNAME = student[i+3];
            Student newStudent = new Student(SID, STUD_ID, FNAME, SNAME);
            searchList.add(newStudent);
        }
        displaySearchData();
    }

    //Method that displays students searched for on the GUI from the searchlist
    public void displaySearchData(){

        try{
            tfStudent_SID.setText(searchList.get(studentIndex).getSID());
            tfStudent_FNAME.setText(searchList.get(studentIndex).getFNAME());
            tfStudent_SNAME.setText(searchList.get(studentIndex).getSNAME());
            tfStudent_STUD_ID.setText(searchList.get(studentIndex).getSTUD_ID());
        }catch (Exception e){
            labelError.setText("");
        }
    }

    //Method that returns GUI to the login state and closes connection to the server
    public void logout(){
        clientTextArea.append("Logging out...");
        btNext.setEnabled(false);
        btClear.setEnabled(false);
        btPrevious.setEnabled(false);
        btSearch.setEnabled(false);
        btLogin.setVisible(true);
        btLogout.setEnabled(false);
        clientTextArea.setVisible(false);
        studentList.clear();
        try {
            toServer.writeUTF("Logout");
            socket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Method that clears the GUI by emptying the text fields and the search list
    public void clearGui(){
        tfStudent_STUD_ID.setText("");
        tfStudent_SID.setText("");
        tfStudent_FNAME.setText("");
        tfStudent_SNAME.setText("");
        labelError.setText("");
        searchList.clear();
    }

    //Method that creates the Java Swing GUI
    private void generateGui(){
        JFrame frame = new JFrame("Student Details");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Setting boundaries
        labelStudentDetails.setBounds(140,0,160,40);
        labelStudent_SID.setBounds(10, 30, 80, 40);
        tfStudent_SID.setBounds(90, 42, 160, 20);
        labelStudent_STUD_ID.setBounds(10, 60, 80, 40);
        tfStudent_STUD_ID.setBounds(90, 72, 160, 20);
        labelStudent_FNAME.setBounds(10, 92, 80, 40);
        tfStudent_FNAME.setBounds(90, 102, 160, 20);
        labelStudent_SNAME.setBounds(10, 122, 80, 40);
        tfStudent_SNAME.setBounds(90, 132, 160, 20);
        btPrevious.setBounds(275,42,85,20);
        btNext.setBounds(275, 72, 85,20);
        btClear.setBounds(275,192, 85,20 );
        btSearch.setBounds(150,192,85,20);
        labelError.setBounds(50,410,400,20);
        clientTextArea.setBounds(5,222,375,235);
        labelLogin.setBounds(140, 222, 80, 40);
        labelUID.setBounds(10, 252, 80,40);
        tfUID.setBounds(90,262,160,20);
        btLogin.setBounds(10,302,85,20);
        btLogout.setBounds(25, 192, 85, 20);
        //Adding components
        frame.add(labelStudentDetails);
        frame.add(labelStudent_FNAME);
        frame.add(tfStudent_FNAME);
        frame.add(labelStudent_SNAME);
        frame.add(tfStudent_SNAME);
        frame.add(labelStudent_SID);
        frame.add(tfStudent_SID);
        frame.add(labelStudent_STUD_ID);
        frame.add(tfStudent_STUD_ID);
        frame.add(clientTextArea);
        clientTextArea.setVisible(false);
        btNext.setEnabled(false);
        btPrevious.setEnabled(false);
        btSearch.setEnabled(false);
        btLogout.setEnabled(false);
        btClear.setEnabled(false);

        frame.add(labelLogin);
        frame.add(labelUID);
        frame.add(tfUID);

        //Adding buttons as well as their event handlers
        frame.add(btLogin);
        //When login is pressed, the client attempts to connect to the server and then login
        btLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();
                loginToServer();
            }
        });
        frame.add(btLogout);
        //When login is pressed, the GUI is reset and connection to server terminated
        btLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        //Previous button changes the index (back) of which student to display from the list when it gets to the start
        //of the list it will loop back around to the top. Note, if the searchlist is not empty, then it will display
        //from the search list instead, otherwise it is from the standard students list containing all students in db
        frame.add(btPrevious);
        btPrevious.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                studentIndex--;
                if (searchList.size() != 0){
                    if (studentIndex < 0){
                        studentIndex = searchList.size()-1;
                        displaySearchData();
                    }
                    displaySearchData();
                }else {
                    if (studentIndex < 0) {
                        studentIndex = studentList.size()-1;
                        displayStudentData();
                    }
                    displayStudentData();
                }
            }
        });
        frame.add(btNext);

        //Next button changes the index (forwards) of which student to display from the list when it gets to the end
        //of the list it will loop back around to the start. Note, if the searchlist is not empty, then it will display
        //from the search list instead, otherwise it is from the standard students list containing all students in db
        btNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                studentIndex++;
                if (searchList.size() != 0){
                    if (studentIndex >= searchList.size()){
                        studentIndex = 0;
                        displaySearchData();
                    }
                    displaySearchData();
                }
                else{
                    if (studentIndex >= studentList.size()){
                        studentIndex = 0;
                        displayStudentData();
                    }
                    displayStudentData();
                }
            }
        });
        frame.add(btClear);

        //When the clear button is pressed, the fields in the GUI are emptied
        btClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearGui();
            }
        });

        frame.add(btSearch);

        //When the search button is pressed the search request is sent to the server
        btSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchStudents();
            }
        });
        frame.add(labelError);
        frame.getContentPane().setLayout(null);
        frame.pack();
        frame.setSize(400, 500);
        frame.setVisible(true);
    }
}

//Student Class to create student object 
class Student{

    private String SID;
    private String STUD_ID;
    private String FNAME;
    private String SNAME;

    public Student(String SID, String STUD_ID, String FNAME, String SNAME){
        this.setFNAME(FNAME);
        this.setSNAME(SNAME);
        this.setSID(SID);
        this.setSTUD_ID(STUD_ID);
    }

    
    //Getters and setters for Student class
    public String getSID() {
        return SID;
    }

    public void setSID(String SID) {
        this.SID = SID;
    }

    public String getSTUD_ID() {
        return STUD_ID;
    }

    public void setSTUD_ID(String STUD_ID) {
        this.STUD_ID = STUD_ID;
    }

    public String getFNAME() {
        return FNAME;
    }

    public void setFNAME(String FNAME) {
        this.FNAME = FNAME;
    }

    public String getSNAME() {
        return SNAME;
    }

    public void setSNAME(String SNAME) {
        this.SNAME = SNAME;
    }
}