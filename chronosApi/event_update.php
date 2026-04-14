<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

require "config.php";
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: PUT, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Accept, Authorization");

$pdo = get_db();
$method = $_SERVER['REQUEST_METHOD'];

if ($method !== 'PUT') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Method not allowed"]);
    exit;
}

$data = json_decode(file_get_contents("php://input"), true);
if (!$data || !is_array($data) || !isset($data['id'])) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Invalid JSON or missing event id"]);
    exit;
}

$eventId = intval($data['id']);
if ($eventId <= 0) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Invalid event ID"]);
    exit;
}

try {
    $pdo->beginTransaction();

    // --- Champs principaux à mettre à jour ---
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

    // --- Vérifier si l'événement existe ---
    $check = $pdo->prepare("SELECT id FROM events WHERE id = :id");
    $check->execute([':id' => $eventId]);
    if (!$check->fetch()) {
        http_response_code(404);
        echo json_encode(["success" => false, "message" => "Event not found"]);
        exit;
    }

    // --- Update event ---
    $stmtUpdate = $pdo->prepare("
        UPDATE events SET
            user_id = :user_id,
            event_type_id = :event_type_id,
            title = :title,
            description = :description,
            date_debut = :date_debut,
            date_fin = :date_fin,
            location = :location,
            color = :color,
            visibility = :visibility,
            status = :status
        WHERE id = :event_id
    ");
    $stmtUpdate->execute([
        ':user_id' => intval($user_id),
        ':event_type_id' => $event_type_id,
        ':title' => $title,
        ':description' => $description,
        ':date_debut' => $date_debut,
        ':date_fin' => $date_fin,
        ':location' => $location,
        ':color' => $color,
        ':visibility' => $visibility,
        ':status' => $status,
        ':event_id' => $eventId
    ]);

    // --- Reset event_users ---
    $pdo->prepare("DELETE FROM event_users WHERE event_id = :event_id")->execute([':event_id' => $eventId]);
    $userIds = $data['userIds'] ?? [];
    if ($visibility === 'private') $userIds = [$user_id];

    if (!empty($userIds)) {
        $stmtUser = $pdo->prepare("
            INSERT INTO event_users (event_id, user_id, role_in_event, status)
            VALUES (:event_id, :user_id, :role, :status)
        ");
        foreach ($userIds as $uid) {
            $uid = intval($uid);
            if ($uid <= 0) continue;

            $checkUser = $pdo->prepare("SELECT id FROM users WHERE id=:id");
            $checkUser->execute([':id'=>$uid]);
            if (!$checkUser->fetch()) continue;

            $stmtUser->execute([
                ':event_id' => $eventId,
                ':user_id' => $uid,
                ':role' => $data['role_in_event'] ?? 'participant',
                ':status' => 'pending'
            ]);
        }
    }

    // --- Reset event_notifications ---
    $pdo->prepare("DELETE FROM event_notifications WHERE event_id = :event_id")->execute([':event_id' => $eventId]);

    $reminders = $data['reminders'] ?? [];
    if (!empty($reminders)) {
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

    echo json_encode(["success" => true, "id" => $eventId, "message" => "Événement mis à jour avec succès"]);

} catch (Exception $e) {
    if ($pdo->inTransaction()) $pdo->rollBack();
    http_response_code(500);
    error_log("update_event error: " . $e->getMessage());
    echo json_encode(["success" => false, "message" => "Server error", "error" => $e->getMessage()]);
}
