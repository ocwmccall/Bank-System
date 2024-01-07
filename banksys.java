import java.sql.*;
import java.util.Base64;
import java.util.InputMismatchException;
import java.util.Objects;
import java.util.Scanner;


public class banksys {
    private String user_ID;
    private int user_type;
    private Long user_balance;
    private String user_PIN;
    private boolean logged;


    void set_ID(String input) {
        user_ID = input;
    }
    String get_ID() {
        return user_ID;
    }
    void set_type(int input) {
        user_type = input;
    }
    int get_type() {
        return user_type;
    }
    void set_balance(Long input) {
        user_balance = input;
    }
    Long get_balance() {
        return user_balance;
    }
    String get_PIN() {
        return user_PIN;
    }
    void set_PIN(String input){
        user_PIN = input;
    }
    void set_logged(boolean input) {
        logged = input;
    }
    boolean get_logged() {
        return logged;
    }

    public Connection get_conn() {
        String jdbcurl = "jdbc:sqlite:/Users/owenmccall/bin/sqlite/db/bankdb.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(jdbcurl);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }

    public void login() throws SQLException{
        System.out.println("------------------------------\nHello! Welcome to the Bank");

        while (true) {
            System.out.println("----------\n" +
                    "Please enter your four-digit ID or -1 to exit the program:\n" +
                    "----------");
            try (Connection conn = get_conn();){
                String var = four_digits();
                if (var.equals("-1")) {
                    System.exit(0);
                }
                String sql = "SELECT * FROM Users2 WHERE userID = '" + var +"'";
                Statement statement = conn.createStatement();
                ResultSet results = statement.executeQuery(sql);

                set_ID(results.getString("userID"));
                if (get_ID() == null) {
                    System.out.println("Error: ID could not be found.");
                    continue;
                }
                set_type(results.getInt("type"));
                set_PIN(results.getString("PIN"));
                if (get_type() == 1) {
                    set_balance(results.getLong("balance"));

                }
                break;
            } catch (SQLException e) {
                System.out.println("Error: login_screen() 2");
                e.printStackTrace();
            }
        }

        String attempt = "";
        System.out.println("----------\n" +
                "Please enter your four digit PIN or -1 to return to login:\n" +
                "----------");
        while (!get_logged()) {
            attempt = four_digits();
            if (attempt.equals(get_PIN())) {
                set_logged(true);
                break;
            } if (attempt.equals("-1")) {
                login();
            } else {
                System.out.println("Incorrect PIN");
            }
        }
    }

    public String four_digits() {
        Integer IDinputInt;
        Scanner input1 = new Scanner(System.in);
        while (true) {
            try {
                String IDinput = input1.nextLine();
                IDinputInt = Integer.parseInt(IDinput);
                if (IDinputInt == -1) {
                    break;
                }
                if (IDinput.length() != 4) {
                    throw new ArithmeticException();
                }
                break;
            } catch (ArithmeticException a) {
                System.out.println("Error: Invalid Entry");
            } catch (InputMismatchException e) {
                input1.next();
                System.out.println("Error: Invalid Entry");
            } catch (Exception e) {
                System.out.println("Error: Invalid Entry");
            }
        }
        String output = Objects.toString(IDinputInt);
        if (output.equals("-1")) {
            return output;
        }
        if (IDinputInt < 10) {
            output = "000" + output;
            return output;
        } if (IDinputInt < 100) {
            output = "00" + output;
            return output;
        } if (IDinputInt < 1000) {
            output = "0" + output;
            return output;
        }
        return output;
    }

    public Integer one_digit(Integer range) {
        Integer IDinputInt;
        Scanner input1 = new Scanner(System.in);
        while (true) {
            try {
                String IDinput = input1.nextLine();
                IDinputInt = Integer.parseInt(IDinput);
                if (IDinputInt == -1) {
                    break;
                }
                if (IDinputInt < 1 || IDinputInt > range) {
                    throw new ArithmeticException();
                }
                break;
            } catch (ArithmeticException a) {
                System.out.println("Error: Invalid Entry");
            } catch (InputMismatchException e) {
                input1.next();
                System.out.println("Error: Invalid Entry");
            } catch (Exception e) {
                System.out.println("Error: Invalid Entry");
            }
        }
        return IDinputInt;
    }

    public Long many_digits(int digits) {
        Long temp;
        Scanner input1 = new Scanner(System.in);
        while (true) {
            try {
                String var = input1.nextLine();
                temp = Long.parseLong(var);
                if (temp == -1) {
                    break;
                }
                if (var.length() < 1 || var.length() > digits) {
                    throw new ArithmeticException();
                }
                break;
            } catch (ArithmeticException a) {
                System.out.println("Error: Invalid Entry");
            } catch (InputMismatchException e) {
                input1.next();
                System.out.println("Error: Invalid Entry");
            } catch (Exception e) {
                System.out.println("Error: Invalid Entry");
            }
        }
        return temp;
    }

    public String long_to_money(Long long1) {
        Long cents = (long1 % 100);
        String cents1;
        if ( cents < 10) {
            cents1 = "0" + Objects.toString(cents);
        } else {
            cents1 = Objects.toString(cents);
        }
        Long dollars = (long1 - cents) / 100;
        return ("$" + Objects.toString(dollars) + "." + cents1);
    }

}