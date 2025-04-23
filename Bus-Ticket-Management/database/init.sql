-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Nov 17, 2024 at 06:47 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12
SET
  SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";

START TRANSACTION;

SET
  time_zone = "+07:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */
;

/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */
;

/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */
;

/*!40101 SET NAMES utf8mb4 */
;

--
-- Database: `bus_ticket_management`
--
-- --------------------------------------------------------
DROP DATABASE IF EXISTS `bus_ticket_management`;

CREATE DATABASE IF NOT EXISTS `bus_ticket_management` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `bus_ticket_management`;

--
-- Table structure for table `account`
--
CREATE TABLE `account` (
  `id` int(11) NOT NULL,
  `email` varchar(50) NOT NULL,
  `password` text NOT NULL,
  `role_id` int(11) NOT NULL,
  `name` varchar(70) DEFAULT NULL,
  `phone` varchar(12) DEFAULT NULL,
  `status` enum('unverified', 'verified', 'banned') NOT NULL DEFAULT 'unverified',
  `verification_code` varchar(255) DEFAULT NULL,
  `verification_expiration` datetime DEFAULT NULL,
  `login_token` text DEFAULT NULL,
  `password_reset_token` text DEFAULT NULL,
  `password_reset_expiration` datetime DEFAULT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 1
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- Table structure for table `bill`
--
CREATE TABLE `bill` (
  `bill_id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `payment_method` enum('cash', 'vnpay') NOT NULL DEFAULT 'cash',
  `payment_date` datetime NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- Table structure for table `bill_detail`
--
CREATE TABLE `bill_detail` (
  `id` int(11) NOT NULL,
  `bill_id` int(11) NOT NULL,
  `trip_id` int(11) NOT NULL,
  `number_of_ticket` smallint(6) NOT NULL,
  `fee` float NOT NULL,
  `ticket_type` enum('one_way_ticket', 'round_trip_ticket') NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- Table structure for table `booking`
--
CREATE TABLE `booking` (
  `booking_id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `trip_id` int(11) NOT NULL,
  `number_of_seat` tinyint(6) NOT NULL,
  `is_round_trip` tinyint(1) NOT NULL,
  `round_trip_id` varchar(50) DEFAULT NULL,
  `pickup_point` varchar(100) NOT NULL,
  `dropoff_point` varchar(100) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- Table structure for table `bus`
--
CREATE TABLE `bus` (
  `bus_id` varchar(12) NOT NULL,
  `seat_type` enum('limousine', 'economy') NOT NULL DEFAULT 'economy',
  `number_of_seat` tinyint(50) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- Table structure for table `bus_seats`
--
CREATE TABLE `bus_seats` (
  `id` int(11) NOT NULL,
  `bus_id` varchar(12) NOT NULL,
  `seat_name` varchar(10) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- Table structure for table `checkpoint`
--
CREATE TABLE `checkpoint` (
  `checkpoint_id` smallint(6) NOT NULL,
  `place_name` varchar(70) NOT NULL,
  `address` varchar(100) NOT NULL,
  `province` varchar(255) DEFAULT NULL,
  `city` varchar(255) NOT NULL,
  `region` varchar(50) NOT NULL,
  `phone` varchar(12) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- Table structure for table `controller`
--
CREATE TABLE `controller` (
  `controller_id` int(11) NOT NULL,
  `account_id` int(11) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- Table structure for table `customer`
--
CREATE TABLE `customer` (
  `customer_id` int(11) NOT NULL,
  `email` varchar(255) NOT NULL,
  `name` varchar(70) NOT NULL,
  `phone` varchar(12) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- Table structure for table `driver`
--
CREATE TABLE `driver` (
  `driver_id` int(11) NOT NULL,
  `account_id` int(11) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- Table structure for table `permission`
--
CREATE TABLE `permission` (
  `permission_id` int(11) NOT NULL,
  `permission_name` varchar(50) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- Table structure for table `role`
--
CREATE TABLE `role` (
  `role_id` int(11) NOT NULL,
  `role_name` varchar(50) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- Table structure for table `role_permission`
--
CREATE TABLE `role_permission` (
  `id` int(11) NOT NULL,
  `role_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- Table structure for table `route`
--
CREATE TABLE `route` (
  `route_id` int(11) NOT NULL,
  `code` varchar(50) NOT NULL,
  `name` varchar(100) NOT NULL,
  `time` varchar(20) NOT NULL,
  `distance` float NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- Table structure for table `route_checkpoint`
--
CREATE TABLE `route_checkpoint` (
  `id` int(11) NOT NULL,
  `route_code` varchar(50) NOT NULL,
  `checkpoint_id` smallint(6) NOT NULL,
  `checkpoint_order` tinyint(15) NOT NULL,
  `type` enum('departure', 'drop_off', 'en_route', 'rest') NOT NULL DEFAULT 'departure'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- Table structure for table `seat_reservations`
--
CREATE TABLE `seat_reservations` (
  `id` int(11) NOT NULL,
  `seat_id` int(11) NOT NULL,
  `booking_id` int(11) DEFAULT NULL,
  `trip_id` int(11) NOT NULL,
  `status` enum('open', 'reserved', 'booked') NOT NULL DEFAULT 'open'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- Table structure for table `spring_session`
--
CREATE TABLE `spring_session` (
  `PRIMARY_ID` char(36) NOT NULL,
  `SESSION_ID` char(36) NOT NULL,
  `CREATION_TIME` bigint(20) NOT NULL,
  `LAST_ACCESS_TIME` bigint(20) NOT NULL,
  `MAX_INACTIVE_INTERVAL` int(11) NOT NULL,
  `EXPIRY_TIME` bigint(20) NOT NULL,
  `PRINCIPAL_NAME` varchar(100) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- Table structure for table `staff`
--
CREATE TABLE `staff` (
  `staff_id` int(11) NOT NULL,
  `account_id` int(11) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

-- --------------------------------------------------------
--
-- Table structure for table `trip`
--
CREATE TABLE `trip` (
  `trip_id` int(11) NOT NULL,
  `departure_time` datetime NOT NULL,
  `arrival_time` datetime NOT NULL,
  `price` float NOT NULL,
  `status` enum('finished', 'arriving', 'waiting') NOT NULL DEFAULT 'waiting',
  `bus_id` varchar(12) NOT NULL,
  `driver_id` int(11) NOT NULL,
  `controller_id` int(11) NOT NULL,
  `staff_id` int(11) NOT NULL,
  `route_code` varchar(50) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

--
-- Indexes for dumped tables
--
--
-- Indexes for table `account`
--
ALTER TABLE
  `account`
ADD
  PRIMARY KEY (`id`),
ADD
  UNIQUE KEY `email` (`email`),
ADD
  UNIQUE KEY `phone` (`phone`),
ADD
  KEY `id` (`id`, `role_id`),
ADD
  KEY `role_id` (`role_id`);

--
-- Indexes for table `bill`
--
ALTER TABLE
  `bill`
ADD
  PRIMARY KEY (`bill_id`),
ADD
  KEY `customer_id` (`customer_id`),
ADD
  KEY `bill_id` (`bill_id`);

--
-- Indexes for table `bill_detail`
--
ALTER TABLE
  `bill_detail`
ADD
  PRIMARY KEY (`id`),
ADD
  KEY `bill_id` (`id`, `trip_id`),
ADD
  KEY `trip_id` (`trip_id`),
ADD
  KEY `bill_id_2` (`bill_id`);

--
-- Indexes for table `booking`
--
ALTER TABLE
  `booking`
ADD
  PRIMARY KEY (`booking_id`),
ADD
  KEY `booking_id` (`booking_id`, `customer_id`, `trip_id`),
ADD
  KEY `customer_id` (`customer_id`),
ADD
  KEY `trip_id` (`trip_id`);

--
-- Indexes for table `bus`
--
ALTER TABLE
  `bus`
ADD
  PRIMARY KEY (`bus_id`),
ADD
  KEY `bus_id` (`bus_id`);

--
-- Indexes for table `bus_seats`
--
ALTER TABLE
  `bus_seats`
ADD
  PRIMARY KEY (`id`),
ADD
  KEY `id` (`id`, `bus_id`),
ADD
  KEY `bus_id` (`bus_id`);

--
-- Indexes for table `checkpoint`
--
ALTER TABLE
  `checkpoint`
ADD
  PRIMARY KEY (`checkpoint_id`),
ADD
  KEY `checkpoint_id` (`checkpoint_id`);

--
-- Indexes for table `controller`
--
ALTER TABLE
  `controller`
ADD
  PRIMARY KEY (`controller_id`),
ADD
  KEY `controller_id` (`controller_id`, `account_id`),
ADD
  KEY `account_id` (`account_id`);

--
-- Indexes for table `customer`
--
ALTER TABLE
  `customer`
ADD
  PRIMARY KEY (`customer_id`),
ADD
  KEY `customer_id` (`customer_id`);

--
-- Indexes for table `driver`
--
ALTER TABLE
  `driver`
ADD
  PRIMARY KEY (`driver_id`),
ADD
  KEY `driver_id` (`driver_id`),
ADD
  KEY `account_id` (`account_id`);

--
-- Indexes for table `permission`
--
ALTER TABLE
  `permission`
ADD
  PRIMARY KEY (`permission_id`),
ADD
  KEY `permission_id` (`permission_id`);

--
-- Indexes for table `role`
--
ALTER TABLE
  `role`
ADD
  PRIMARY KEY (`role_id`),
ADD
  KEY `role_id` (`role_id`);

--
-- Indexes for table `role_permission`
--
ALTER TABLE
  `role_permission`
ADD
  PRIMARY KEY (`id`),
ADD
  KEY `id` (`id`, `role_id`, `permission_id`),
ADD
  KEY `role_id` (`role_id`),
ADD
  KEY `permission_id` (`permission_id`);

--
-- Indexes for table `route`
--
ALTER TABLE
  `route`
ADD
  PRIMARY KEY (`route_id`),
ADD
  UNIQUE KEY `code` (`code`),
ADD
  KEY `route_id` (`route_id`);

--
-- Indexes for table `route_checkpoint`
--
ALTER TABLE
  `route_checkpoint`
ADD
  PRIMARY KEY (`id`),
ADD
  KEY `checkpoint_id` (`checkpoint_id`),
ADD
  KEY `route_code` (`route_code`);

--
-- Indexes for table `seat_reservations`
--
ALTER TABLE
  `seat_reservations`
ADD
  PRIMARY KEY (`id`),
ADD
  KEY `id` (`id`, `seat_id`, `booking_id`),
ADD
  KEY `seat_id` (`seat_id`),
ADD
  KEY `booking_id` (`booking_id`),
ADD
  KEY `seat_reservations_ibfk_3` (`trip_id`);

--
-- Indexes for table `staff`
--
ALTER TABLE
  `staff`
ADD
  PRIMARY KEY (`staff_id`),
ADD
  KEY `staff_id` (`staff_id`, `account_id`),
ADD
  KEY `account_id` (`account_id`);

--
-- Indexes for table `trip`
--
ALTER TABLE
  `trip`
ADD
  PRIMARY KEY (`trip_id`),
ADD
  KEY `bus_id` (
    `bus_id`,
    `driver_id`,
    `controller_id`,
    `staff_id`,
    `route_code`
  ),
ADD
  KEY `trip_id` (`trip_id`),
ADD
  KEY `driver_id` (`driver_id`),
ADD
  KEY `controller_id` (`controller_id`),
ADD
  KEY `staff_id` (`staff_id`),
ADD
  KEY `route_id` (`route_code`),
ADD
  KEY `route_code` (`route_code`);

--
-- AUTO_INCREMENT for dumped tables
--
--
-- AUTO_INCREMENT for table `account`
--
ALTER TABLE
  `account`
MODIFY
  `id` int(11) NOT NULL AUTO_INCREMENT,
  AUTO_INCREMENT = 21;

--
-- AUTO_INCREMENT for table `bill`
--
ALTER TABLE
  `bill`
MODIFY
  `bill_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `bill_detail`
--
ALTER TABLE
  `bill_detail`
MODIFY
  `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `booking`
--
ALTER TABLE
  `booking`
MODIFY
  `booking_id` int(11) NOT NULL AUTO_INCREMENT,
  AUTO_INCREMENT = 8990;

--
-- AUTO_INCREMENT for table `bus_seats`
--
ALTER TABLE
  `bus_seats`
MODIFY
  `id` int(11) NOT NULL AUTO_INCREMENT,
  AUTO_INCREMENT = 2845;

--
-- AUTO_INCREMENT for table `checkpoint`
--
ALTER TABLE
  `checkpoint`
MODIFY
  `checkpoint_id` smallint(6) NOT NULL AUTO_INCREMENT,
  AUTO_INCREMENT = 11;

--
-- AUTO_INCREMENT for table `controller`
--
ALTER TABLE
  `controller`
MODIFY
  `controller_id` int(11) NOT NULL AUTO_INCREMENT,
  AUTO_INCREMENT = 4;

--
-- AUTO_INCREMENT for table `customer`
--
ALTER TABLE
  `customer`
MODIFY
  `customer_id` int(11) NOT NULL AUTO_INCREMENT,
  AUTO_INCREMENT = 21;

--
-- AUTO_INCREMENT for table `driver`
--
ALTER TABLE
  `driver`
MODIFY
  `driver_id` int(11) NOT NULL AUTO_INCREMENT,
  AUTO_INCREMENT = 5;

--
-- AUTO_INCREMENT for table `permission`
--
ALTER TABLE
  `permission`
MODIFY
  `permission_id` int(11) NOT NULL AUTO_INCREMENT,
  AUTO_INCREMENT = 8;

--
-- AUTO_INCREMENT for table `role`
--
ALTER TABLE
  `role`
MODIFY
  `role_id` int(11) NOT NULL AUTO_INCREMENT,
  AUTO_INCREMENT = 6;

--
-- AUTO_INCREMENT for table `role_permission`
--
ALTER TABLE
  `role_permission`
MODIFY
  `id` int(11) NOT NULL AUTO_INCREMENT,
  AUTO_INCREMENT = 24;

--
-- AUTO_INCREMENT for table `route`
--
ALTER TABLE
  `route`
MODIFY
  `route_id` int(11) NOT NULL AUTO_INCREMENT,
  AUTO_INCREMENT = 51;

--
-- AUTO_INCREMENT for table `route_checkpoint`
--
ALTER TABLE
  `route_checkpoint`
MODIFY
  `id` int(11) NOT NULL AUTO_INCREMENT,
  AUTO_INCREMENT = 94;

--
-- AUTO_INCREMENT for table `seat_reservations`
--
ALTER TABLE
  `seat_reservations`
MODIFY
  `id` int(11) NOT NULL AUTO_INCREMENT,
  AUTO_INCREMENT = 73;

--
-- AUTO_INCREMENT for table `staff`
--
ALTER TABLE
  `staff`
MODIFY
  `staff_id` int(11) NOT NULL AUTO_INCREMENT,
  AUTO_INCREMENT = 6;

--
-- AUTO_INCREMENT for table `trip`
--
ALTER TABLE
  `trip`
MODIFY
  `trip_id` int(11) NOT NULL AUTO_INCREMENT,
  AUTO_INCREMENT = 244;

--
-- Constraints for dumped tables
--
--
-- Constraints for table `account`
--
ALTER TABLE
  `account`
ADD
  CONSTRAINT `account_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`);

--
-- Constraints for table `bill`
--
ALTER TABLE
  `bill`
ADD
  CONSTRAINT `bill_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`);

--
-- Constraints for table `bill_detail`
--
ALTER TABLE
  `bill_detail`
ADD
  CONSTRAINT `bill_detail_ibfk_1` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`trip_id`),
ADD
  CONSTRAINT `bill_detail_ibfk_2` FOREIGN KEY (`bill_id`) REFERENCES `bill` (`bill_id`);

--
-- Constraints for table `booking`
--
ALTER TABLE
  `booking`
ADD
  CONSTRAINT `booking_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE CASCADE ON UPDATE CASCADE,
ADD
  CONSTRAINT `booking_ibfk_2` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`trip_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `bus_seats`
--
ALTER TABLE
  `bus_seats`
ADD
  CONSTRAINT `bus_seats_ibfk_1` FOREIGN KEY (`bus_id`) REFERENCES `bus` (`bus_id`);

--
-- Constraints for table `controller`
--
ALTER TABLE
  `controller`
ADD
  CONSTRAINT `controller_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `driver`
--
ALTER TABLE
  `driver`
ADD
  CONSTRAINT `driver_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `role_permission`
--
ALTER TABLE
  `role_permission`
ADD
  CONSTRAINT `role_permission_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE,
ADD
  CONSTRAINT `role_permission_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `route_checkpoint`
--
ALTER TABLE
  `route_checkpoint`
ADD
  CONSTRAINT `route_checkpoint_ibfk_1` FOREIGN KEY (`checkpoint_id`) REFERENCES `checkpoint` (`checkpoint_id`),
ADD
  CONSTRAINT `route_checkpoint_ibfk_2` FOREIGN KEY (`route_code`) REFERENCES `route` (`code`);

--
-- Constraints for table `seat_reservations`
--
ALTER TABLE
  `seat_reservations`
ADD
  CONSTRAINT `seat_reservations_ibfk_1` FOREIGN KEY (`seat_id`) REFERENCES `bus_seats` (`id`),
ADD
  CONSTRAINT `seat_reservations_ibfk_2` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`),
ADD
  CONSTRAINT `seat_reservations_ibfk_3` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`trip_id`);

--
-- Constraints for table `staff`
--
ALTER TABLE
  `staff`
ADD
  CONSTRAINT `staff_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `trip`
--
ALTER TABLE
  `trip`
ADD
  CONSTRAINT `trip_ibfk_1` FOREIGN KEY (`bus_id`) REFERENCES `bus` (`bus_id`),
ADD
  CONSTRAINT `trip_ibfk_2` FOREIGN KEY (`driver_id`) REFERENCES `driver` (`driver_id`),
ADD
  CONSTRAINT `trip_ibfk_3` FOREIGN KEY (`controller_id`) REFERENCES `controller` (`controller_id`),
ADD
  CONSTRAINT `trip_ibfk_4` FOREIGN KEY (`staff_id`) REFERENCES `staff` (`staff_id`),
ADD
  CONSTRAINT `trip_ibfk_5` FOREIGN KEY (`route_code`) REFERENCES `route` (`code`);

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */
;

/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */
;

/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */
;