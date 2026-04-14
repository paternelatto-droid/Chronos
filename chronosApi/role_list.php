<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

// Connexion DB (à adapter avec ton fichier de config)
require "config.php";

header('Content-Type: application/json');

$response = ["success" => false, "roles" => []];

try {
    
    $pdo = get_db();


    // Récupération des rôles
    $query = $pdo->prepare("SELECT * FROM roles ORDER BY name ASC");
    $query->execute();
    $roles = $query->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
            "success" => true,
            "roles" => $roles
        ], JSON_UNESCAPED_UNICODE);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "error" => $e->getMessage()
    ], JSON_UNESCAPED_UNICODE);
}
