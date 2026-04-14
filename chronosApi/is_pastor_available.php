<?php
require "config.php"; // Connexion à la base $pdo

// Forcer le type de contenu JSON
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Accept, Authorization");

$pdo = get_db();
$method = $_SERVER['REQUEST_METHOD'];

switch($method) {
    
    case 'GET':
    // Vérifier la disponibilité d'un pasteur

    
$event_datetime_raw = $_GET['event_datetime'] ?? null;

if (!$event_datetime_raw) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Missing parameter: event_datetime"
    ]);
    exit;
}

// Nettoyer et convertir en timestamp
$event_datetime_raw = str_replace('.', '', $event_datetime_raw);
$timestamp = strtotime($event_datetime_raw);

if (!$timestamp) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Invalid event_datetime format"
    ]);
    exit;
}

$event_date = date("Y-m-d", $timestamp);
$event_time = date("H:i:s", $timestamp);
$daysEnToFr = [
    "Monday" => "Lundi",
    "Tuesday" => "Mardi",
    "Wednesday" => "Mercredi",
    "Thursday" => "Jeudi",
    "Friday" => "Vendredi",
    "Saturday" => "Samedi",
    "Sunday" => "Dimanche"
];
$dayOfWeekFr = $daysEnToFr[date('l', $timestamp)] ?? null;

// Vérifier disponibilité pasteur
$stmt = $pdo->prepare("
    SELECT 1
    FROM users u
    JOIN pastor_availability pa ON pa.user_id = u.id
    WHERE u.role_id = 4
      AND u.is_active = 1
      AND (
          pa.specific_date = :event_date
          OR (pa.specific_date IS NULL AND pa.day_of_week = :day_of_week)
      )
      AND pa.start_time <= :event_time
      AND pa.end_time >= :event_time
    LIMIT 1
");
$stmt->execute([
    ':event_date' => $event_date,
    ':day_of_week' => $dayOfWeekFr,
    ':event_time' => $event_time
]);

$available = $stmt->rowCount() > 0;

echo json_encode([
    "success" => true,
    "available" => $available,
    "message" => $available ? "Pasteur disponible" : "Pasteur non disponible"
]);
    break;

    case 'POST':
        // Ajouter une disponibilité pour un pasteur
        $data = json_decode(file_get_contents("php://input"), true);

        if(empty($data['user_id']) || empty($data['start_time']) || empty($data['end_time'])) {
            http_response_code(400);
            echo json_encode([
                "success" => false,
                "message" => "Missing required fields"
            ]);
            exit;
        }

        $stmt = $pdo->prepare("
            INSERT INTO pastor_availability (user_id, day_of_week, specific_date, start_time, end_time)
            VALUES (:user_id, :day_of_week, :specific_date, :start_time, :end_time)
        ");
        $stmt->execute([
            ':user_id' => intval($data['user_id']),
            ':day_of_week' => $data['day_of_week'] ?? null,
            ':specific_date' => $data['specific_date'] ?? null,
            ':start_time' => $data['start_time'],
            ':end_time' => $data['end_time']
        ]);

        echo json_encode([
            "success" => true,
            "id" => $pdo->lastInsertId()
        ]);
        break;

    default:
        http_response_code(405);
        echo json_encode([
            "success" => false,
            "message" => "Method not allowed"
        ]);
        break;
}
?>
