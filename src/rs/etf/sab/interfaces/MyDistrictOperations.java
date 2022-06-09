package rs.etf.sab.interfaces;

import java.sql.*;
import java.util.*;

import rs.etf.sab.DB.DB;
import rs.etf.sab.operations.DistrictOperations;

public class MyDistrictOperations implements DistrictOperations {

	private Connection connection = DB.getInstance().getConnection();
	
	@Override
	public int deleteAllDistrictsFromCity(String nameOfTheCity) {
		final String sqlQuery = "DELETE FROM Opstina WHERE IdGra IN (SELECT IdGra FROM Grad WHERE Naziv=?)";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, nameOfTheCity);
			return ps.executeUpdate();
		} catch (SQLException e) {
			return 0;
		}
	}

	@Override
	public boolean deleteDistrict(int idDistrict) {
		final String sqlQuery = "DELETE FROM Opstina WHERE IdOps=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, idDistrict);
			return (ps.executeUpdate() == 1);
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public int deleteDistricts(String... names) {
		final String sqlQuery = "DELETE FROM Opstina WHERE Naziv=?";
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
	public List<Integer> getAllDistricts() {
		List<Integer> allDistricts = new ArrayList<>();
		final String sqlQuery = "SELECT IdOps FROM Opstina";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sqlQuery)) {
			while (rs.next()) {
				int districtFromCity = rs.getInt("IdOps");
				allDistricts.add(districtFromCity);
			}
			return allDistricts;
		} catch (SQLException e) {
			return null;
		}
	}

	@Override
	public List<Integer> getAllDistrictsFromCity(int cityId) {
		String sqlQuery = "SELECT * FROM Grad WHERE IdGra=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, cityId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next() == false) return null;
			}
		} catch (SQLException e) {
			return null;
		}
		List<Integer> districtsFromCity = new ArrayList<>();
		sqlQuery = "SELECT IdOps FROM Opstina WHERE IdGra=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, cityId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int districtFromCity = rs.getInt("IdOps");
					districtsFromCity.add(districtFromCity);
				}
			}
			return districtsFromCity;
		} catch (SQLException e) {
			return null;
		}
	}

	@Override
	public int insertDistrict(String name, int cityId, int xCord, int yCord) {
		final String sqlQuery = "INSERT INTO Opstina (Naziv, IdGra, XKoordinata, YKoordinata) VALUES (?, ?, ?, ?)";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, name);
			ps.setInt(2, cityId);
			ps.setInt(3, xCord);
			ps.setInt(4, yCord);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0)
				throw new SQLException("Creating a district failed, no rows affected.");
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next())
					return rs.getInt(1);
				else
					throw new SQLException("Creating a district failed, no ID obtained.");
			}
		} catch (SQLException e) {
			return -1;
		}
	}

}
