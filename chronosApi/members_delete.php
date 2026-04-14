<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

require "config.php";

header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: DELETE, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Accept, Authorization");

if ($_SERVER['REQUEST_METHOD'] !== 'DELETE') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Method not allowed"]);
    exit;
}

// Récupérer l'ID du membre
$memberId = intval($_GET['id'] ?? 0);
if ($memberId <= 0) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "ID membre invalide"]);
    exit;
}

try {
    $pdo = get_db();

    // Supprimer l'utilisateur lié si existant
    $stmtUser = $pdo->prepare("UPDATE users SET is_active = 0 WHERE member_id = ?");
    $stmtUser->execute([$memberId]);

    // Supprimer le membre
    $stmt = $pdo->prepare("DELETE FROM members WHERE id = ?");
    $stmt->execute([$memberId]);

    echo json_encode(["success" => true, "message" => "Membre supprimé"]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Erreur serveur", "error" => $e->getMessage()]);
}
