<?php
require "config.php";
$pdo = get_db();
$method = $_SERVER['REQUEST_METHOD'];

header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, DELETE, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Accept, Authorization");
if ($method === 'OPTIONS') { http_response_code(200); exit; }

try {
    if ($method === 'GET') {
        // GET /event_users.php?event_id=5  OR ?user_id=2
        if (isset($_GET['event_id'])) {
            $event_id = intval($_GET['event_id']);
            $stmt = $pdo->prepare("SELECT eu.*, u.name AS user_name, u.email AS user_email FROM event_users eu LEFT JOIN users u ON eu.user_id = u.id WHERE eu.event_id = :event_id");
            $stmt->execute([':event_id' => $event_id]);
            echo json_encode($stmt->fetchAll(PDO::FETCH_ASSOC));
            exit;
        } elseif (isset($_GET['user_id'])) {
            $user_id = intval($_GET['user_id']);
            $stmt = $pdo->prepare("SELECT eu.*, e.title AS event_title, e.date_debut FROM event_users eu LEFT JOIN events e ON eu.event_id = e.id WHERE eu.user_id = :user_id");
            $stmt->execute([':user_id' => $user_id]);
            echo json_encode($stmt->fetchAll(PDO::FETCH_ASSOC));
            exit;
        } else {
            // all
            $stmt = $pdo->query("SELECT * FROM event_users");
            echo json_encode($stmt->fetchAll(PDO::FETCH_ASSOC));
            exit;
        }
    }

    if ($method === 'POST') {
        // Ajouter user à event, JSON: { "event_id": 5, "user_id": 2, "role_in_event": "participant" }
        $data = json_decode(file_get_contents("php://input"), true);
        if (!$data || !isset($data['event_id']) || !isset($data['user_id'])) {
            http_response_code(400);
            echo json_encode(["error" => "Missing event_id or user_id"]);
            exit;
        }

        $pdo->beginTransaction();

        $stmt = $pdo->prepare("INSERT INTO event_users (event_id, user_id, role_in_event, status) VALUES (:event_id, :user_id, :role, :status)");
        $stmt->execute([
            ':event_id' => intval($data['event_id']),
            ':user_id' => intval($data['user_id']),
            ':role' => $data['role_in_event'] ?? 'participant',
            ':status' => $data['status'] ?? 'pending'
        ]);

        $insertedId = intval($pdo->lastInsertId());

        // Créer notification serveur
        // Récupérer le titre de l'event
        $stmtE = $pdo->prepare("SELECT title FROM events WHERE id = :id LIMIT 1");
        $stmtE->execute([':id' => intval($data['event_id'])]);
        $ev = $stmtE->fetch(PDO::FETCH_ASSOC);
        $title = $ev ? $ev['title'] : 'Événement';

        $notifTitle = "Nouvel événement : " . $title;
        $notifMsg = "Vous avez été ajouté à l'événement \"$title\".";

        $stmtNotif = $pdo->prepare("INSERT INTO event_notifications (event_id, user_id, title, message, is_read, send_at) VALUES (:event_id, :user_id, :title, :message, 0, NOW())");
        $stmtNotif->execute([
            ':event_id' => intval($data['event_id']),
            ':user_id' => intval($data['user_id']),
            ':title' => $notifTitle,
            ':message' => $notifMsg
        ]);

        $pdo->commit();
        echo json_encode(["success" => true, "id" => $insertedId]);
        exit;
    }

    if ($method === 'DELETE') {
        // DELETE /event_users.php?event_id=5&user_id=2 or ?id=123
        if (isset($_GET['id'])) {
            $id = intval($_GET['id']);
            $stmt = $pdo->prepare("DELETE FROM event_users WHERE id = :id");
            $stmt->execute([':id' => $id]);
            echo json_encode(["success" => true]); exit;
        }
        if (isset($_GET['event_id']) && isset($_GET['user_id'])) {
            $event_id = intval($_GET['event_id']);
            $user_id = intval($_GET['user_id']);
            $stmt = $pdo->prepare("DELETE FROM event_users WHERE event_id = :event_id AND user_id = :user_id");
            $stmt->execute([':event_id' => $event_id, ':user_id' => $user_id]);
            echo json_encode(["success" => true]); exit;
        }
        http_response_code(400);
        echo json_encode(["error" => "Missing parameters"]);
        exit;
    }

    http_response_code(405);
    echo json_encode(["error" => "Method not allowed"]);
    exit;

} catch (Exception $e) {
    if ($pdo->inTransaction()) $pdo->rollBack();
    http_response_code(500);
    echo json_encode(["error" => "Server error: " . $e->getMessage()]);
    exit;
}
