<?php
//require 'vendor/autoload.php'; // pour firebase/php-jwt
require 'config.php';

use Firebase\JWT\JWT;
use Firebase\JWT\Key;

/**
 * Vérifie le token JWT passé dans l'en-tête Authorization
 * @return array|false Retourne le payload du token si valide, false sinon
 */
function authenticate() {
    $headers = apache_request_headers();
    
    if (!isset($headers['Authorization'])) {
        http_response_code(401);
        echo json_encode(["success" => false, "message" => "Token manquant"]);
        exit;
    }

    $authHeader = $headers['Authorization'];
    if (strpos($authHeader, 'Bearer ') !== 0) {
        http_response_code(400);
        echo json_encode(["success" => false, "message" => "Format d'autorisation invalide"]);
        exit;
    }

    $token = trim(str_replace('Bearer ', '', $authHeader));

    try {
        $secret = "mon_secret_long_et_complexe"; // même que login.php
        $decoded = JWT::decode($token, new Key($secret, 'HS256'));
        return (array) $decoded;

    } catch (\Firebase\JWT\ExpiredException $e) {
        http_response_code(401);
        echo json_encode(["success" => false, "message" => "Token expiré"]);
        exit;
    } catch (\Exception $e) {
        http_response_code(401);
        echo json_encode(["success" => false, "message" => "Token invalide"]);
        exit;
    }
}

/**
 * Vérifie qu'un utilisateur a une permission spécifique
 * @param string $permission clé de permission à vérifier
 * @param array $tokenPayload payload décodé du JWT
 * @return bool
 */
function hasPermission($permission, $tokenPayload) {
    if (!isset($tokenPayload['permissions']) || !is_array($tokenPayload['permissions'])) {
        return false;
    }
    return in_array($permission, $tokenPayload['permissions']);
}
