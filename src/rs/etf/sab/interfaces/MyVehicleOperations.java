package rs.etf.sab.interfaces;

import java.math.BigDecimal;
import java.util.*;
import java.sql.*;

import rs.etf.sab.DB.DB;
import rs.etf.sab.operations.VehicleOperations;

public class MyVehicleOperations implements VehicleOperations {

	private Connection connection = DB.getInstance().getConnection();
	
	@Override
	public boolean changeConsumption(String licencePlateNumber, BigDecimal fuelConsumption) {
		if (fuelConsumption.doubleValue() < 0.0) return false;
		final String sqlQuery = "UPDATE Vozilo SET Potrosnja=? WHERE RegistracioniBroj=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setBigDecimal(1, fuelConsumption);
			ps.setString(2, licencePlateNumber);
			return (ps.executeUpdate() == 1); 
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public boolean changeFuelType(String licencePlateNumber, int fuelType) {
		if (fuelType < 0 || fuelType > 2) return false;
		final String sqlQuery = "UPDATE Vozilo SET TipGoriva=? WHERE RegistracioniBroj=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, fuelType);
			ps.setString(2, licencePlateNumber);
			return (ps.executeUpdate() == 1); 
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public int deleteVehicles(String... licencePlateNumbers) {
		final String sqlQuery = "DELETE FROM Vozilo WHERE RegistracioniBroj=?";
		int affectedRows = 0;
		for (String licencePlateNumber : licencePlateNumbers) {
			try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
				ps.setString(1, licencePlateNumber);
				affectedRows += ps.executeUpdate();
			} catch (SQLException e) {
				// ...
			}
		}
		return affectedRows;
	}

	@Override
	public List<String> getAllVehichles() {
		List<String> allVehicles = new ArrayList<>();
		final String sqlQuery = "SELECT RegistracioniBroj FROM Vozilo";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sqlQuery)) {
			while (rs.next()) {
				String user = rs.getString("RegistracioniBroj");
				allVehicles.add(user);
			}
			return allVehicles;
		} catch (SQLException e) {
			return null;
		}
	}

	@Override
	public boolean insertVehicle(String licencePlateNumber, int fuelType, BigDecimal fuelConsumption) {
		if (fuelType < 0 || fuelType > 2 || fuelConsumption.doubleValue() < 0.0) return false;
		final String sqlQuery = "INSERT INTO Vozilo (RegistracioniBroj, TipGoriva, Potrosnja)"
				+ " VALUES (?, ?, ?)";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, licencePlateNumber);
			ps.setInt(2, fuelType);
			ps.setBigDecimal(3, fuelConsumption);
			return (ps.executeUpdate() == 1);
		} catch (SQLException e) {
			return false;
		}
	}

}
