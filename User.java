import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.LinkedList;
import java.util.Objects;

public class User extends banksys{
    public User(banksys input) {
        set_ID(input.get_ID());
        set_balance(input.get_balance());
        set_PIN(input.get_PIN());
        set_logged(input.get_logged());
        }

    public void home() throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        System.out.println("------------------------------\n" +
                "Welcome to the Bank!");
        while(get_logged()) {
            System.out.println("------------------------------\n" +
                    "(1) Check Balance \n----------\n" +
                    "(2) Make a Deposit\n----------\n" +
                    "(3) Make a Withdrawal\n----------\n" +
                    "(4) Transfer Funds\n----------\n" +
                    "(5) Transaction History\n----------\n" +
                    "(6) Reset PIN\n----------\n" +
                    "(7) Logout\n----------\n" +
                    "Please enter the number for your choice\n" +
                    "------------------------------");

            int choice = one_digit(7);
            switch (choice) {
                case 1:
                    check_balance();
                    break;
                case 2:
                    user_deposit();
                    break;
                case 3:
                    user_withdrawal();
                    break;
                case 4:
                    user_transfer();
                    break;
                case 5:
                    user_history();
                    break;
                case 6:
                    reset_PIN();
                    break;
                case 7:
                    if (get_logged()) {
                        System.out.println("Signing out....");
                    }
                    set_logged(false);
                    break;
            }
        }
    }

    private void check_balance() throws SQLException {
        Long temp= get_balance().longValue();
        System.out.println("----------\n" +
                "Your Balance is: " + long_to_money(temp));
    }

    private void user_deposit() throws SQLException {
        System.out.println("----------\nPlease enter your deposit amount in cents, " +
                "the maximum deposit per transaction is one billion dollars:\n----------");
        Long temp;
        Boolean flag = false;
        while (true) {
            while (true) {
                temp = many_digits(19);
                if (temp == -1) {
                    flag = true;
                    break;
                }
                if (temp > Long.parseLong("100000000000")) {
                    System.out.println("That amount is too high, please deposit a smaller amount over multiple transactions.");
                    continue;
                }
                break;
            }
            if (flag) {
                break;
            }
            try (Connection conn = get_conn();){
                Statement statement = conn.createStatement();
                String sql = "UPDATE Users2 SET balance ='" + Objects.toString(get_balance() + temp) +
                        "' WHERE userID='" + Objects.toString(get_ID()) + "'";
                statement.executeUpdate(sql);
                set_balance(get_balance() + temp);
            } catch (SQLException e) {
                System.out.println("Error in user_deposit(), entry was:" + Objects.toString(temp));
                e.printStackTrace();
            }
            String sql = "INSERT INTO '" + get_ID() + "' (thirdparty, amount, type) VALUES (?, ?, ?)";

            try (PreparedStatement pstmt = get_conn().prepareStatement(sql)) {
                pstmt.setString(1, "ATM ");
                pstmt.setString(2, Objects.toString(temp));
                pstmt.setInt(3, 0);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            System.out.println("----------\n" + long_to_money(temp) + " has been deposited into your account.");

            break;
        }
    }

    private void user_withdrawal() throws SQLException {
        System.out.println("----------\nPlease enter your withdrawal amount in cents, " +
                "the maximum withdrawal per transaction is one billion dollars:\n----------");
        Long temp;
        Boolean flag = false;
        while (true) {
            while (true) {
                temp = many_digits(19);
                if (temp == -1) {
                    flag = true;
                    break;
                }
                if (temp > Long.parseLong("100000000000")) {
                    System.out.println("That amount is too high, please deposit a smaller amount over multiple transactions.");
                    continue;
                }
                break;
            }
            if (flag) {
                break;
            }
            try (Connection conn = get_conn();){
                Statement statement = conn.createStatement();
                String sql = "UPDATE Users2 SET balance ='" + Objects.toString(get_balance() - temp) +
                        "' WHERE userID='" + Objects.toString(get_ID()) + "'";
                statement.executeUpdate(sql);
                set_balance(get_balance() - temp);
                System.out.println("----------\n" + long_to_money(temp) + " has been withdrawn from your account.");
            } catch (SQLException e) {
                System.out.println("Error in user_withdrawal(), entry was:" + Objects.toString(temp));
                e.printStackTrace();
            }

            String sql = "INSERT INTO '" + get_ID() + "' (thirdparty, amount, type) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = get_conn().prepareStatement(sql)) {
                pstmt.setString(1, "ATM ");
                pstmt.setString(2, Objects.toString(temp));
                pstmt.setInt(3, 1);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            System.out.println("----------\n" + long_to_money(temp) + " has been withdrawn into your account.");

            break;
        }
    }

    private void user_transfer() throws SQLException {
        while (true) {
            System.out.println("----------\nPlease enter the account number you wish to transfer " +
                    "fund to, or -1 to go back:\n----------");
            String target = four_digits();
            if (target.equals(get_ID())) {
                System.out.println("Error: cannot transfer fund to same account");
                continue;
            }
            if (target.equals("-1")) {
                break;
            }
            ResultSet results = null;
            Integer targetID = null;
            long target_balance = 0;
            try (Statement state = get_conn().createStatement();){
                results = state.executeQuery("SELECT * FROM Users2 WHERE userID = '"
                        + target + "'");
                if (!results.isBeforeFirst()) {
                    System.out.println("Error: Account could not be found.");
                    continue;
                }
                if (results.getInt("type" ) != 1) {
                    System.out.println("Error: target account must be a customer account.");
                    continue;
                }
                targetID = results.getInt("userID");
                target_balance = results.getLong("balance");
            } catch (SQLException e) {
                System.out.println("ERROR LOCATION: user_transfer() query from ID");
            }

            System.out.println("Please enter an amount to transfer in cents, " +
                    "the maximum amount per transfer is one billion dollars, you can also enter -1 to go back.\n" +
                    "----------");
            Long temp;
            Boolean flag = false;

            while (true) {
                temp = many_digits(19);
                if (temp == -1) {
                    flag = true;
                    break;
                }
                if (temp > Long.parseLong("100000000000")) {
                    System.out.println("You cannot transfer amounts over one billion, please enter a smaller amount.");
                    continue;
                }
                break;
            }
            if (flag) {
                continue;
            }

            String source = "UPDATE Users2 SET balance='" + Objects.toString(get_balance() - temp) +
                            "' WHERE userID = '" + get_ID() + "'";
            String destination = "UPDATE Users2 SET balance='" + Objects.toString(target_balance + temp) +
                                "' WHERE userID ='" + target + "'";

            try (Statement state = get_conn().createStatement();){
                state.executeUpdate(source);
            } catch (SQLException e) {
                System.out.print("ERROR LOCATION: user_transfer() sql source update");
                e.printStackTrace();
            }

            try (Statement state = get_conn().createStatement();){
                state.executeUpdate(destination);
            } catch (SQLException e) {
                System.out.println("ERROR LOCATION: user_transfer() sql destination update");
                e.printStackTrace();
            }

            source = "INSERT INTO '" + get_ID() + "' (thirdparty, amount, type) VALUES (?, ?, ?)";
            destination = "INSERT INTO '" + target + "' (thirdparty, amount, type) VALUES (?, ?, ?)";

            try(PreparedStatement prepstat = get_conn().prepareStatement(source);) {
                prepstat.setString(1, Objects.toString(target));
                prepstat.setString( 2, Objects.toString(temp));
                prepstat.setInt(3, 1);
                prepstat.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Source record update prepstatement");
                e.printStackTrace();
            }

            try (PreparedStatement prepstat = get_conn().prepareStatement(destination);) {
                prepstat.setString(1, Objects.toString(get_ID()));
                prepstat.setString(2,Objects.toString(temp));
                prepstat.setInt(3, 0);
                prepstat.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Destination record update prepstatement");
                e.printStackTrace();
            }
            set_balance(get_balance() - temp);
            System.out.println("----------\n" + long_to_money(temp) + " has been transferred to " + Objects.toString(target) + ".");
            break;
        }
    }

    private void user_history() throws SQLException {
        String sql = "SELECT * FROM '" + get_ID() + "'";
        LinkedList<String> transactions = new LinkedList<>();
        try (PreparedStatement prepstat = get_conn().prepareStatement(sql)) {
            ResultSet results = prepstat.executeQuery();
            while (results.next()) {
                String source = results.getString("thirdparty");
                String amount = long_to_money(results.getLong("amount"));
                if (results.getInt("type") == 1) {
                    amount = "-" + amount;
                }
                transactions.addFirst(source + "                " + amount);
            }
        } catch (SQLException e) {
            System.out.println("Query for all rows error");
            e.printStackTrace();
        }


        int page = 1;
        int max_page = transactions.size() / 5;
        if (transactions.size() % 5 > 0) {
            max_page += 1;
        }
        //if (transactions.size() < 6) {
            //System.out.println("----------\nSource/AccountID    Amount                  " +
                   // "Page 1 of 1.\n------------------------------");
          //  for (int i = 0; i < transactions.size(); i++) {
           //     System.out.println(transactions.get(i));
            //}
        while (true) {
            System.out.println("Enter desired page below or -1 to exit, displaying max 5 entries per page:");

            System.out.println("----------\nSource/AccountID    Amount                  " +
                    "Page " + page + " of " + max_page + ".\n------------------------------");
            for (int i = (page * 5) - 5; i < (page * 5); i++) {
                if (i == transactions.size()) {
                    break;
                }
                System.out.println(transactions.get(i));
            }
            System.out.println("------------------------------\nPlease enter next page number or -1 to exit:");
            page = one_digit(max_page).intValue();
            if (page == -1) {
                break;
            }
        }
    }

    private void reset_PIN() throws NoSuchAlgorithmException, InvalidKeySpecException {
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
                        flag1= true;
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