package rs.etf.sab.interfaces;

import java.util.*;
import java.sql.*;

import rs.etf.sab.DB.DB;
import rs.etf.sab.operations.UserOperations;

public class MyUserOperations implements UserOperations {

	private Connection connection = DB.getInstance().getConnection();
	
	@Override
	public int declareAdmin(String username) {
		String sqlQuery = "SELECT * FROM Korisnik WHERE KorisnickoIme=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, username);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next() == false) return 2;
			}
		} catch (SQLException e) {
			return 2;
		}
		sqlQuery = "SELECT * FROM Administrator WHERE KorisnickoIme=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, username);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next() == true) return 1;
			}
		} catch (SQLException e) {
			return 1;
		}
		sqlQuery = "INSERT INTO Administrator (KorisnickoIme) VALUES (?)";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, username);
			return (ps.executeUpdate() == 1) ? 0 : -1;
		} catch (SQLException e) {
			return -1;
		}
	}
	
	@Override
	public int deleteUsers(String... usernames) {
		final String sqlQuery = "DELETE FROM Korisnik WHERE KorisnickoIme=?";
		int affectedRows = 0;
		for (String username : usernames) {
			try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
				ps.setString(1, username);
				affectedRows += ps.executeUpdate();
			} catch (SQLException e) {
				// ...
			}
		}
		return affectedRows;
	}

	@Override
	public List<String> getAllUsers() {
		List<String> allUsers = new ArrayList<>();
		final String sqlQuery = "SELECT KorisnickoIme FROM Korisnik";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sqlQuery)) {
			while (rs.next()) {
				String user = rs.getString("KorisnickoIme");
				allUsers.add(user);
			}
			return allUsers;
		} catch (SQLException e) {
			return null;
		}
	}

	@Override
	public Integer getSentPackages(String... usernames) {
		Set<String> distinctUsernames = new HashSet<>();
		for (String username : usernames)
			distinctUsernames.add(username);
		boolean anyUserFound = false;
		int numberOfSentPackages = 0;
		final String sqlQuery = "SELECT BrojPoslatihPaketa FROM Korisnik WHERE KorisnickoIme=?";
		for (String username : distinctUsernames) {
			try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
				ps.setString(1, username);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						if (anyUserFound == false) anyUserFound = true;
						numberOfSentPackages += rs.getInt("BrojPoslatihPaketa");
					}
				}
			} catch (SQLException e) {
				return null;
			}
		}
		return (anyUserFound == true) ? numberOfSentPackages : null;
	}

	private boolean isCapital(char c) {
		return c >= 'A' && c <= 'Z';
	}
	
	private boolean passwordValid(String password) {
		if (password.length() < 8) return false;
		boolean containsLetters = false, containsNumbers = false;
		for (int i = 0; i < password.length(); i++) {
			char c = password.charAt(i);
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) containsLetters = true;
			else if (c >= '0' && c <= '9') containsNumbers = true;
		}
		return containsLetters == true && containsNumbers == true;
	}
	
	@Override
	public boolean insertUser(String username, String firstName, String lastName, String password) {
		if (isCapital(firstName.charAt(0)) == false || isCapital(lastName.charAt(0)) == false ||
				passwordValid(password) == false) return false;
		final String sqlQuery = "INSERT INTO Korisnik (KorisnickoIme, Ime, Prezime, Sifra, BrojPoslatihPaketa)"
				+ " VALUES (?, ?, ?, ?, 0)";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, username);
			ps.setString(2, firstName);
			ps.setString(3, lastName);
			ps.setString(4, password);
			return (ps.executeUpdate() == 1);
		} catch (SQLException e) {
			return false;
		}
	}

}
