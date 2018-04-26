-- phpMyAdmin SQL Dump
-- version 4.7.2
-- https://www.phpmyadmin.net/
--
-- 主機: 172.16.157.11
-- 產生時間： 2018 年 04 月 25 日 02:33
-- 伺服器版本: 10.1.22-MariaDB-1~jessie
-- PHP 版本： 7.0.16

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 資料庫： `kado`
--

-- --------------------------------------------------------

--
-- 資料表結構 `Chart`
--

CREATE TABLE `Chart` (
  `Number` int(11) NOT NULL,
  `JobID` int(11) NOT NULL,
  `Type` varchar(10) COLLATE utf8mb4_bin NOT NULL,
  `Chart_Name` varchar(150) COLLATE utf8mb4_bin NOT NULL,
  `Chart_Setting` text COLLATE utf8mb4_bin NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- 資料表結構 `Groups`
--

CREATE TABLE `Groups` (
  `GID` int(11) NOT NULL,
  `GroupName` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `Memo` varchar(100) COLLATE utf8mb4_bin NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- 資料表結構 `Job`
--

CREATE TABLE `Job` (
  `JobID` int(11) NOT NULL,
  `JobName` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `JobOwner` int(11) DEFAULT '0',
  `JobLevel` int(11) NOT NULL,
  `JobMemo` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL,
  `Notification` tinyint(1) NOT NULL DEFAULT '0',
  `JobStorageType` int(11) DEFAULT '0',
  `StorageResources` int(11) DEFAULT '0',
  `FilePath` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL,
  `FileName` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL,
  `DBSQL` text COLLATE utf8mb4_bin,
  `JobSQL` text COLLATE utf8mb4_bin,
  `CRTIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `Replace_Value` int(11) DEFAULT NULL,
  `Replace_Sign` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL,
  `Report` tinyint(1) NOT NULL DEFAULT '0',
  `ReportEmail` text COLLATE utf8mb4_bin,
  `ReportLength` int(11) DEFAULT '0',
  `ReportFileType` int(11) DEFAULT '0',
  `ReportTitle` varchar(150) COLLATE utf8mb4_bin DEFAULT '',
  `ReportWhileEmpty` tinyint(1) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- 資料表結構 `Job_History`
--

CREATE TABLE `Job_History` (
  `JHID` int(11) NOT NULL,
  `JobID` int(11) NOT NULL,
  `PrestoID` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `JobOwner` int(11) NOT NULL,
  `JobLevel` int(11) NOT NULL,
  `JobStartTime` datetime NOT NULL,
  `JobStopTime` datetime DEFAULT NULL,
  `JobStatus` int(11) NOT NULL,
  `JobProgress` int(11) NOT NULL,
  `JobLog` int(11) NOT NULL,
  `CRTIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `JobType` int(11) NOT NULL,
  `Report` tinyint(1) NOT NULL,
  `ReportEmail` text COLLATE utf8mb4_bin NOT NULL,
  `ReportLength` int(11) NOT NULL,
  `ReportFileType` int(11) NOT NULL,
  `ReportTitle` varchar(150) COLLATE utf8mb4_bin NOT NULL,
  `ReportWhileEmpty` tinyint(1) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- 資料表結構 `Job_Log`
--

CREATE TABLE `Job_Log` (
  `JLID` int(11) NOT NULL,
  `JobSQL` text COLLATE utf8mb4_bin NOT NULL,
  `JobOutput` varchar(200) COLLATE utf8mb4_bin NOT NULL,
  `JobLogfile` varchar(200) COLLATE utf8mb4_bin NOT NULL,
  `JobStorageType` int(11) NOT NULL,
  `StorageResources` int(11) NOT NULL,
  `FilePath` varchar(200) COLLATE utf8mb4_bin NOT NULL,
  `FileName` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `DBSQL` text COLLATE utf8mb4_bin NOT NULL,
  `CRTIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `Replace_Value` int(11) DEFAULT NULL,
  `Replace_Sign` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL,
  `ResultCount` int(11) NOT NULL DEFAULT '0',
  `Valid` tinyint(1) NOT NULL DEFAULT '0',
  `ReportWhileEmpty` tinyint(1) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- 資料表結構 `Schedule`
--

CREATE TABLE `Schedule` (
  `ScheduleID` int(11) NOT NULL,
  `ScheduleName` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `ScheduleOwner` int(11) NOT NULL,
  `ScheduleLevel` int(11) NOT NULL,
  `ScheduleMemo` varchar(200) COLLATE utf8mb4_bin NOT NULL,
  `ScheduleStatus` int(11) NOT NULL,
  `ScheduleStartTime` datetime NOT NULL,
  `ScheduleTimeType` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL,
  `StartWith` datetime DEFAULT NULL,
  `TimeEvery` int(11) DEFAULT NULL,
  `TimeEveryType` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL,
  `TimeCycle` int(11) DEFAULT NULL,
  `TimeEach` int(11) DEFAULT NULL,
  `Notification` tinyint(1) NOT NULL,
  `CRTIME` datetime DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- 資料表結構 `Schedule_History`
--

CREATE TABLE `Schedule_History` (
  `SHID` int(11) NOT NULL,
  `ScheduleID` int(11) NOT NULL,
  `ScheduleName` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `ScheduleOwner` int(11) NOT NULL,
  `ScheduleLevel` int(11) NOT NULL,
  `ScheduleMemo` text COLLATE utf8mb4_bin NOT NULL,
  `ScheduleStatus` int(11) NOT NULL,
  `ScheduleStartTime` datetime NOT NULL,
  `ScheduleStopTime` datetime NOT NULL,
  `ScheduleLog` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL,
  `ScheduleTimeType` varchar(20) COLLATE utf8mb4_bin NOT NULL,
  `StartWith` datetime NOT NULL,
  `TimeEvery` int(11) NOT NULL,
  `TimeEveryType` varchar(20) COLLATE utf8mb4_bin NOT NULL,
  `TimeCycle` int(11) NOT NULL,
  `TimeEach` varchar(20) COLLATE utf8mb4_bin NOT NULL,
  `Notification` tinyint(1) NOT NULL,
  `CRTIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- 資料表結構 `Schedule_Job`
--

CREATE TABLE `Schedule_Job` (
  `SJID` int(11) NOT NULL,
  `ScheduleID` int(11) NOT NULL,
  `JobID` int(11) NOT NULL,
  `SortIndex` int(11) NOT NULL,
  `CRTIME` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- 資料表結構 `Schedule_Job_History`
--

CREATE TABLE `Schedule_Job_History` (
  `SJHID` int(11) NOT NULL,
  `SHID` int(11) NOT NULL,
  `JHID` int(11) NOT NULL,
  `JobID` int(11) NOT NULL,
  `SortIndex` int(11) NOT NULL,
  `CRTIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- 資料表結構 `Schedule_Time`
--

CREATE TABLE `Schedule_Time` (
  `STID` int(11) NOT NULL,
  `ScheduleID` int(11) NOT NULL,
  `Time` datetime NOT NULL,
  `Tag` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `CRTIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- 資料表結構 `SQLtemplate`
--

CREATE TABLE `SQLtemplate` (
  `JobID` int(11) NOT NULL,
  `URLKey` varchar(200) COLLATE utf8mb4_bin NOT NULL,
  `SQLKey` varchar(200) COLLATE utf8mb4_bin NOT NULL,
  `DefaultValue` varchar(100) COLLATE utf8mb4_bin NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- 資料表結構 `User`
--

CREATE TABLE `User` (
  `UID` int(11) NOT NULL,
  `AccountID` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `UserName` varchar(100) COLLATE utf8mb4_bin NOT NULL,
  `Password` varchar(150) COLLATE utf8mb4_bin NOT NULL,
  `Gid` int(11) NOT NULL,
  `Admin` tinyint(1) NOT NULL,
  `General` tinyint(1) NOT NULL,
  `Email` varchar(200) COLLATE utf8mb4_bin NOT NULL,
  `Enable` tinyint(1) NOT NULL,
  `ChartBuilder` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- 資料表結構 `User_Login`
--

CREATE TABLE `User_Login` (
  `ULID` int(11) NOT NULL,
  `UID` int(11) NOT NULL,
  `Admin` tinyint(1) NOT NULL,
  `LoginTime` datetime NOT NULL,
  `LogoutTime` datetime DEFAULT NULL,
  `Token` varchar(150) COLLATE utf8mb4_bin NOT NULL,
  `Guest` tinyint(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

--
-- 已匯出資料表的索引
--

--
-- 資料表索引 `Chart`
--
ALTER TABLE `Chart`
  ADD PRIMARY KEY (`Number`);

--
-- 資料表索引 `Groups`
--
ALTER TABLE `Groups`
  ADD PRIMARY KEY (`GID`);

--
-- 資料表索引 `Job`
--
ALTER TABLE `Job`
  ADD PRIMARY KEY (`JobID`),
  ADD KEY `JobOwner` (`JobOwner`);

--
-- 資料表索引 `Job_History`
--
ALTER TABLE `Job_History`
  ADD PRIMARY KEY (`JHID`),
  ADD KEY `JobStartTime` (`JobStartTime`),
  ADD KEY `JobID` (`JobID`);

--
-- 資料表索引 `Job_Log`
--
ALTER TABLE `Job_Log`
  ADD PRIMARY KEY (`JLID`);

--
-- 資料表索引 `Schedule`
--
ALTER TABLE `Schedule`
  ADD PRIMARY KEY (`ScheduleID`);

--
-- 資料表索引 `Schedule_History`
--
ALTER TABLE `Schedule_History`
  ADD PRIMARY KEY (`SHID`),
  ADD KEY `ScheduleStartTime` (`ScheduleStartTime`),
  ADD KEY `ScheduleID` (`ScheduleID`);

--
-- 資料表索引 `Schedule_Job`
--
ALTER TABLE `Schedule_Job`
  ADD PRIMARY KEY (`SJID`);

--
-- 資料表索引 `Schedule_Job_History`
--
ALTER TABLE `Schedule_Job_History`
  ADD PRIMARY KEY (`SJHID`);

--
-- 資料表索引 `Schedule_Time`
--
ALTER TABLE `Schedule_Time`
  ADD PRIMARY KEY (`STID`);

--
-- 資料表索引 `User`
--
ALTER TABLE `User`
  ADD PRIMARY KEY (`UID`);

--
-- 資料表索引 `User_Login`
--
ALTER TABLE `User_Login`
  ADD PRIMARY KEY (`ULID`),
  ADD KEY `Token` (`Token`);

--
-- 在匯出的資料表使用 AUTO_INCREMENT
--

--
-- 使用資料表 AUTO_INCREMENT `Chart`
--
ALTER TABLE `Chart`
  MODIFY `Number` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;
--
-- 使用資料表 AUTO_INCREMENT `Groups`
--
ALTER TABLE `Groups`
  MODIFY `GID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;
--
-- 使用資料表 AUTO_INCREMENT `Job`
--
ALTER TABLE `Job`
  MODIFY `JobID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=355;
--
-- 使用資料表 AUTO_INCREMENT `Job_History`
--
ALTER TABLE `Job_History`
  MODIFY `JHID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=995872;
--
-- 使用資料表 AUTO_INCREMENT `Job_Log`
--
ALTER TABLE `Job_Log`
  MODIFY `JLID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=961737;
--
-- 使用資料表 AUTO_INCREMENT `Schedule`
--
ALTER TABLE `Schedule`
  MODIFY `ScheduleID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=87;
--
-- 使用資料表 AUTO_INCREMENT `Schedule_History`
--
ALTER TABLE `Schedule_History`
  MODIFY `SHID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=919023;
--
-- 使用資料表 AUTO_INCREMENT `Schedule_Job`
--
ALTER TABLE `Schedule_Job`
  MODIFY `SJID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=710;
--
-- 使用資料表 AUTO_INCREMENT `Schedule_Job_History`
--
ALTER TABLE `Schedule_Job_History`
  MODIFY `SJHID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=951202;
--
-- 使用資料表 AUTO_INCREMENT `Schedule_Time`
--
ALTER TABLE `Schedule_Time`
  MODIFY `STID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=55;
--
-- 使用資料表 AUTO_INCREMENT `User`
--
ALTER TABLE `User`
  MODIFY `UID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=79;
--
-- 使用資料表 AUTO_INCREMENT `User_Login`
--
ALTER TABLE `User_Login`
  MODIFY `ULID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
