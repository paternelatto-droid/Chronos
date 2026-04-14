<?php
require "config.php";
header("Content-Type: application/json; charset=UTF-8");

try {
    if (!isset($_GET['id'])) {
        echo json_encode(["success" => false, "message" => "id is required"]);
        exit;
    }

    $notifId = intval($_GET['id']);
    if ($notifId <= 0) {
        echo json_encode(["success" => false, "message" => "Invalid id"]);
        exit;
    }

    $pdo = get_db();
    $stmt = $pdo->prepare("UPDATE event_notifications SET is_sent = 1, send_at = NOW() WHERE id = :id AND is_sent = 0");
    $stmt->execute([':id' => $notifId]);

    echo json_encode([
        "success" => true,
        "message" => "Notification marked as sent",
        "id" => $notifId
    ], JSON_UNESCAPED_UNICODE);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "error" => $e->getMessage()], JSON_UNESCAPED_UNICODE);
}
