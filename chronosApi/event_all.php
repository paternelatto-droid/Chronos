<?php
require "config.php";
header("Content-Type: application/json; charset=UTF-8");

try {
    $pdo = get_db();

    if (!isset($_GET['user_id'])) {
        http_response_code(400);
        echo json_encode(["error" => "Paramètre 'user_id' manquant"]);
        exit;
    }

    $userId = $_GET['user_id'] ?? 0;

    // événements publics
    // $publicEvents = $pdo->query("SELECT * FROM events WHERE visibility='public'")->fetchAll(PDO::FETCH_ASSOC);
    
    $publicEvents = $pdo->query("SELECT * FROM events")->fetchAll(PDO::FETCH_ASSOC);

    // événements liés à l'utilisateur
    $userEvents = $pdo->prepare("
        SELECT e.* FROM events e
        INNER JOIN event_users eu ON e.id = eu.event_id
        WHERE eu.user_id = :user_id
    ");
    $userEvents->execute(['user_id' => $userId]);
    $userEventsData = $userEvents->fetchAll(PDO::FETCH_ASSOC);

    // fusion et suppression doublons
    $allEvents = array_values(array_unique(array_merge($publicEvents, $userEventsData), SORT_REGULAR));

    echo json_encode([
        "success" => true,
        "events" => $allEvents
    ]);


} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(["error" => $e->getMessage()]);
}
