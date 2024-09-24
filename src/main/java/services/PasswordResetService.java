package services;

import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PasswordResetService {

    private static PasswordResetService passwordResetService;
    private static ConcurrentMap<String, String> otpMap = new ConcurrentHashMap<>();

    private final Connection connection = DatabaseConnection.getConnection();



    public static PasswordResetService getPasswordResetService(){
        if (passwordResetService == null)
            passwordResetService = new PasswordResetService();
        return passwordResetService;
    }

    public void deleteOtp(String email){
        String query = "DELETE FROM otp WHERE email = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1,email);
            statement.executeUpdate();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }
}
