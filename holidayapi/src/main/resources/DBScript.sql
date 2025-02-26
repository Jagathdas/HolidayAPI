USE holiday_db;


CREATE TABLE country (
    country_code VARCHAR(150) PRIMARY KEY,
    country_name VARCHAR(255) UNIQUE
);


CREATE TABLE federal_holidays (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    country_code VARCHAR(150) NOT NULL ,
    country_name VARCHAR(255) NOT NULL ,
    holiday_name VARCHAR(255) NOT NULL ,
    holiday_date DATE NOT NULL ,
    day_of_week VARCHAR(20) NOT NULL ,
    FOREIGN KEY (country_code) REFERENCES country(country_code),
   UNIQUE (country_code, country_name, holiday_date)
);

DELIMITER $$

CREATE TRIGGER set_day_of_week BEFORE INSERT ON federal_holidays
FOR EACH ROW
BEGIN`country``country`
    SET NEW.day_of_week = DAYNAME(NEW.holiday_date);
END $$

DELIMITER ;




CREATE TABLE `Federal_holidays` (
  id INT AUTO_INCREMENT PRIMARY KEY,
  ---- ADD country CODE - FOregien KEY
  country VARCHAR(50) NOT NULL,  ----country NAME
  NAME VARCHAR(100) NOT NULL, -- hiliday name
  DATE DATE NOT NULL,  --- holiday DATE
  UNIQUE (countrycode, countryname, DATE)
  
  based DATE get DAY OF the WEEK (monday,saturday)
);

DROP TABLE federal_holidays;

