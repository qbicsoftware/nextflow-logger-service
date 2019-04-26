DROP SCHEMA IF EXISTS `workflows` ;

CREATE SCHEMA IF NOT EXISTS `workflows` ;
USE `workflows` ;

CREATE TABLE IF NOT EXISTS `workflows`.`runs` (
  `id` INTEGER PRIMARY KEY AUTO_INCREMENT,
  `runId` VARCHAR(36),
  `name` VARCHAR(45),
  `lastRecordedEvent` VARCHAR(45),
  `startTime` DATETIME,
  `parameters` MEDIUMTEXT,
  `workDir` VARCHAR(256),
  `container` VARCHAR(45) NULL DEFAULT NULL,
  `user` VARCHAR(45) NULL DEFAULT NULL,
  `manifest` BLOB NULL DEFAULT NULL,
  `revision` VARCHAR(45) NULL DEFAULT NULL,
  `duration` INT(11) NULL DEFAULT NULL,
  `success` TINYINT(1) NULL DEFAULT NULL,
  `resume` TINYINT(1) NULL DEFAULT NULL,
  `nextflowVersion` VARCHAR(45) NULL DEFAULT NULL,
  `errorMessage` MEDIUMTEXT NULL DEFAULT NULL,
  UNIQUE INDEX `runId_UNIQUE` (`runId` ASC))
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `workflows`.`traces` (
  `id` INT(11) AUTO_INCREMENT,
  `runId` INT(11),
  `taskId` INT(11),
  `name` VARCHAR(45),
  `status` VARCHAR(45),
  `exit` INT(11),
  `attempt` INT(11),
  `queue` VARCHAR(45),
  `memory` INT(11),
  `duration` BIGINT(20),
  `cpus` INT(11),
  `submissionTime` DATETIME,
  `startTime` DATETIME,
  PRIMARY KEY (`id`),
  CONSTRAINT `runId`
    FOREIGN KEY (`runId`)
    REFERENCES `workflows`.`runs` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;
