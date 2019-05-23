
DROP SCHEMA IF EXISTS `workflows` CASCADE ;

CREATE SCHEMA IF NOT EXISTS `workflows`;
USE `workflows` ;

CREATE TABLE IF NOT EXISTS `workflows`.`runs` (
  `id` INTEGER PRIMARY KEY AUTO_INCREMENT,
  `runId` VARCHAR(36),
  `name` VARCHAR(45),
  `lastEvent` VARCHAR(45),
  `lastRecord` DATETIME,
  UNIQUE INDEX `runId_UNIQUE` (`runId` ASC))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `workflows`.`metadata` (
  `id` INT(11) AUTO_INCREMENT,
  `runId` INT(11),
  `startTime` DATETIME,
  `parameters` TEXT,
  `workDir` VARCHAR(256),
  `container` VARCHAR(45),
  `user` VARCHAR(45),
  `manifest` CLOB,
  `revision` VARCHAR(45),
  `duration` INT(11),
  `success` TINYINT(1),
  `resume` TINYINT(1),
  `nextflowVersion` VARCHAR(45),
  `exitStatus` INT(11),
  `errorMessage` CLOB,
  PRIMARY KEY (`id`),
    FOREIGN KEY (`runId`)
    REFERENCES `workflows`.`runs` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
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
  `memory` BIGINT,
  `duration` BIGINT,
  `cpus` INT(11),
  `submissionTime` BIGINT,
  `startTime` BIGINT,
  PRIMARY KEY (`id`),
    FOREIGN KEY (`runId`)
    REFERENCES `workflows`.`runs` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;
