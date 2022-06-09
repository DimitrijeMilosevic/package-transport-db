package rs.etf.sab.interfaces;

import java.sql.*;
import java.util.*;

import rs.etf.sab.DB.DB;
import rs.etf.sab.operations.CourierRequestOperation;

public class MyCourierRequestOperation implements CourierRequestOperation {

	private Connection connection = DB.getInstance().getConnection();
	
	@Override
	public boolean changeVehicleInCourierRequest(String username, String licencePlateNumber) {
		final String sqlQuery = "UPDATE ZahtevKurir SET RegistracioniBroj=? WHERE KorisnickoIme=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, licencePlateNumber);
			ps.setString(2, username);
			return (ps.executeUpdate() == 1);
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public boolean deleteCourierRequest(String username) {
		final String sqlQuery = "{call spDeleteCourierRequest(?)}";
		try (CallableStatement cs = connection.prepareCall(sqlQuery)) {
			cs.setString(1, username);
			cs.execute();
			return (cs.getUpdateCount() > 0);
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public List<String> getAllCourierRequests() {
		List<String> courierRequests = new ArrayList<>();
		final String sqlQuery = "SELECT KorisnickoIme FROM ZahtevKurir";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sqlQuery)) {
			while (rs.next()) {
				String courierRequest = rs.getString("KorisnickoIme");
				courierRequests.add(courierRequest);
			}
			return courierRequests;
		} catch (SQLException e) {
			return null;
		}
	}

	@Override
	public boolean grantRequest(String username) {
		final String sqlQuery = "{call spGrantRequest(?)}";
		try (CallableStatement cs = connection.prepareCall(sqlQuery)) {
			cs.setString(1, username);
			cs.execute();
			return (cs.getUpdateCount() > 0);
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public boolean insertCourierRequest(String username, String licencePlateNumber) {
		final String sqlQuery = "INSERT INTO ZahtevKurir (KorisnickoIme, RegistracioniBroj) VALUES (?, ?)";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, username);
			ps.setString(2, licencePlateNumber);
			return (ps.executeUpdate() == 1);
		} catch (SQLException e) {
			return false;
		}
	}

}
