CREATE TABLE document (
    idDocument      int PRIMARY KEY DEFAULT nextval('serial'),
    nom       varchar(40) NOT NULL,
);

CREATE TABLE terme (
    idTerme   int PRIMARY KEY DEFAULT nextval('serial'),
    nom       varchar(40) NOT NULL,
    nb        int NOT NULL DEFAULT 0
);

CREATE TABLE association (
    idDocument 	int,
	idTerme   	int,
    nom       	varchar(40) NOT NULL,
	PRIMARY KEY (idDocument, idTerme),
	CONSTRAINT association_idTerme_fkey FOREIGN KEY (idTerme)
      REFERENCES terme (idTerme),
	CONSTRAINT association_idDocument_fkey FOREIGN KEY (idDocument)
      REFERENCES document (idDocument)
);
