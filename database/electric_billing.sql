-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jun 01, 2025 at 03:42 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `electric_billing`
--

-- --------------------------------------------------------

--
-- Table structure for table `admin`
--

CREATE TABLE `admin` (
  `id` int(11) NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `contact` varchar(20) DEFAULT NULL,
  `email` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `admin`
--

INSERT INTO `admin` (`id`, `username`, `password`, `name`, `contact`, `email`) VALUES
(1, 'Ash', '1234', 'Ashlene', '09462431531', 'laineblaire@gmail.com');

-- --------------------------------------------------------

--
-- Table structure for table `bills`
--

CREATE TABLE `bills` (
  `id` int(11) NOT NULL,
  `customer_name` varchar(255) NOT NULL,
  `customer_address` varchar(255) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `meter_no` text NOT NULL,
  `billing_date` date DEFAULT NULL,
  `kwh_used` int(11) DEFAULT NULL,
  `rate_per_kwh` decimal(6,4) DEFAULT NULL,
  `energy_cost` decimal(10,2) DEFAULT NULL,
  `discount` decimal(10,2) DEFAULT NULL,
  `vat_sales` decimal(10,2) DEFAULT NULL,
  `vat_amount` decimal(10,2) DEFAULT NULL,
  `total_amount` decimal(10,2) DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  `status` varchar(20) DEFAULT 'Unpaid',
  `is_paid` tinyint(1) NOT NULL DEFAULT 0,
  `penalty` decimal(10,2) DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bills`
--

INSERT INTO `bills` (`id`, `customer_name`, `customer_address`, `customer_id`, `meter_no`, `billing_date`, `kwh_used`, `rate_per_kwh`, `energy_cost`, `discount`, `vat_sales`, `vat_amount`, `total_amount`, `due_date`, `status`, `is_paid`, `penalty`) VALUES
(5, 'asdf', 'hgff', 10, 'MTR-00001', '2025-06-01', 100, 12.0000, 1200.00, 60.00, 1140.00, 136.80, 1276.80, '2025-06-06', 'Unpaid', 1, 0.00),
(6, 'aggs', 'sbahs', 11, 'MTR-00002', '2025-06-01', 200, 12.0000, 2400.00, 120.00, 2280.00, 273.60, 2553.60, '2025-06-06', 'Unpaid', 0, 0.00);

-- --------------------------------------------------------

--
-- Table structure for table `customers`
--

CREATE TABLE `customers` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `address` varchar(255) NOT NULL,
  `contact` varchar(20) DEFAULT NULL,
  `senior` tinyint(1) NOT NULL,
  `units_consumed` int(11) NOT NULL,
  `month` varchar(255) NOT NULL,
  `meter_no` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `created_at` date DEFAULT curdate(),
  `action_status` varchar(50) DEFAULT 'Unpaid'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`id`, `name`, `address`, `contact`, `senior`, `units_consumed`, `month`, `meter_no`, `status`, `created_at`, `action_status`) VALUES
(11, 'aggs', 'sbahs', 'gsga', 1, 200, 'June', 'MTR-00002', 'Up to date', '2025-06-01', 'Pending');

-- --------------------------------------------------------

--
-- Table structure for table `readings`
--

CREATE TABLE `readings` (
  `id` int(11) NOT NULL,
  `meter_no` text NOT NULL,
  `reading_date` text NOT NULL,
  `reading_value` int(11) NOT NULL,
  `previous_reading_value` int(11) DEFAULT NULL,
  `units_consumed` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `readings`
--

INSERT INTO `readings` (`id`, `meter_no`, `reading_date`, `reading_value`, `previous_reading_value`, `units_consumed`) VALUES
(6, 'MTR-00001', '2025-06-01', 100, 0, 100),
(7, 'MTR-00002', '2025-06-01', 200, 0, 200);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admin`
--
ALTER TABLE `admin`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `bills`
--
ALTER TABLE `bills`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `customers`
--
ALTER TABLE `customers`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `readings`
--
ALTER TABLE `readings`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admin`
--
ALTER TABLE `admin`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `bills`
--
ALTER TABLE `bills`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `readings`
--
ALTER TABLE `readings`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
