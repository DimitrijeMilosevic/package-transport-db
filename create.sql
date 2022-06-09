
CREATE TABLE [Administrator]
( 
	[KorisnickoIme]      varchar(100)  NOT NULL 
)
go

CREATE TABLE [Grad]
( 
	[IdGra]              integer  IDENTITY  NOT NULL ,
	[Naziv]              varchar(100)  NOT NULL ,
	[PostanskiBroj]      varchar(100)  NOT NULL 
)
go

CREATE TABLE [Korisnik]
( 
	[Ime]                varchar(100)  NOT NULL ,
	[Prezime]            varchar(100)  NOT NULL ,
	[KorisnickoIme]      varchar(100)  NOT NULL ,
	[Sifra]              varchar(100)  NOT NULL ,
	[BrojPoslatihPaketa] integer  NOT NULL 
	CONSTRAINT [BrojPoslatihPaketaDefault_202805978]
		 DEFAULT  0
	CONSTRAINT [GreaterThanZero_119350573]
		CHECK  ( BrojPoslatihPaketa >= 0 )
)
go

CREATE TABLE [Kurir]
( 
	[KorisnickoIme]      varchar(100)  NOT NULL ,
	[BrojIsporucenihPaketa] integer  NOT NULL 
	CONSTRAINT [BrojIsporucenihPaketaDefault_862233731]
		 DEFAULT  0
	CONSTRAINT [GreaterThanZero_1255397770]
		CHECK  ( BrojIsporucenihPaketa >= 0 ),
	[OstvarenProfit]     decimal(10,3)  NOT NULL 
	CONSTRAINT [OstvarenProfitDefault_920869880]
		 DEFAULT  0,
	[Status]             integer  NOT NULL 
	CONSTRAINT [StatusValidationRule_949864860]
		CHECK  ( [Status]=0 OR [Status]=1 ),
	[RegistracioniBroj]  varchar(100)  NOT NULL 
)
go

CREATE TABLE [Opstina]
( 
	[IdOps]              integer  IDENTITY  NOT NULL ,
	[Naziv]              varchar(100)  NOT NULL ,
	[XKoordinata]        integer  NOT NULL ,
	[YKoordinata]        integer  NOT NULL ,
	[IdGra]              integer  NOT NULL 
)
go

CREATE TABLE [Paket]
( 
	[IdPak]              integer  IDENTITY  NOT NULL ,
	[KorisnickoIme]      varchar(100)  NOT NULL ,
	[Kurir]              varchar(100)  NULL ,
	[IdOpsPre]           integer  NOT NULL ,
	[IdOpsDos]           integer  NOT NULL ,
	[TipPaketa]          integer  NOT NULL 
	CONSTRAINT [TipPaketaValidationRule_1889511157]
		CHECK  ( [TipPaketa]=0 OR [TipPaketa]=1 OR [TipPaketa]=2 ),
	[TezinaPaketa]       decimal(10,3)  NOT NULL 
	CONSTRAINT [GreaterThanZero_18432858]
		CHECK  ( TezinaPaketa >= 0 ),
	[StatusIsporuke]     integer  NOT NULL 
	CONSTRAINT [StatusIsporukeDefault_1071470338]
		 DEFAULT  0
	CONSTRAINT [StatusIsporukeValidationRule_1529264667]
		CHECK  ( [StatusIsporuke]=0 OR [StatusIsporuke]=1 OR [StatusIsporuke]=2 OR [StatusIsporuke]=3 ),
	[VremePrihvatanjaZahteva] datetime  NULL ,
	[Cena]               decimal(10,3)  NULL 
)
go

CREATE TABLE [Ponuda]
( 
	[IdPon]              integer  IDENTITY  NOT NULL ,
	[IdPak]              integer  NOT NULL ,
	[KorisnickoIme]      varchar(100)  NOT NULL ,
	[Procenat]           decimal(10,3)  NOT NULL 
)
go

CREATE TABLE [Vozi]
( 
	[KorisnickoIme]      varchar(100)  NOT NULL ,
	[IdPak]              integer  NOT NULL 
)
go

CREATE TABLE [Vozilo]
( 
	[RegistracioniBroj]  varchar(100)  NOT NULL ,
	[TipGoriva]          integer  NOT NULL 
	CONSTRAINT [TipGorivaValidationRule_1867313396]
		CHECK  ( [TipGoriva]=0 OR [TipGoriva]=1 OR [TipGoriva]=2 ),
	[Potrosnja]          decimal(10,3)  NOT NULL 
	CONSTRAINT [GreaterThanZero_1412017808]
		CHECK  ( Potrosnja >= 0 )
)
go

CREATE TABLE [ZahtevKurir]
( 
	[KorisnickoIme]      varchar(100)  NOT NULL ,
	[RegistracioniBroj]  varchar(100)  NOT NULL 
)
go

ALTER TABLE [Administrator]
	ADD CONSTRAINT [XPKAdministrator] PRIMARY KEY  CLUSTERED ([KorisnickoIme] ASC)
go

ALTER TABLE [Grad]
	ADD CONSTRAINT [XPKGrad] PRIMARY KEY  CLUSTERED ([IdGra] ASC)
go

ALTER TABLE [Korisnik]
	ADD CONSTRAINT [XPKKorisnik] PRIMARY KEY  CLUSTERED ([KorisnickoIme] ASC)
go

ALTER TABLE [Kurir]
	ADD CONSTRAINT [XPKKurir] PRIMARY KEY  CLUSTERED ([KorisnickoIme] ASC)
go

ALTER TABLE [Opstina]
	ADD CONSTRAINT [XPKOpstina] PRIMARY KEY  CLUSTERED ([IdOps] ASC)
go

ALTER TABLE [Paket]
	ADD CONSTRAINT [XPKPaket] PRIMARY KEY  CLUSTERED ([IdPak] ASC)
go

ALTER TABLE [Ponuda]
	ADD CONSTRAINT [XPKPonuda] PRIMARY KEY  CLUSTERED ([IdPon] ASC)
go

ALTER TABLE [Vozi]
	ADD CONSTRAINT [XPKVozi] PRIMARY KEY  CLUSTERED ([KorisnickoIme] ASC,[IdPak] ASC)
go

ALTER TABLE [Vozilo]
	ADD CONSTRAINT [XPKVozilo] PRIMARY KEY  CLUSTERED ([RegistracioniBroj] ASC)
go

ALTER TABLE [ZahtevKurir]
	ADD CONSTRAINT [XPKZahtevKurir] PRIMARY KEY  CLUSTERED ([KorisnickoIme] ASC)
go


ALTER TABLE [Administrator]
	ADD CONSTRAINT [R_3] FOREIGN KEY ([KorisnickoIme]) REFERENCES [Korisnik]([KorisnickoIme])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go


ALTER TABLE [Kurir]
	ADD CONSTRAINT [R_6] FOREIGN KEY ([KorisnickoIme]) REFERENCES [Korisnik]([KorisnickoIme])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go

ALTER TABLE [Kurir]
	ADD CONSTRAINT [R_16] FOREIGN KEY ([RegistracioniBroj]) REFERENCES [Vozilo]([RegistracioniBroj])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go


ALTER TABLE [Opstina]
	ADD CONSTRAINT [R_2] FOREIGN KEY ([IdGra]) REFERENCES [Grad]([IdGra])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go


ALTER TABLE [Paket]
	ADD CONSTRAINT [R_7] FOREIGN KEY ([KorisnickoIme]) REFERENCES [Korisnik]([KorisnickoIme])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Paket]
	ADD CONSTRAINT [R_8] FOREIGN KEY ([IdOpsPre]) REFERENCES [Opstina]([IdOps])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Paket]
	ADD CONSTRAINT [R_9] FOREIGN KEY ([IdOpsDos]) REFERENCES [Opstina]([IdOps])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Paket]
	ADD CONSTRAINT [R_20] FOREIGN KEY ([Kurir]) REFERENCES [Kurir]([KorisnickoIme])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go


ALTER TABLE [Ponuda]
	ADD CONSTRAINT [R_11] FOREIGN KEY ([IdPak]) REFERENCES [Paket]([IdPak])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go

ALTER TABLE [Ponuda]
	ADD CONSTRAINT [R_13] FOREIGN KEY ([KorisnickoIme]) REFERENCES [Kurir]([KorisnickoIme])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Vozi]
	ADD CONSTRAINT [R_21] FOREIGN KEY ([KorisnickoIme]) REFERENCES [Kurir]([KorisnickoIme])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Vozi]
	ADD CONSTRAINT [R_22] FOREIGN KEY ([IdPak]) REFERENCES [Paket]([IdPak])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [ZahtevKurir]
	ADD CONSTRAINT [R_4] FOREIGN KEY ([KorisnickoIme]) REFERENCES [Korisnik]([KorisnickoIme])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go

ALTER TABLE [ZahtevKurir]
	ADD CONSTRAINT [R_5] FOREIGN KEY ([RegistracioniBroj]) REFERENCES [Vozilo]([RegistracioniBroj])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go
