package rs.etf.sab.interfaces;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

import rs.etf.sab.DB.DB;
import rs.etf.sab.operations.PackageOperations;

public class MyPackageOperations implements PackageOperations {
	
	private Connection connection = DB.getInstance().getConnection();
	
	private class Offer {
		private String courierUsername;
		private int packageId;
		private double discount;
		public Offer(String courierUsername, int packageId, double discount) {
			this.courierUsername = courierUsername;
			this.packageId = packageId;
			this.discount = discount;
		}
	}
	
	private Offer getOffer(int offerId) {
		final String sqlQuery = "SELECT KorisnickoIme, IdPak, Procenat FROM Ponuda WHERE IdPon=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, offerId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) 
					return new Offer(rs.getString("KorisnickoIme"), rs.getInt("IdPak"), rs.getBigDecimal("Procenat").doubleValue());
				else return null;
			}
		} catch (SQLException e) {
			return null;
		}
	}
	
	private class PriceFactors {
		double startingPrice, weightFactor, pricePerKG;
		public PriceFactors(double startingPrice, double weightFactor, double pricePerKG) {
			this.startingPrice = startingPrice;
			this.weightFactor = weightFactor;
			this.pricePerKG = pricePerKG;
		}
	}
	
	private PriceFactors getPriceFactors(int packageType) {
		switch (packageType) {
		case 0:
			return new PriceFactors(10.0D, 0.0D, 0.0D);
		case 1:
			return new PriceFactors(25.0D, 1.0D, 100.0D);
		case 2:
			return new PriceFactors(75.0D, 2.0D, 300.0D);
		default:
			return null; // Unreachable code
		}
	}
	
	private class DistrictCoordinates {
		private int xCord, yCord;
		public DistrictCoordinates(int xCord, int yCord) {
			this.xCord = xCord;
			this.yCord = yCord;
		}
	}
	
	private DistrictCoordinates getDistrictCoordinates(int districtId) {
		final String sqlQuery = "SELECT XKoordinata, YKoordinata FROM Opstina WHERE IdOps=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, districtId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					int xCord = rs.getInt("XKoordinata"), yCord = rs.getInt("YKoordinata");
					return new DistrictCoordinates(xCord, yCord);
				}
				else return null;
			}
		} catch (SQLException e) {
			return null;
		}
	}
	
	private double euclidDistance(DistrictCoordinates districtFromCoordinates, DistrictCoordinates districtToCoordinates) {
		return Math.sqrt(Math.pow(districtToCoordinates.xCord - districtFromCoordinates.xCord, 2)
				+ Math.pow(districtToCoordinates.yCord - districtFromCoordinates.yCord, 2));
	}
	
	private BigDecimal getDeliveryPrice(int packageId, double discount) {
		final String sqlQuery = "SELECT IdOpsPre, IdOpsDos, TipPaketa, TezinaPaketa FROM Paket WHERE IdPak=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, packageId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					int districtFrom = rs.getInt("IdOpsPre"), districtTo = rs.getInt("IdOpsDos");
					DistrictCoordinates districtFromCoordinates = getDistrictCoordinates(districtFrom);
					if (districtFromCoordinates == null) return null;
					DistrictCoordinates districtToCoordinates = getDistrictCoordinates(districtTo);
					if (districtToCoordinates == null) return null;
					int packageType = rs.getInt("TipPaketa");
					PriceFactors priceFactors = getPriceFactors(packageType);
					if (priceFactors == null) return null;
					double packageWeight = rs.getBigDecimal("TezinaPaketa").doubleValue();
					double basePrice = 
						(priceFactors.startingPrice + priceFactors.weightFactor * packageWeight * priceFactors.pricePerKG) 
							* euclidDistance(districtFromCoordinates, districtToCoordinates);
					return new BigDecimal(basePrice * (1.0D + discount / 100.0D));
				}
				else return null;
			}
		} catch (SQLException e) {
			return null;
		}
	}
	
	private boolean updateNumberOfSentPackages(int packageId) {
		final String sqlQuery = "UPDATE Korisnik SET BrojPoslatihPaketa=BrojPoslatihPaketa+1 WHERE KorisnickoIme=(SELECT KorisnickoIme FROM Paket WHERE IdPak=?)";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, packageId);
			return (ps.executeUpdate() == 1); 
		} catch (SQLException e) {
			return false;
		}
	}
	
	@Override
	public boolean acceptAnOffer(int offerId) {
		Offer offer = getOffer(offerId);
		if (offer == null) return false;
		BigDecimal deliveryPrice = getDeliveryPrice(offer.packageId, offer.discount);
		String sqlQuery = "UPDATE Paket SET StatusIsporuke=1, VremePrihvatanjaZahteva=GETDATE(), Cena=?, Kurir=? WHERE IdPak=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setBigDecimal(1, deliveryPrice);
			ps.setString(2, offer.courierUsername);
			ps.setInt(3, offer.packageId);
			if (ps.executeUpdate() != 1) return false; 
		} catch (SQLException e) {
			return false;
		}
		return updateNumberOfSentPackages(offer.packageId);
	}

	@Override
	public boolean changeType(int packageId, int newType) {
		if (newType < 0 || newType > 2) return false;
		final String sqlQuery = "UPDATE Paket SET TipPaketa=? WHERE IdPak=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, newType);
			ps.setInt(2, packageId);
			return (ps.executeUpdate() == 1); 
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public boolean changeWeight(int packageId, BigDecimal newWeight) {
		if (newWeight.doubleValue() < 0.0) return false;
		final String sqlQuery = "UPDATE Paket SET TezinaPaketa=? WHERE IdPak=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setBigDecimal(1, newWeight);
			ps.setInt(2, packageId);
			return (ps.executeUpdate() == 1); 
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public boolean deletePackage(int packageId) {
		final String sqlQuery = "DELETE FROM Paket WHERE IdPak=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, packageId);
			return (ps.executeUpdate() == 1);
		} catch (SQLException e) {
			return false;
		}
	}

	private boolean courierCurrentlyDriving(String courierUsername) {
		final String sqlQuery = "SELECT Status FROM Kurir WHERE KorisnickoIme=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, courierUsername);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) 
					return (rs.getInt("Status") == 1);
				else return false;
			}
		} catch (SQLException e) {
			return false;
		}
	}
	
	private boolean startTheDrive(String courierUsername) {
		// Update courier status to currently driving
		String sqlQuery = "UPDATE Kurir SET Status=1 WHERE KorisnickoIme=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, courierUsername);
			if (ps.executeUpdate() != 1)
				return false; 
		} catch (SQLException e) {
			return false;
		}
		// Set all of the courier's packages' statuses to picked up
		sqlQuery = "UPDATE Paket SET StatusIsporuke=2 WHERE StatusIsporuke=1 AND Kurir=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, courierUsername);
			if (ps.executeUpdate() == 0) return false; 
		} catch (SQLException e) {
			return false;
		}
		// Add all of the packages to be delivered to temporary table Vozi
		sqlQuery = "SELECT IdPak FROM Paket WHERE StatusIsporuke=2 AND Kurir=?";
		try (PreparedStatement psSelect = connection.prepareStatement(sqlQuery)) {
			psSelect.setString(1, courierUsername);
			try (ResultSet rs = psSelect.executeQuery()) {
				while (rs.next()) {
					int packageId = rs.getInt("IdPak");
					final String sqlInsertQuery = "INSERT INTO Vozi(KorisnickoIme, IdPak) VALUES (?, ?)";
					try (PreparedStatement psInsert = connection.prepareStatement(sqlInsertQuery)) {
						psInsert.setString(1, courierUsername);
						psInsert.setInt(2, packageId);
						if (psInsert.executeUpdate() == 0) return false;
					}
				}
				return true;
			}
		} catch (SQLException e) {
			return false;
		}
	}
	
	private boolean deliverThePackage(int packageId) {
		final String sqlQuery = "UPDATE Paket SET StatusIsporuke=3 WHERE IdPak=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, packageId);
			return (ps.executeUpdate() == 1); 
		} catch (SQLException e) {
			return false;
		}
	}
	
	private boolean updateNumberOfPackagesDelivered(String courierUsername) {
		final String sqlQuery = "UPDATE Kurir SET BrojIsporucenihPaketa=BrojIsporucenihPaketa+1 WHERE KorisnickoIme=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, courierUsername);
			return (ps.executeUpdate() == 1); 
		} catch (SQLException e) {
			return false;
		}
	}
	
	private class ConsumptionFactors {
		double consumption, pricePerL;
		public ConsumptionFactors(double consumption, double pricePerL) {
			this.consumption = consumption;
			this.pricePerL = pricePerL;
		}
	}
	
	private ConsumptionFactors getConsumptionFactors(String courierUsername) {
		final String sqlQuery = "SELECT V.Potrosnja, V.TipGoriva FROM Kurir K JOIN Vozilo V ON K.RegistracioniBroj=V.RegistracioniBroj WHERE KorisnickoIme=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, courierUsername);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					double consumption = rs.getBigDecimal("Potrosnja").doubleValue();
					int fuelType = rs.getInt("TipGoriva");
					switch (fuelType) {
					case 0:
						return new ConsumptionFactors(consumption, 15.0D);
					case 1:
						return new ConsumptionFactors(consumption, 32.0D);
					case 2:
						return new ConsumptionFactors(consumption, 36.0D);
					default:
						return null; // Unreachable code
					}
				}
				else return null;
			}
		} catch (SQLException e) {
			return null;
		}
	}
	
	private BigDecimal getLoss(String courierUsername) {
		ConsumptionFactors consumptionFactors = getConsumptionFactors(courierUsername);
		final String sqlQuery = "SELECT P.IdOpsPre, P.IdOpsDos FROM Vozi V JOIN Paket P ON V.IdPak=P.IdPak WHERE V.KorisnickoIme=? ORDER BY P.VremePrihvatanjaZahteva";
		double loss = 0.0;
		DistrictCoordinates currentDistrictCoordinates = null;
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, courierUsername);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int districtFrom = rs.getInt("IdOpsPre"), districtTo = rs.getInt("IdOpsDos");
					DistrictCoordinates districtFromCoordinates = getDistrictCoordinates(districtFrom);
					if (districtFromCoordinates == null) return null;
					DistrictCoordinates districtToCoordinates = getDistrictCoordinates(districtTo);
					if (districtToCoordinates == null) return null;
					// Get to district from
					if (currentDistrictCoordinates != null) {
						BigDecimal euclidDistance = new BigDecimal(euclidDistance(currentDistrictCoordinates, districtFromCoordinates));
						loss += euclidDistance.doubleValue() * consumptionFactors.consumption * consumptionFactors.pricePerL;
					}
					// Get to district to
					BigDecimal euclidDistance = new BigDecimal(euclidDistance(districtFromCoordinates, districtToCoordinates));
					loss += euclidDistance.doubleValue() * consumptionFactors.consumption * consumptionFactors.pricePerL;
					// Update the current district
					currentDistrictCoordinates = districtToCoordinates;
				}
				return new BigDecimal(loss);
			}
		} catch (SQLException e) {
			return null;
		}
	}
	
	private BigDecimal calculateProfit(String courierUsername) {
		final String sqlQuery = "SELECT P.IdOpsPre, P.IdOpsDos, P.Cena FROM Vozi V JOIN Paket P ON V.IdPak=P.IdPak WHERE V.KorisnickoIme=?";
		double profit = 0.0;
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, courierUsername);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					BigDecimal price = rs.getBigDecimal("Cena");
					profit += price.doubleValue();
				}
				BigDecimal loss = getLoss(courierUsername);
				if (loss == null) return null;
				profit -= loss.doubleValue();
				return new BigDecimal(profit);
			}
		} catch (SQLException e) {
			return null;
		}
	}
	
	private boolean hasMorePackagesToDrive(String courierUsername) {
		final String sqlQuery = "SELECT TOP 1 IdPak FROM Paket WHERE Kurir=? AND StatusIsporuke=2";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, courierUsername);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() == true;
			}
		} catch (SQLException e) {
			return true;
		}
	}
	
	private boolean stopTheDrive(String courierUsername) {
		String sqlQuery = "UPDATE Kurir SET Status=0, OstvarenProfit=OstvarenProfit+? WHERE KorisnickoIme=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			BigDecimal profit = calculateProfit(courierUsername);
			if (profit == null) return false;
			ps.setBigDecimal(1, profit);
			ps.setString(2, courierUsername);
			if (ps.executeUpdate() != 1) return false; 
		} catch (SQLException e) {
			return false;
		}
		sqlQuery = "DELETE FROM Vozi WHERE KorisnickoIme=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, courierUsername);
			return (ps.executeUpdate() != 0);
		} catch (SQLException e) {
			return false;
		}
	}
	
	@Override
	public int driveNextPackage(String courierUsername) {
		if (courierCurrentlyDriving(courierUsername) == false)
			if (startTheDrive(courierUsername) == false) return -2;
		final String sqlQuery = "SELECT TOP 1 IdPak FROM Paket WHERE Kurir=? AND StatusIsporuke=2 ORDER BY VremePrihvatanjaZahteva";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, courierUsername);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					int deliveredPackageId = rs.getInt("IdPak");
					if (deliverThePackage(deliveredPackageId) == false) return -2;
					if (updateNumberOfPackagesDelivered(courierUsername) == false) return -2;
					if (hasMorePackagesToDrive(courierUsername) == false)
						if (stopTheDrive(courierUsername) == false) return -2;
					return deliveredPackageId;
				}
				else {
					if (stopTheDrive(courierUsername) == false) return -2;
					return -1;
				}
			}
		} catch (SQLException e) {
			return -2;
		}
	}

	@Override
	public java.sql.Date getAcceptanceTime(int packageId) {
		final String sqlQuery = "SELECT VremePrihvatanjaZahteva FROM Paket WHERE IdPak=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, packageId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) 
					return rs.getDate("VremePrihvatanjaZahteva");
				else return null;
			}
		} catch (SQLException e) {
			return null;
		}
	}

	@Override
	public List<Integer> getAllOffers() {
		List<Integer> offerIds = new ArrayList<>();
		final String sqlQuery = "SELECT IdPon FROM Ponuda";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sqlQuery)) {
			while (rs.next()) {
				int offerId = rs.getInt("IdPon");
				offerIds.add(offerId);
			}
		} catch (SQLException e) {
			return null;
		}
		return offerIds;
	}
	
	private class MyPair implements Pair<Integer, BigDecimal> {
		
		private int offerId;
		private BigDecimal offerDiscount;
		
		public MyPair(int offerId, BigDecimal offerDiscount) {
			this.offerId = offerId;
			this.offerDiscount = offerDiscount;
		}
		
		@Override
		public Integer getFirstParam() {
			return offerId;
		}

		@Override
		public BigDecimal getSecondParam() {
			return offerDiscount;
		}
		
	}

	@Override
	public List<Pair<Integer, BigDecimal>> getAllOffersForPackage(int packageId) {
		List<Pair<Integer, BigDecimal>> allOffersForPackage = new ArrayList<>();
		final String sqlQuery = "SELECT IdPon, Procenat FROM Ponuda WHERE IdPak=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, packageId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int offerId = rs.getInt("IdPon");
					BigDecimal offerDiscount = rs.getBigDecimal("Procenat");
					allOffersForPackage.add(new MyPair(offerId, offerDiscount));
				}
			}
		} catch (SQLException e) {
			return null;
		}
		return allOffersForPackage;
	}

	@Override
	public List<Integer> getAllPackages() {
		List<Integer> packageIds = new ArrayList<>();
		final String sqlQuery = "SELECT IdPak FROM Paket";
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sqlQuery)) {
			while (rs.next()) {
				int packageId = rs.getInt("IdPak");
				packageIds.add(packageId);
			}
		} catch (SQLException e) {
			return null;
		}
		return packageIds;
	}

	@Override
	public List<Integer> getAllPackagesWithSpecificType(int type) {
		List<Integer> packagesWithSpecificType = new ArrayList<>();
		if (type < 0 || type > 2) return packagesWithSpecificType;
		final String sqlQuery = "SELECT IdPak FROM Paket WHERE TipPaketa=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, type);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int packageWithSpecificType = rs.getInt("IdPak");
					packagesWithSpecificType.add(packageWithSpecificType);
				}
			}
		} catch (SQLException e) {
			return null;
		}
		return packagesWithSpecificType;
	}

	@Override
	public Integer getDeliveryStatus(int packageId) {
		final String sqlQuery = "SELECT StatusIsporuke FROM Paket WHERE IdPak=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, packageId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) 
					return rs.getInt("StatusIsporuke");
				else return null;
			}
		} catch (SQLException e) {
			return null;
		}
	}
	
	@Override
	public List<Integer> getDrive(String courierUsername) {
		if (courierCurrentlyDriving(courierUsername) == false) return null;
		List<Integer> packageIds = new ArrayList<>();
		final String sqlQuery = "SELECT IdPak FROM Paket WHERE Kurir=? AND Status=2";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, courierUsername);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int packageId = rs.getInt("IdPak");
					packageIds.add(packageId);
				}
			}
		} catch (SQLException e) {
			return null;
		}
		return packageIds;
	}

	@Override
	public BigDecimal getPriceOfDelivery(int packageId) {
		final String sqlQuery = "SELECT Cena FROM Paket WHERE IdPak=?";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setInt(1, packageId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) 
					return rs.getBigDecimal("Cena");
				else return null;
			}
		} catch (SQLException e) {
			return null;
		}
	}

	@Override
	public int insertPackage(int districtFrom, int districtTo, String username, int packageType, BigDecimal weight) {
		if (packageType < 0 || packageType > 2 || weight.doubleValue() < 0.0) return -1;
		final String sqlQuery = "INSERT INTO Paket (IdOpsPre, IdOpsDos, KorisnickoIme, TipPaketa, TezinaPaketa, StatusIsporuke)"
				+ " VALUES (?, ?, ?, ?, ?, 0)";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, districtFrom);
			ps.setInt(2, districtTo);
			ps.setString(3, username);
			ps.setInt(4, packageType);
			ps.setBigDecimal(5, weight);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0)
				throw new SQLException("Creating a package failed, no rows affected.");
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next())
					return rs.getInt(1);
				else
					throw new SQLException("Creating a package failed, no ID obtained.");
			}
		} catch (SQLException e) {
			return -1;
		}
	}
	
	private boolean courierAvailable(String courierUsername) {
		final String sqlQuery = "SELECT * FROM Kurir WHERE KorisnickoIme=? AND Status=0";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery)) {
			ps.setString(1, courierUsername);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public int insertTransportOffer(String courierUsername, int packageId, BigDecimal pricePercentage) {
		final double pricePercentageValue = pricePercentage.doubleValue();
		if (pricePercentageValue < -10.0 || pricePercentageValue > 10.0 || 
				courierAvailable(courierUsername) == false) return -1;
		final String sqlQuery = "INSERT INTO Ponuda (KorisnickoIme, IdPak, Procenat) VALUES (?, ?, ?)";
		try (PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, courierUsername);
			ps.setInt(2, packageId);
			ps.setBigDecimal(3, pricePercentage);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0)
				throw new SQLException("Creating a transport offer failed, no rows affected.");
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next())
					return rs.getInt(1);
				else
					throw new SQLException("Creating a transport offer failed, no ID obtained.");
			}
		} catch (SQLException e) {
			return -1;
		}
	}

}
