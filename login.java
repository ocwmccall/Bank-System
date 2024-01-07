import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.InputMismatchException;
import java.util.Objects;
import java.util.Scanner;

public class login {
    public static void main(String[] args) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        while (true) {
            banksys bank = new banksys();
            bank.login();

            switch (bank.get_type()) {
                case 1:
                    User bankuser = new User(bank);
                    bankuser.home();
                    break;
                case 2:
                    Employee empuser = new Employee(bank);
                    empuser.home();
                    break;
                case 3:
                    Admin adminuser = new Admin(bank);
                    adminuser.home();
                    break;
            }
        }
    }
}
