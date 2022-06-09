package rs.etf.sab.interfaces;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

import rs.etf.sab.DB.DB;
import rs.etf.sab.operations.CourierOperations;

public class MyCourierOperations implements CourierOperations {

	private Connection connection = DB.getInstance().getConnection();
	
	@Override
	public boolean deleteCourier(String courierUsername) {
		final String sqlQuery = "DELETE FROM Kurir WHERE KorisnickoIme=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, courierUsername);
			return (ps.executeUpdate() == 1);
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public List<String> getAllCouriers() {
		List<String> courierUsernames = new ArrayList<>();
		final String sqlQuery = "SELECT KorisnickoIme FROM Kurir";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sqlQuery)) {
			while (rs.next()) {
				String courierUsername = rs.getString("KorisnickoIme");
				courierUsernames.add(courierUsername);
			}
		} catch (SQLException e) {
			return null;
		}
		return courierUsernames;
	}

	@Override
	public BigDecimal getAverageCourierProfit(int numberOfDeliveries) {
		final String sqlQuery = "SELECT AVG(OstvarenProfit) FROM Kurir WHERE BrojIsporucenihPaketa >= ?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, numberOfDeliveries);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getBigDecimal(1);
				else
					return new BigDecimal(0);
			}
		} catch (SQLException e) {
			return new BigDecimal(0);
		}
	}

	@Override
	public List<String> getCouriersWithStatus(int statusOfCourier) {
		List<String> couriersWithStatus = new ArrayList<>();
		final String sqlQuery = "SELECT KorisnickoIme FROM Kurir WHERE Status=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, statusOfCourier);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String courierWithStatus = rs.getString("KorisnickoIme");
					couriersWithStatus.add(courierWithStatus);
				}
			}
		} catch (SQLException e) {
			return null;
		}
		return couriersWithStatus;
	}

	private boolean vehicleUsed(String licencePlateNumber) {
		final String sqlQuery = "SELECT * FROM Kurir WHERE RegistracioniBroj=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, licencePlateNumber);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return true;
				else
					return false;
			}
		} catch (SQLException e) {
			return true;
		}
	}
	
	@Override
	public boolean insertCourier(String courierUsername, String licencePlateNumber) {
		if (vehicleUsed(licencePlateNumber) == true) return false;
		final String sqlQuery = "INSERT INTO Kurir (KorisnickoIme, BrojIsporucenihPaketa, OstvarenProfit, Status, RegistracioniBroj) "
				+ "VALUES (?, 0, 0, 0, ?)";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, courierUsername);
			ps.setString(2, licencePlateNumber);
			return (ps.executeUpdate() == 1);
		} catch (SQLException e) {
			return false;
		}
	}

}
