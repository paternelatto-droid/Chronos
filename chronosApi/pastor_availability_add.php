<?php
// pastor_availability_add.php
ini_set('display_errors',1);
ini_set('display_startup_errors',1);
error_reporting(E_ALL);

require "config.php";
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Accept, Authorization");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(200); exit; }
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(["success"=>false,"message"=>"Method not allowed"]);
    exit;
}

$data = json_decode(file_get_contents("php://input"), true);
if (!$data || !is_array($data)) {
    http_response_code(400);
    echo json_encode(["success"=>false,"message"=>"Invalid JSON"]);
    exit;
}

try {
    $pdo = get_db();

    $user_id = isset($data['user_id']) ? intval($data['user_id']) : 0;
    $day_of_week = isset($data['day_of_week']) ? trim($data['day_of_week']) : null; // 'Lundi'.. or null
    $specific_date = isset($data['specific_date']) && $data['specific_date'] !== '' ? $data['specific_date'] : null; // 'YYYY-MM-DD' or null
    $start_time = isset($data['start_time']) ? $data['start_time'] : null; // 'HH:mm' or 'HH:mm:ss'
    $end_time = isset($data['end_time']) ? $data['end_time'] : null;

    if ($user_id <= 0 || (!$day_of_week && !$specific_date) || !$start_time || !$end_time) {
        http_response_code(400);
        echo json_encode(["success"=>false,"message"=>"Missing required fields (user_id + (day_of_week or specific_date) + start_time + end_time)"]);
        exit;
    }

    // Normalize times to HH:MM:SS
    $start_time = date('H:i:s', strtotime($start_time));
    $end_time = date('H:i:s', strtotime($end_time));

    $stmt = $pdo->prepare("INSERT INTO pastor_availability (user_id, day_of_week, specific_date, start_time, end_time) VALUES (:user_id, :day_of_week, :specific_date, :start_time, :end_time)");
    $stmt->execute([
        ':user_id' => $user_id,
        ':day_of_week' => $day_of_week,
        ':specific_date' => $specific_date,
        ':start_time' => $start_time,
        ':end_time' => $end_time
    ]);

    $id = intval($pdo->lastInsertId());
    echo json_encode(["success"=>true, "id"=>$id, "message"=>"Availability added"]);
} catch (Exception $e) {
    http_response_code(500);
    if ($pdo && $pdo->inTransaction()) { /* no tx used here */ }
    error_log("pastor_availability_add error: ".$e->getMessage());
    echo json_encode(["success"=>false,"message"=>"Server error","error"=>$e->getMessage()]);
}
