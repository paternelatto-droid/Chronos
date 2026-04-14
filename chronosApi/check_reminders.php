<?php
require "config.php";
header("Content-Type: application/json; charset=UTF-8");

try {
    $pdo = get_db();
    $userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;
    if ($userId <= 0) {
        echo json_encode(["success"=>false,"message"=>"user_id required"]);
        exit;
    }

    $now = time();

    $stmt = $pdo->prepare("
        SELECT en.id AS notif_id, en.event_id, en.user_id, en.title, en.message, en.minutes_before, e.date_debut
        FROM event_notifications en
        INNER JOIN events e ON e.id = en.event_id
        WHERE en.is_sent = 0 AND en.user_id = :user_id
    ");
    $stmt->execute([':user_id' => $userId]);
    $all = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $due = [];

    foreach ($all as $n) {
        $eventTime = strtotime($n['date_debut']);
        $minutesBefore = intval($n['minutes_before']);
        $reminderTime = $eventTime - ($minutesBefore * 60);

        if ($now >= $reminderTime) {
            $due[] = $n; // ✅ Android se charge de marquer comme envoyé après affichage
        }
    }

    echo json_encode([
        "success" => true,
        "reminders" => $due
    ], JSON_UNESCAPED_UNICODE);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(["success"=>false,"error"=>$e->getMessage()], JSON_UNESCAPED_UNICODE);
}
