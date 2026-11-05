/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dunkin.database;
import java.sql.Connection;
/**
 *
 * @author ava
 */
public class TestConnection {
    public static void main(String[] args) {

        Connection connection = DatabaseConnection.getConnection();

        if (connection != null) {
            System.out.println("Connected to MySQL!");
        } else {
            System.out.println("Connection failed!");
        }
    }
}
