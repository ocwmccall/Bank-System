import java.sql.*;
import java.util.Objects;

public class Employee extends banksys{
    public Employee(banksys input) {
        set_ID(input.get_ID());
        set_balance(input.get_balance());
        set_PIN(input.get_PIN());
        set_logged(input.get_logged());
    }

    public void home() throws SQLException {
        System.out.println("------------------------------\n" +
                "Hello Employee " + Objects.toString(get_ID()));
        while(get_logged()) {
            System.out.println("------------------------------\n" +
                    "(1) Create Customer Account \n----------\n" +
                    "(2) Reset Customer PIN\n----------\n" +
                    "(3) Reset PIN\n----------\n" +
                    "(4) Logout \n---------\n" +
                    "Please enter the number for your choice\n" +
                    "------------------------------");

            int choice = one_digit(4);
            switch (choice) {
                case 1:
                    create_user();
                    break;
                case 2:
                    reset_userPIN();
                    break;
                case 3:
                    reset_PIN();
                    break;
                case 4:
                    set_logged(false);
                    break;
            }
        }
    }

    private void create_user() {
        System.out.println("----------\nPlease ask Customer for a desired userID or enter -1 to go back.\n----------");
        String newID;
        Boolean flag = false;
        while (true) {
            while (true) {
                newID = four_digits();
                if (newID.equals("-1")) {
                    flag = true;
                    break;
                }
                String sql = "SELECT * FROM Users2 WHERE userID='" + Objects.toString(newID) + "'";
                try (PreparedStatement prep = get_conn().prepareStatement(sql)) {
                    ResultSet results = prep.executeQuery();
                    if (0 != results.getInt("userID")) {
                        System.out.println("This userID is already taken, please ask the customer for another.");
                        continue;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }
            if (flag) {
                break;
            }
            String sql = "INSERT INTO Users2 (userID, PIN, type, balance) VALUES (?, ?, ?, ?)";
            try (Connection conn = get_conn()) {
                PreparedStatement prep = conn.prepareStatement(sql);
                prep.setString(1, newID);
                prep.setString(2, "0000");
                prep.setInt(3,1);
                prep.setString(4, "0");
                if (prep.executeUpdate() != 1) {
                    throw new ArithmeticException();
                }
            }catch (SQLException e) {
                e.printStackTrace();
            }catch (ArithmeticException a) {
                System.out.println("Entry wasn't added to table");
                a.printStackTrace();
            }

            sql = "CREATE TABLE '" + Objects.toString(newID) + "' (thirdparty TEXT, amount TEXT, type INTEGER)";
            try (PreparedStatement prep = get_conn().prepareStatement(sql)) {
                prep.executeUpdate();
                System.out.println("----------\nCustomer " + newID + " has been created with PIN set to 0000");
                break;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void reset_userPIN() throws SQLException {
        System.out.println("----------\nPlease enter ID of user to reset their PIN or -1 to go back\n----------");
        String target;
        while (true) {
            target = four_digits();
            if (target.equals("-1")) {
                break;
            }
            if (target.equals(get_ID())) {
                System.out.println("Cannot reset own PIN with this function," +
                        " choose another ID or -1 to go back.");
                continue;
            }
            try (Statement stat = get_conn().createStatement()) {
                ResultSet results = stat.executeQuery("SELECT * FROM Users2 WHERE userID='"
                        + target + "'");
                if (!results.isBeforeFirst()) {
                    System.out.println("Error: ID could not be found, select another or enter -1 to go back.");
                    continue;
                }
                Integer type = results.getInt("type");
                if (type != 1) {
                    System.out.println("Cannot reset non-user PIN with this function," +
                            " please enter a user PIN or -1 to go back");
                    continue;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            String sql = "UPDATE Users2 SET PIN='0000' WHERE userID='"
                    + target + "'";
            try (PreparedStatement prep = get_conn().prepareStatement(sql)) {
                prep.executeUpdate();
                System.out.println("----------\nUser " + target + " has had their PIN set to 0000.");
                break;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    private void reset_PIN() {
        String temp = "";
        String temp1 = "";
        Boolean flag = false;
        Boolean flag1 = false;
        while (true) {
            System.out.println("----------\nPlease enter your PIN or -1 to go back\n----------");
            flag = false;
            flag1 = false;
            temp = four_digits();
            if (temp.equals("-1")) {
                break;
            }
            if (!temp.equals(get_PIN())) {
                System.out.println("Incorrect PIN, please try again or -1 to go back.");
                continue;
            }
            while (true) {
                System.out.println("----------\nPlease enter your new PIN or -1 to go back\n----------");
                temp = four_digits();
                if (temp.equals("-1")) {
                    flag = true;
                    break;
                }
                System.out.println("----------\nPlease re-enter the new PIN or -1 to go back\n----------");
                while (true) {
                    temp1 = four_digits();
                    if (temp1.equals("-1")) {
                        break;
                    }
                    if (temp.equals(temp1)) {
                        String sql = "UPDATE Users2 SET PIN='" + Objects.toString(temp1) + "' WHERE userID='"
                                + get_ID() + "'";
                        try (PreparedStatement prep = get_conn().prepareStatement(sql)) {
                            prep.executeUpdate();
                        }catch (SQLException e) {
                            e.printStackTrace();
                        }
                        flag = true;
                        flag1 = true;
                        break;
                    } else {
                        System.out.println("Incorrect PIN, please enter your new PIN or -1 to go back.");
                    }
                }
                if (flag) {
                    break;
                }
            }
            if (flag1) {
                break;
            }
        }
        if (flag1) {
            System.out.println("----------\nYour PIN has been set to " + Objects.toString(temp1) + ".");
        }
    }
}
