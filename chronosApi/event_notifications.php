<?php
require "config.php";
$pdo = get_db();
$method = $_SERVER['REQUEST_METHOD'];

header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Accept, Authorization");
if ($method === 'OPTIONS') { http_response_code(200); exit; }

try {
    if ($method === 'GET') {
        // GET /event_notifications.php?user_id=2 or &event_id=5
        $user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : null;
        $event_id = isset($_GET['event_id']) ? intval($_GET['event_id']) : null;

        if ($user_id === null) {
            http_response_code(400);
            echo json_encode([]);
            exit;
        }

        $sql = "SELECT en.* FROM event_notifications en WHERE en.user_id = :user_id";
        if ($event_id !== null && $event_id > 0) {
            $sql .= " AND en.event_id = :event_id";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([':user_id' => $user_id, ':event_id' => $event_id]);
        } else {
            $stmt = $pdo->prepare($sql);
            $stmt->execute([':user_id' => $user_id]);
        }

        $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);
        echo json_encode($rows);
        exit;
    }

    if ($method === 'POST') {
        // POST pour créer une notification manuelle (body: event_id,user_id,title,message)
        $data = json_decode(file_get_contents("php://input"), true);
        if (!$data || !isset($data['user_id'])) { http_response_code(400); echo json_encode(["error"=>"Missing user_id"]); exit; }

        $stmt = $pdo->prepare("INSERT INTO event_notifications (event_id, user_id, title, message, is_read, send_at, is_sent) VALUES (:event_id, :user_id, :title, :message, 0, NOW(),0)");
        $stmt->execute([
            ':event_id' => $data['event_id'] ?? null,
            ':user_id' => intval($data['user_id']),
            ':title' => $data['title'] ?? 'Notification',
            ':message' => $data['message'] ?? ''
        ]);

        echo json_encode(["success" => true, "id" => $pdo->lastInsertId()]);
        exit;
    }

    if ($method === 'PUT') {
        // Mettre à jour (ex: marquer lu) -> body: id, is_read
        $data = json_decode(file_get_contents("php://input"), true);
        if (!$data || !isset($data['id'])) { http_response_code(400); echo json_encode(["error"=>"Missing id"]); exit; }

        $stmt = $pdo->prepare("UPDATE event_notifications SET is_read = :is_read WHERE id = :id");
        $stmt->execute([
            ':is_read' => isset($data['is_read']) && $data['is_read'] ? 1 : 0,
            ':id' => intval($data['id'])
        ]);
        echo json_encode(["success" => true]);
        exit;
    }

    if ($method === 'DELETE') {
        // DELETE /event_notifications.php?id=123
        $id = isset($_GET['id']) ? intval($_GET['id']) : 0;
        if ($id <= 0) { http_response_code(400); echo json_encode(["error"=>"Invalid id"]); exit; }
        $stmt = $pdo->prepare("DELETE FROM event_notifications WHERE id = :id");
        $stmt->execute([':id' => $id]);
        echo json_encode(["success" => true]);
        exit;
    }

    http_response_code(405);
    echo json_encode(["error" => "Method not allowed"]);
    exit;

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(["error" => "Server error: " . $e->getMessage()]);
    exit;
}
