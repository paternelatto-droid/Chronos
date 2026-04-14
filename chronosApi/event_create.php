<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

require "config.php";
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Accept, Authorization");

$pdo = get_db();
$method = $_SERVER['REQUEST_METHOD'];

if ($method !== 'POST') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Method not allowed"]);
    exit;
}

$data = json_decode(file_get_contents("php://input"), true);
if (!$data || !is_array($data)) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Invalid JSON"]);
    exit;
}

try {
    $pdo->beginTransaction();

    // --- Champs principaux ---
    $date_debut = $data['date'] ?? $data['date_debut'] ?? null;
    $date_fin   = $data['date_fin'] ?? null;
    $title      = trim($data['title'] ?? '');
    $description = $data['note'] ?? $data['description'] ?? '';
    $location   = $data['location'] ?? null;
    $color      = isset($data['color']) ? intval($data['color']) : 0;
    $visibility = $data['visibility'] ?? 'public';
    $user_id    = $data['user_id'] ?? $data['ownerId'] ?? 0;
    $event_type_id = isset($data['event_type_id']) ? intval($data['event_type_id']) : null;
    $status     = $data['status'] ?? 'pending';

    if (empty($title) || empty($date_debut) || !$user_id) {
        http_response_code(400);
        echo json_encode(["success" => false, "message" => "Missing required fields (title/date_debut/user_id)"]);
        exit;
    }

    // --- Insert event ---
    $stmt = $pdo->prepare("
        INSERT INTO events
        (user_id, event_type_id, title, description, date_debut, date_fin, location, color, visibility, status)
        VALUES (:user_id, :event_type_id, :title, :description, :date_debut, :date_fin, :location, :color, :visibility, :status)
    ");
    $stmt->execute([
        ':user_id' => intval($user_id),
        ':event_type_id' => $event_type_id,
        ':title' => $title,
        ':description' => $description,
        ':date_debut' => $date_debut,
        ':date_fin' => $date_fin,
        ':location' => $location,
        ':color' => $color,
        ':visibility' => $visibility,
        ':status' => $status
    ]);

    $eventId = intval($pdo->lastInsertId());

    // --- Gérer event_users ---
    $userIds = $data['userIds'] ?? [];
    if ($visibility === 'private') $userIds = [$user_id]; // uniquement le créateur
    if (!empty($userIds)) {
        $stmtUser = $pdo->prepare("
            INSERT INTO event_users (event_id, user_id, role_in_event, status)
            VALUES (:event_id, :user_id, :role, :status)
        ");
        foreach ($userIds as $uid) {
            $uid = intval($uid);
            if ($uid <= 0) continue;

            // Vérifier que l'utilisateur existe pour éviter les foreign key errors
            $check = $pdo->prepare("SELECT id FROM users WHERE id=:id");
            $check->execute([':id'=>$uid]);
            if (!$check->fetch()) continue;

            $stmtUser->execute([
                ':event_id' => $eventId,
                ':user_id' => $uid,
                ':role' => $data['role_in_event'] ?? 'participant',
                ':status' => 'pending'
            ]);
        }
    }

    // --- Gérer les reminders dans event_notifications ---
    $reminders = $data['reminders'] ?? []; // tableau de minutes_before
    if (!empty($reminders)) {
        // Déterminer les users cibles
        if ($visibility === 'public') {
            $stmtAll = $pdo->query("SELECT id FROM users");
            $targetUsers = $stmtAll->fetchAll(PDO::FETCH_ASSOC);
        } else {
            $stmtUsers = $pdo->prepare("
                SELECT u.id 
                FROM users u 
                INNER JOIN event_users eu ON eu.user_id = u.id 
                WHERE eu.event_id = :event_id
            ");
            $stmtUsers->execute([':event_id'=>$eventId]);
            $targetUsers = $stmtUsers->fetchAll(PDO::FETCH_ASSOC);
        }

        $stmtNotif = $pdo->prepare("
            INSERT INTO event_notifications
            (event_id, user_id, title, message, minutes_before, is_read, send_at, is_sent)
            VALUES (:event_id, :user_id, :title, :message, :minutes_before, 0, NULL, 0)
        ");

        foreach ($targetUsers as $user) {
            foreach ($reminders as $minutes) {
                $stmtNotif->execute([
                    ':event_id' => $eventId,
                    ':user_id' => $user['id'],
                    ':title' => "Rappel: $title",
                    ':message' => "Rappel de l'événement dans $minutes minute(s)",
                    ':minutes_before' => intval($minutes)
                ]);
            }
        }
    }

    $pdo->commit();

    echo json_encode(["success"=>true, "id"=>$eventId, "message"=>"Événement créé avec succès"]);

} catch (Exception $e) {
    if ($pdo->inTransaction()) $pdo->rollBack();
    http_response_code(500);
    error_log("create_event error: " . $e->getMessage());
    echo json_encode(["success"=>false, "message"=>"Server error", "error"=>$e->getMessage()]);
}
