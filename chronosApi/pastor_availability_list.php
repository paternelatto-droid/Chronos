<?php 
// pastor_availability_list.php
ini_set('display_errors',1);
ini_set('display_startup_errors',1);
error_reporting(E_ALL);

require "config.php";
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

try {
    $pdo = get_db();

    $user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;
    if ($user_id <= 0) {
        http_response_code(400);
        echo json_encode(["success" => false, "message" => "user_id required"]);
        exit;
    }

    // ✅ FILTRAGE : garder les lignes sans date OU avec date >= aujourd'hui
    $stmt = $pdo->prepare("
        SELECT 
            id, 
            user_id, 
            day_of_week, 
            specific_date, 
            TIME_FORMAT(start_time, '%H:%i:%s') as start_time, 
            TIME_FORMAT(end_time, '%H:%i:%s') as end_time, 
            created_at, 
            updated_at
        FROM pastor_availability
        WHERE user_id = :user_id
        AND (
            specific_date IS NULL
            OR specific_date >= CURDATE()
        )
        ORDER BY 
            specific_date IS NOT NULL DESC, 
            FIELD(day_of_week,'Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi','Dimanche'), 
            specific_date, 
            start_time
    ");
    $stmt->execute([':user_id' => $user_id]);
    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode(["success" => true, "availability" => $rows], JSON_UNESCAPED_UNICODE);
} catch (Exception $e) {
    http_response_code(500);
    error_log("pastor_availability_list error: " . $e->getMessage());
    echo json_encode(["success" => false, "message" => "Server error", "error" => $e->getMessage()]);
}
