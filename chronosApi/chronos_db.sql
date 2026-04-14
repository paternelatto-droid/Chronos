-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3306
-- Généré le : mar. 14 avr. 2026 à 12:06
-- Version du serveur : 8.0.31
-- Version de PHP : 8.3.14

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `chronos_db`
--

-- --------------------------------------------------------

--
-- Structure de la table `events`
--

DROP TABLE IF EXISTS `events`;
CREATE TABLE IF NOT EXISTS `events` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` int UNSIGNED NOT NULL,
  `event_type_id` int UNSIGNED DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `date_debut` datetime NOT NULL,
  `date_fin` datetime DEFAULT NULL,
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `color` int DEFAULT '0',
  `visibility` enum('public','restricted','private') COLLATE utf8mb4_unicode_ci DEFAULT 'public',
  `status` enum('pending','confirmed','canceled','done') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_events_user` (`user_id`),
  KEY `fk_events_event_type` (`event_type_id`),
  KEY `idx_date_visibility` (`date_debut`,`visibility`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `events`
--

INSERT INTO `events` (`id`, `user_id`, `event_type_id`, `title`, `description`, `date_debut`, `date_fin`, `location`, `color`, `visibility`, `status`, `created_at`, `updated_at`) VALUES
(16, 1, 1, 'fhfhhd', 'hdxbxb', '2025-10-12 20:47:00', '2025-10-12 20:47:00', 'hdhd', -14398372, 'restricted', 'pending', '2025-10-12 20:48:40', '2025-10-12 20:48:40'),
(28, 1, 1, 'rrhsfhdhfd', '', '2025-10-12 22:45:00', '2025-10-12 23:16:00', 'gg', -14398372, 'restricted', 'pending', '2025-10-12 21:38:18', '2025-10-12 22:22:17'),
(29, 1, 1, 'fjg', 'dhfhbd', '2025-10-13 16:30:00', '2025-10-13 15:00:00', 'fhfhdbd', -14398372, 'restricted', 'pending', '2025-10-13 15:01:52', '2025-10-13 15:01:52'),
(31, 1, 1, 'réunion du Dimanche', 'xbchgdbfd', '2025-10-14 21:19:00', '2025-10-14 19:19:00', 'jgfjgd', -7657, 'restricted', 'pending', '2025-10-14 19:20:08', '2025-10-14 21:38:31'),
(32, 1, 1, 'rencontre Dominicale', '', '2025-10-18 12:53:00', '2025-10-18 10:52:00', 'A domicile', -1567441, 'restricted', 'pending', '2025-10-18 10:54:40', '2025-10-18 10:54:40'),
(33, 1, 1, 'ggjjx', '', '2025-10-18 02:55:00', '2025-10-18 10:55:00', 'fyxxffy', -167737600, 'public', 'pending', '2025-10-18 10:56:24', '2025-10-18 10:56:24'),
(34, 1, 1, 'Bienvenue', 'dghgjjc', '2025-10-18 03:00:00', '2025-10-18 10:56:00', 'gwgxjccjfj', -13363201, 'public', 'pending', '2025-10-18 10:58:26', '2025-10-18 11:27:19'),
(37, 1, 1, 'ygfffh', '', '2025-10-19 05:32:00', '2025-10-19 05:00:00', 'yhh', -14398372, 'public', 'pending', '2025-10-19 05:01:09', '2025-10-19 05:01:09'),
(38, 1, 1, 'tigkggl', '', '2025-10-19 16:55:00', '2025-10-19 15:42:00', '', -14398372, 'private', 'pending', '2025-10-19 15:42:54', '2025-10-19 15:42:54');

-- --------------------------------------------------------

--
-- Structure de la table `event_notifications`
--

DROP TABLE IF EXISTS `event_notifications`;
CREATE TABLE IF NOT EXISTS `event_notifications` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `event_id` int UNSIGNED NOT NULL,
  `user_id` int UNSIGNED NOT NULL COMMENT 'Destinataire de la notification',
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `send_at` datetime DEFAULT NULL,
  `is_read` tinyint(1) DEFAULT '0',
  `is_sent` tinyint(1) DEFAULT '0',
  `minutes_before` int UNSIGNED DEFAULT NULL COMMENT 'Nombre de minutes avant l’événement pour le rappel',
  PRIMARY KEY (`id`),
  KEY `fk_event_notifications_event` (`event_id`),
  KEY `fk_event_notifications_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `event_notifications`
--

INSERT INTO `event_notifications` (`id`, `event_id`, `user_id`, `title`, `message`, `send_at`, `is_read`, `is_sent`, `minutes_before`) VALUES
(1, 28, 1, 'Rappel: rrhsfhdhfd', 'Rappel de l\'événement dans 15 minute(s)', '2025-10-12 22:14:32', 0, 0, 2),
(2, 28, 1, 'Rappel: rrhsfhdhfd', 'Rappel de l\'événement dans 30 minute(s)', '2025-10-12 22:14:32', 0, 0, 5),
(3, 28, 2, 'Rappel: rrhsfhdhfd', 'Rappel de l\'événement dans 15 minute(s)', '2025-10-12 22:14:32', 0, 0, 15),
(4, 28, 2, 'Rappel: rrhsfhdhfd', 'Rappel de l\'événement dans 30 minute(s)', '2025-10-12 22:14:32', 0, 0, 30),
(5, 29, 1, 'Rappel: fjg', 'Rappel de l\'événement dans 10 minute(s)', '2025-10-13 16:42:28', 0, 1, 10),
(6, 29, 1, 'Rappel: fjg', 'Rappel de l\'événement dans 15 minute(s)', '2025-10-13 16:42:28', 0, 1, 15),
(7, 29, 2, 'Rappel: fjg', 'Rappel de l\'événement dans 10 minute(s)', '2025-10-13 16:42:28', 0, 1, 10),
(8, 29, 2, 'Rappel: fjg', 'Rappel de l\'événement dans 15 minute(s)', '2025-10-13 16:42:28', 0, 1, 15),
(13, 33, 1, 'Rappel: ggjjx', 'Rappel de l\'événement dans 30 minute(s)', '2025-10-18 22:18:17', 0, 1, 30),
(14, 33, 1, 'Rappel: ggjjx', 'Rappel de l\'événement dans 60 minute(s)', '2025-10-18 22:18:18', 0, 1, 60),
(15, 33, 2, 'Rappel: ggjjx', 'Rappel de l\'événement dans 30 minute(s)', '2025-10-18 22:18:18', 0, 1, 30),
(16, 33, 2, 'Rappel: ggjjx', 'Rappel de l\'événement dans 60 minute(s)', '2025-10-18 22:18:18', 0, 1, 60),
(46, 37, 1, 'Rappel: ygfffh', 'Rappel de l\'événement dans 15 minute(s)', NULL, 0, 0, 15),
(47, 37, 1, 'Rappel: ygfffh', 'Rappel de l\'événement dans 30 minute(s)', NULL, 0, 0, 30),
(48, 37, 2, 'Rappel: ygfffh', 'Rappel de l\'événement dans 15 minute(s)', NULL, 0, 0, 15),
(49, 37, 2, 'Rappel: ygfffh', 'Rappel de l\'événement dans 30 minute(s)', NULL, 0, 0, 30),
(50, 38, 1, 'Rappel: tigkggl', 'Rappel de l\'événement dans 5 minute(s)', NULL, 0, 0, 5),
(51, 38, 1, 'Rappel: tigkggl', 'Rappel de l\'événement dans 10 minute(s)', NULL, 0, 0, 10),
(52, 38, 1, 'Rappel: tigkggl', 'Rappel de l\'événement dans 15 minute(s)', NULL, 0, 0, 15),
(53, 38, 1, 'Rappel: tigkggl', 'Rappel de l\'événement dans 30 minute(s)', NULL, 0, 0, 30),
(54, 38, 1, 'Rappel: tigkggl', 'Rappel de l\'événement dans 60 minute(s)', NULL, 0, 0, 60);

-- --------------------------------------------------------

--
-- Structure de la table `event_types`
--

DROP TABLE IF EXISTS `event_types`;
CREATE TABLE IF NOT EXISTS `event_types` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `has_duration` tinyint(1) DEFAULT '0',
  `requires_pastor` tinyint(1) DEFAULT '0',
  `icon` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `color_hex` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_unique` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `event_types`
--

INSERT INTO `event_types` (`id`, `name`, `code`, `description`, `has_duration`, `requires_pastor`, `icon`, `color_hex`, `created_at`, `updated_at`) VALUES
(1, 'Culte du dimanche', 'SUNDAY_SERVICE', 'Service principal du dimanche', 1, 0, 'ic_sunday.png', '#4CAF50', '2025-10-10 23:53:40', '2025-10-10 23:53:40'),
(2, 'Réunion de prière', 'PRAYER_MEETING', 'Temps de prière collectif', 1, 0, 'ic_prayer.png', '#2196F3', '2025-10-10 23:53:40', '2025-10-10 23:53:40'),
(3, 'Concert / Louange', 'WORSHIP_EVENT', 'Soirée de louange et adoration', 1, 0, 'ic_music.png', '#FF9800', '2025-10-10 23:53:40', '2025-10-10 23:53:40'),
(4, 'Rendez-vous pastoral', 'PASTOR_RDV', 'Entretien individuel avec un pasteur', 1, 1, 'ic_pastor.png', '#9C27B0', '2025-10-10 23:53:40', '2025-10-10 23:53:40'),
(5, 'Accompagnement spirituel', 'SPIRIT_GUIDE', 'Suivi spirituel individuel', 1, 1, 'ic_spirit.png', '#673AB7', '2025-10-10 23:53:40', '2025-10-10 23:53:40'),
(6, 'Conseil matrimonial', 'MARRIAGE_ADVICE', 'Conseil conjugal avec un responsable', 1, 1, 'ic_couple.png', '#E91E63', '2025-10-10 23:53:40', '2025-10-10 23:53:40'),
(7, 'Confession / Écoute', 'CONFESSION', 'Moment de confession et d’écoute', 1, 1, 'ic_confession.png', '#795548', '2025-10-10 23:53:40', '2025-10-10 23:53:40');

-- --------------------------------------------------------

--
-- Structure de la table `event_users`
--

DROP TABLE IF EXISTS `event_users`;
CREATE TABLE IF NOT EXISTS `event_users` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `event_id` int UNSIGNED NOT NULL,
  `user_id` int UNSIGNED NOT NULL COMMENT 'Utilisateur/membre/pasteur lié à l’événement',
  `role_in_event` enum('participant','pasteur','organisateur') COLLATE utf8mb4_unicode_ci DEFAULT 'participant',
  `status` enum('pending','confirmed','cancelled') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_event_users_event` (`event_id`),
  KEY `fk_event_users_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `event_users`
--

INSERT INTO `event_users` (`id`, `event_id`, `user_id`, `role_in_event`, `status`, `created_at`) VALUES
(15, 28, 1, 'participant', 'pending', '2025-10-12 21:38:18'),
(16, 28, 2, 'participant', 'pending', '2025-10-12 21:38:18'),
(17, 29, 1, 'participant', 'pending', '2025-10-13 15:01:52'),
(18, 29, 2, 'participant', 'pending', '2025-10-13 15:01:52'),
(27, 38, 1, 'participant', 'pending', '2025-10-19 15:42:54');

-- --------------------------------------------------------

--
-- Structure de la table `members`
--

DROP TABLE IF EXISTS `members`;
CREATE TABLE IF NOT EXISTS `members` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `first_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` enum('M','F') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `baptism_date` date DEFAULT NULL,
  `status` enum('active','inactive','visitor','member','leader') COLLATE utf8mb4_unicode_ci DEFAULT 'member',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `members`
--

INSERT INTO `members` (`id`, `first_name`, `last_name`, `gender`, `phone`, `email`, `address`, `date_of_birth`, `baptism_date`, `status`, `created_at`, `updated_at`) VALUES
(1, 'Pascale', 'Brou', 'M', '+225070658523', 'jean.dupont@example.com', 'Abidjan, Côte d’Ivoire', '1980-05-15', '2025-10-16', '', '2025-10-11 16:13:27', '2025-10-17 13:39:39'),
(2, 'Jean', 'Bourg', 'M', '+2250700000000', 'jean.dupont@example.com', 'Abidjan, Côte d’Ivoire', '1980-05-15', '2025-10-17', '', '2025-10-15 12:19:08', '2025-10-18 14:12:11'),
(3, 'amena', 'Aboua', 'F', '087048506', 'dgfh@gjg.h', 'fhhf', '2025-10-01', NULL, '', '2025-10-16 13:37:00', '2025-10-16 22:11:39');

-- --------------------------------------------------------

--
-- Structure de la table `pastor_availability`
--

DROP TABLE IF EXISTS `pastor_availability`;
CREATE TABLE IF NOT EXISTS `pastor_availability` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` int UNSIGNED NOT NULL COMMENT 'Référence vers le pasteur',
  `day_of_week` enum('Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi','Dimanche') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `specific_date` date DEFAULT NULL COMMENT 'Optionnel : jour spécifique de disponibilité',
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_pastor_availability_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `pastor_availability`
--

INSERT INTO `pastor_availability` (`id`, `user_id`, `day_of_week`, `specific_date`, `start_time`, `end_time`, `created_at`, `updated_at`) VALUES
(1, 2, 'Lundi', NULL, '09:00:00', '12:00:00', '2025-10-11 16:02:04', '2025-10-12 05:27:50'),
(3, 2, NULL, '2025-10-11', '10:00:00', '15:00:00', '2025-10-11 16:02:04', '2025-10-11 16:13:56'),
(4, 2, NULL, '2025-10-20', '09:00:00', '12:00:00', '2025-10-11 16:02:04', '2025-10-11 16:14:01'),
(5, 2, 'Jeudi', NULL, '00:23:00', '13:23:00', '2025-10-18 00:23:19', '2025-10-18 00:23:19');

-- --------------------------------------------------------

--
-- Structure de la table `permissions`
--

DROP TABLE IF EXISTS `permissions`;
CREATE TABLE IF NOT EXISTS `permissions` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'code unique ex: event.create, event.delete',
  `label` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Texte lisible ex: Créer un événement',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_unique` (`key`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `permissions`
--

INSERT INTO `permissions` (`id`, `key`, `label`, `created_at`) VALUES
(1, 'user.view', 'Voir la liste des utilisateurs', '2025-10-13 09:35:46'),
(2, 'user.create', 'Créer un utilisateur', '2025-10-13 09:35:46'),
(3, 'user.delete', 'Supprimer un utilisateur', '2025-10-13 09:35:46'),
(4, 'event.view', 'Voir les événements', '2025-10-13 09:35:46'),
(5, 'event.create', 'Créer un événement', '2025-10-13 09:35:46'),
(6, 'event.update', 'Modifier un événement', '2025-10-13 09:35:46'),
(7, 'event.delete', 'Supprimer un événement', '2025-10-13 09:35:46'),
(8, 'event.join', 'Participer à un événement', '2025-10-13 09:35:46'),
(9, 'event.validate', 'Valider les présences', '2025-10-13 09:35:46'),
(10, 'member.view', 'Voir la liste des membres', '2025-10-14 22:11:11'),
(11, 'member.create', 'Création de membre', '2025-10-14 22:11:11'),
(12, 'member.update', 'Modification de membre', '2025-10-14 22:13:28'),
(13, 'member.delete', 'Suppression de membre', '2025-10-14 22:13:28'),
(14, 'availability.view', 'Liste des disponibilités', '2025-10-17 20:47:36'),
(15, 'availability.create', 'Création des disponibilités ', '2025-10-17 20:47:36'),
(16, 'availability.update', 'Mise à jour des disponibilités', '2025-10-17 20:48:16'),
(17, 'availability.delete', 'Suppression des disponibilités ', '2025-10-17 20:48:16');

-- --------------------------------------------------------

--
-- Structure de la table `roles`
--

DROP TABLE IF EXISTS `roles`;
CREATE TABLE IF NOT EXISTS `roles` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_unique` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `roles`
--

INSERT INTO `roles` (`id`, `name`, `description`, `created_at`, `updated_at`) VALUES
(1, 'admin', NULL, '2025-10-10 23:35:47', '2025-10-10 23:35:47'),
(2, 'manager', NULL, '2025-10-10 23:36:21', '2025-10-10 23:36:21'),
(3, 'user', NULL, '2025-10-10 23:36:21', '2025-10-10 23:36:21'),
(4, 'pasteur', NULL, '2025-10-11 16:12:14', '2025-10-11 16:12:14');

-- --------------------------------------------------------

--
-- Structure de la table `role_permissions`
--

DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE IF NOT EXISTS `role_permissions` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `role_id` int UNSIGNED NOT NULL,
  `permission_id` int UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `role_permission_unique` (`role_id`,`permission_id`),
  KEY `permission_id` (`permission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `role_permissions`
--

INSERT INTO `role_permissions` (`id`, `role_id`, `permission_id`) VALUES
(9, 1, 1),
(7, 1, 2),
(8, 1, 3),
(6, 1, 4),
(1, 1, 5),
(4, 1, 6),
(2, 1, 7),
(3, 1, 8),
(5, 1, 9),
(25, 1, 10),
(26, 1, 11),
(31, 1, 12),
(32, 1, 13),
(33, 1, 14),
(34, 1, 15),
(35, 1, 16),
(36, 1, 17),
(19, 2, 4),
(16, 2, 5),
(18, 2, 6),
(17, 2, 8),
(24, 3, 4),
(23, 3, 8);

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `email_verified_at` timestamp NULL DEFAULT NULL,
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `role_id` int UNSIGNED DEFAULT NULL,
  `member_id` int UNSIGNED DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_username` (`username`),
  UNIQUE KEY `uq_email` (`email`),
  KEY `fk_users_role` (`role_id`),
  KEY `fk_users_member` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `users`
--

INSERT INTO `users` (`id`, `name`, `email`, `email_verified_at`, `username`, `password`, `role_id`, `member_id`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 'administrateur', '', NULL, 'admin', '$2y$10$pzvT3Xy5yBPM15G5TX1DI..sK66NGha1cNJgwy/XLwyxUGbfFvQ6K', 1, NULL, 1, '2025-10-11 04:25:12', '2025-10-11 04:25:12'),
(2, 'Pascale Brou', 'jean.dupont@example.com', NULL, 'jeandupont', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 4, 1, 0, '2025-10-11 16:13:27', '2025-10-17 13:23:30');

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `events`
--
ALTER TABLE `events`
  ADD CONSTRAINT `fk_events_event_type` FOREIGN KEY (`event_type_id`) REFERENCES `event_types` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_events_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `event_notifications`
--
ALTER TABLE `event_notifications`
  ADD CONSTRAINT `fk_event_notifications_event` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_event_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `event_users`
--
ALTER TABLE `event_users`
  ADD CONSTRAINT `fk_event_users_event` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_event_users_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `pastor_availability`
--
ALTER TABLE `pastor_availability`
  ADD CONSTRAINT `fk_pastor_availability_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `role_permissions`
--
ALTER TABLE `role_permissions`
  ADD CONSTRAINT `role_permissions_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `role_permissions_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `fk_users_member` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE SET NULL;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
