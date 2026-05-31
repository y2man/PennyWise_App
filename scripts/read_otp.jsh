import java.sql.*;
Class.forName("org.sqlite.JDBC");
Connection c = DriverManager.getConnection("jdbc:sqlite:C:/Users/lenovo/.pennywise/pennywise.db");
PreparedStatement s = c.prepareStatement("select email, otp_code, otp_expiry from users where email=?");
s.setString(1, "yisakyisak20@gmail.com");
ResultSet rs = s.executeQuery();
while (rs.next()) {
    System.out.println(rs.getString(1) + "|" + rs.getString(2) + "|" + rs.getString(3));
}
rs.close(); s.close(); c.close();
