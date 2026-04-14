<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

require "config.php";
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: DELETE, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Accept, Authorization");

$pdo = get_db();
$method = $_SERVER['REQUEST_METHOD'];

if ($method !== 'DELETE') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Method not allowed"]);
    exit;
}

// Récupérer l'ID depuis l'URL (/event_delete.php?id=5) ou JSON
$eventId = null;

if (isset($_GET['id'])) {
    $eventId = intval($_GET['id']);
} else {
    $data = json_decode(file_get_contents("php://input"), true);
    if (isset($data['id'])) {
        $eventId = intval($data['id']);
    }
}

if (!$eventId || $eventId <= 0) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "ID de l'événement manquant ou invalide"]);
    exit;
}

try {
    $pdo->beginTransaction();

    // Vérifier si l'événement existe
    $check = $pdo->prepare("SELECT id FROM events WHERE id = :id");
    $check->execute([':id' => $eventId]);
    if (!$check->fetch()) {
        http_response_code(404);
        echo json_encode(["success" => false, "message" => "Événement introuvable"]);
        exit;
    }

    // Supprimer les dépendances
    $pdo->prepare("DELETE FROM event_users WHERE event_id = :event_id")->execute([':event_id' => $eventId]);
    $pdo->prepare("DELETE FROM event_notifications WHERE event_id = :event_id")->execute([':event_id' => $eventId]);

    // Si tu as une table event_reminders, active ceci :
    // $pdo->prepare("DELETE FROM event_reminders WHERE event_id = :event_id")->execute([':event_id' => $eventId]);

    // Supprimer l'événement
    $pdo->prepare("DELETE FROM events WHERE id = :id")->execute([':id' => $eventId]);

    $pdo->commit();

    echo json_encode(["success" => true, "message" => "Événement supprimé avec succès"]);

} catch (Exception $e) {
    if ($pdo->inTransaction()) $pdo->rollBack();
    http_response_code(500);
    error_log("delete_event error: " . $e->getMessage());
    echo json_encode(["success" => false, "message" => "Erreur serveur", "error" => $e->getMessage()]);
}
