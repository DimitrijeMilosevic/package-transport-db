CREATE PROCEDURE spDeleteCourierRequest
	@Username VARCHAR(100)
AS
BEGIN
	DELETE FROM ZahtevKurir
	WHERE KorisnickoIme = @Username
END
GO
