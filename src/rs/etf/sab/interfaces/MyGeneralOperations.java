package rs.etf.sab.interfaces;

import java.sql.*;

import rs.etf.sab.DB.DB;
import rs.etf.sab.operations.GeneralOperations;

public class MyGeneralOperations implements GeneralOperations {

	private Connection connection = DB.getInstance().getConnection();
	
	private void eraseAllPackages() {
		String sqlQuery = "DELETE FROM Paket";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sqlQuery)) {
		} catch (SQLException e) {
			return;
		}
	}
	
	private void eraseAllDistricts() {
		String sqlQuery = "DELETE FROM Opstina";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sqlQuery)) {
		} catch (SQLException e) {
			return;
		}
	}
	
	private void eraseAllCities() {
		String sqlQuery = "DELETE FROM Grad";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sqlQuery)) {
		} catch (SQLException e) {
			return;
		}
	}
	
	private void eraseAllOffers() {
		String sqlQuery = "DELETE FROM Ponuda";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sqlQuery)) {
		} catch (SQLException e) {
			return;
		}
	}
	
	private void eraseFromVozi() {
		String sqlQuery = "DELETE FROM Vozi";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sqlQuery)) {
		} catch (SQLException e) {
			return;
		}
	}
	
	private void eraseAllCourierRequests() {
		String sqlQuery = "DELETE FROM ZahtevKurir";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sqlQuery)) {
		} catch (SQLException e) {
			return;
		}
	}
	
	private void eraseAllUsers() {
		String sqlQuery = "DELETE FROM Korisnik";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sqlQuery)) {
		} catch (SQLException e) {
			return;
		}
	}
	
	private void eraseAllVehicles() {
		String sqlQuery = "DELETE FROM Vozilo";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sqlQuery)) {
		} catch (SQLException e) {
			return;
		}
	}
	
	@Override
	public void eraseAll() {
		eraseAllPackages();
		eraseAllDistricts();
		eraseAllCities();
		eraseAllOffers();
		eraseFromVozi();
		eraseAllCourierRequests();
		eraseAllUsers();
		eraseAllVehicles();
	}

}
