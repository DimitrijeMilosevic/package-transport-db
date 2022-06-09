CREATE TRIGGER TR_TransportOffer_OfferAccepted 
   ON  Paket 
   AFTER UPDATE
AS 
BEGIN
	SET NOCOUNT ON

	DECLARE @CursorPaket CURSOR
	DECLARE @IdPak INT

	SET @CursorPaket = CURSOR FOR
	SELECT IdPak
	FROM inserted
	WHERE StatusIsporuke=1

	OPEN @CursorPaket

	FETCH NEXT FROM @CursorPaket
	INTO @IdPak

	WHILE @@FETCH_STATUS = 0
	BEGIN
		-- Delete all the offers for the package
		DELETE FROM Ponuda
		WHERE IdPak=@IdPak

		FETCH NEXT FROM @CursorPaket
		INTO @IdPak
	END

	CLOSE @CursorPaket
	DEALLOCATE @CursorPaket
END
GO
