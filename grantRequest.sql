CREATE PROCEDURE spGrantRequest
	@Username VARCHAR(100)
AS
BEGIN
	DECLARE @LicenceNumber VARCHAR(100)
	SELECT @LicenceNumber = RegistracioniBroj
	FROM ZahtevKurir
	WHERE KorisnickoIme = @Username
	IF (NOT EXISTS(SELECT * FROM Kurir WHERE RegistracioniBroj = @LicenceNumber))
	BEGIN
		INSERT INTO Kurir(KorisnickoIme, BrojIsporucenihPaketa, OstvarenProfit, Status, RegistracioniBroj)
		VALUES (@Username, 0, 0, 0, @LicenceNumber)

		DELETE FROM ZahtevKurir
		WHERE KorisnickoIme = @Username
	END
END
GO
