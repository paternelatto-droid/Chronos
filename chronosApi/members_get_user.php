<?php
// members_get_with_user.php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

require "config.php";

header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

$memberId = isset($_GET['member_id']) ? intval($_GET['member_id']) : 0;
if ($memberId <= 0) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "member_id manquant"
    ]);
    exit;
}

try {
    $pdo = get_db();

    // Récupérer le membre
    $stmtMember = $pdo->prepare("SELECT * FROM members WHERE id = :id LIMIT 1");
    $stmtMember->execute([':id' => $memberId]);
    $member = $stmtMember->fetch(PDO::FETCH_ASSOC);

    if (!$member) {
        http_response_code(404);
        echo json_encode([
            "success" => false,
            "message" => "Membre non trouvé"
        ]);
        exit;
    }

    // Récupérer l'utilisateur lié (si existe)
    $stmtUser = $pdo->prepare("SELECT * FROM users WHERE member_id = :member_id LIMIT 1");
    $stmtUser->execute([':member_id' => $memberId]);
    $user = $stmtUser->fetch(PDO::FETCH_ASSOC);
    if (!$user) $user = null;

    echo json_encode([
        "success" => true,
        "member" => $member,
        "user" => $user
    ], JSON_UNESCAPED_UNICODE);

} catch (Exception $e) {
    http_response_code(500);
    error_log("members_get_with_user error: " . $e->getMessage());
    echo json_encode([
        "success" => false,
        "message" => "Erreur serveur",
        "error" => $e->getMessage()
    ]);
}
