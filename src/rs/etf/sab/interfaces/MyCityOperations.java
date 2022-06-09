package rs.etf.sab.interfaces;

import java.sql.*;
import java.util.*;

import rs.etf.sab.DB.DB;
import rs.etf.sab.operations.CityOperations;

public class MyCityOperations implements CityOperations {

	private Connection connection = DB.getInstance().getConnection();
	
	@Override
	public int deleteCity(String... names) {
		final String sqlQuery = "DELETE FROM Grad WHERE Naziv=?";
		int affectedRows = 0;
		for (String name : names) {
			try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
				ps.setString(1, name);
				affectedRows += ps.executeUpdate();
			} catch (SQLException e) {
				// ...
			}
		}
		return affectedRows;
	}

	@Override
	public boolean deleteCity(int idCity) {
		final String sqlQuery = "DELETE FROM Grad WHERE IdGra=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, idCity);
			return (ps.executeUpdate() == 1);
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public List<Integer> getAllCities() {
		List<Integer> cityIds = new ArrayList<>();
		final String sqlQuery = "SELECT IdGra FROM Grad";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sqlQuery)) {
			while (rs.next()) {
				int cityId = rs.getInt("IdGra");
				cityIds.add(cityId);
			}
		} catch (SQLException e) {
			return null;
		}
		return cityIds;
	}
	
	private boolean uniqueNameAndPostalCode(String name, String postalCode) {
		final String sqlQuery = "SELECT * FROM Grad WHERE Naziv=? OR PostanskiBroj=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, name);
			ps.setString(2, postalCode);
			try (ResultSet rs = ps.executeQuery()) {
				return (rs.next() == false);
			}
		} catch (SQLException e) {
			return true;
		}
	}

	@Override
	public int insertCity(String name, String postalCode) {
		if (uniqueNameAndPostalCode(name, postalCode) == false) return -1;
		final String sqlQuery = "INSERT INTO Grad (Naziv, PostanskiBroj) VALUES (?, ?)";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, name);
			ps.setString(2, postalCode);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0)
				throw new SQLException("Creating a city failed, no rows affected.");
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next())
					return rs.getInt(1);
				else
					throw new SQLException("Creating a city failed, no ID obtained.");
			}
		} catch (SQLException e) {
			return -1;
		}
	}

}
