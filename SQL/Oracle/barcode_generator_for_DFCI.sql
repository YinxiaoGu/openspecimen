CREATE OR REPLACE TRIGGER SPECIMEN_BARCODE_FOR_DFCI 
BEFORE INSERT
ON catissue_specimen 
FOR EACH ROW
BEGIN 
SELECT :NEW.IDENTIFIER INTO :NEW.BARCODE from dual; 
END; 
/
CREATE OR REPLACE TRIGGER CPR_BARCODE_FOR_DFCI 
BEFORE INSERT 
ON  catissue_specimen_coll_group
FOR EACH ROW
BEGIN 
SELECT :NEW.IDENTIFIER INTO :NEW.BARCODE from dual; 
END;
/
CREATE OR REPLACE TRIGGER ST_CONTAINER_BARCODE_FOR_DFCI 
BEFORE INSERT  
ON catissue_storage_container 
FOR EACH ROW
BEGIN 
SELECT :NEW.IDENTIFIER INTO :NEW.BARCODE from dual; 
END;
/
CREATE OR REPLACE TRIGGER SHIPPING_BARCODE_FOR_DFCI 
AFTER INSERT  
ON catissue_shipment 
FOR EACH ROW
BEGIN 
SELECT :NEW.IDENTIFIER INTO :NEW.BARCODE from dual; 
END;
/