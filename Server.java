/** @author: Niall Mulcahy */

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Date;

public class Server {

    private static JTextArea serverTextArea = new JTextArea();
    private static Connection con;

    public static void main(String[] args){
        new Server();
        JDBC jdbc = new JDBC();
        jdbc.connectToMySql();
    }

    public Server() {

        JFrame frame = new JFrame("Student Server");
        frame.add(serverTextArea);
        serverTextArea.setBounds(0,0,400,400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            // Create a server socket
            ServerSocket serverSocket = new ServerSocket(8000);
            serverTextArea.append("Server started at " + new Date() + '\n');
            frame.getContentPane().setLayout(null);
            frame.pack();
            frame.setSize(400, 400);
            frame.setVisible(true);

            while (true) {
                Socket s1 = serverSocket.accept();
                myClient c = new myClient(s1, serverTextArea);
                c.start();
            }
        }
        catch(IOException ex) {
            System.err.println(ex);
        }
    }
}

class myClient extends Thread{

    private JDBC jdbc;
    private JTextArea serverTextArea;

    // Socket client connects though
    private Socket socket;

    // Ip address of the client
    private InetAddress address;

    // The input and output streams to the client
    private DataInputStream inputFromClient;
    private DataOutputStream outputToClient;

    // Constructor for the client
    public myClient(Socket socket, JTextArea serverTextArea) throws IOException {

        this.serverTextArea = serverTextArea;
        this.jdbc = new JDBC();
        jdbc.connectToMySql();

        // Create data input and output streams
        this.inputFromClient = new DataInputStream(socket.getInputStream());
        this.outputToClient = new DataOutputStream(socket.getOutputStream());
        this.socket = socket;

        System.out.println(inputFromClient);
        System.out.println(outputToClient);
    }

    public void run() {
        while(true){
            try {
                //Initializes the client request
                String clientRequest = inputFromClient.readUTF();
                //Splits the client request into an array, string was separated by commas, each comma implies a new index
                String[] request = clientRequest.split(",");

                //if the first index of the request array is login, the server attempts to find the user from the db via the UID specified
                if (request[0].equals("Login")){
                    serverTextArea.append("Logging in... \n");
                    ResultSet login = jdbc.getUserData(request[1]);
                    sleep(150);
                    if (login == null){

                        //if unsuccessful, it will be displayed back to the client and on the server
                        serverTextArea.append("Login failed \n");
                        outputToClient.writeUTF("Login failed");
                    } else{

                        //if login is successful, it will be displayed back to the client and the server
                        String uname = login.getString("UNAME");
                        serverTextArea.append("Login Successful \n");
                        serverTextArea.append("Logged in as " +uname +"\n");
                        outputToClient.writeUTF(login.getString("UNAME"));
                    }

                    //if the first index of the request array is student, the server attempts to get all student data from db
                }else if (request[0].equals("Student")){
                    String studentData = jdbc.getStudentData();

                    //if successful it will return the students back the the client add appropriate message to server text area
                    if (studentData != null){
                        outputToClient.writeUTF(studentData);
                        sleep(150);
                        serverTextArea.append("Connected to student database \n");
                        sleep(150);
                        serverTextArea.append("Students found \n");

                        //if unsuccessful it will relay as such back to the client
                    }else {
                        outputToClient.writeUTF("No students found");
                        serverTextArea.append("No students found");
                    }

                    //if the first index of the request array is search, it will search for a student via the second index (SNAME)
                }else if(request[0].equals("Search")){
                    serverTextArea.append("Searching student database \n");
                    String sqlSearch = jdbc.sqlSearch(request[1]);

                    //if the search is successful, the students searched for will be returned to the client as a string
                    if (sqlSearch != null){
                        outputToClient.writeUTF(sqlSearch);
                        serverTextArea.append("Students found \n");
                        System.out.println(sqlSearch);
                    }else {

                        //if unsuccessful it will display as such on both the client and server text areas.
                        serverTextArea.append("No students found \n");
                        outputToClient.writeUTF("No students found");
                    }

                    //if the first index for request array is logout, the current thread is terminated
                }else if (request[0].equals("Logout")){
                    serverTextArea.append("Logging out... \n");
                    serverTextArea.append("Bye \n");
                    this.interrupt();
                }
            } catch (Exception e){
                System.err.println(e + " on " + socket);
            }
        }
    }
}
