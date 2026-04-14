<?php
// pastor_availability_delete.php
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
    $id = isset($data['id']) ? intval($data['id']) : 0;
    if ($id <= 0) {
        http_response_code(400);
        echo json_encode(["success"=>false,"message"=>"id required"]);
        exit;
    }

    $stmt = $pdo->prepare("DELETE FROM pastor_availability WHERE id = :id");
    $stmt->execute([':id' => $id]);
    echo json_encode(["success"=>true,"message"=>"Deleted", "deleted" => $stmt->rowCount()]);
} catch (Exception $e) {
    http_response_code(500);
    error_log("pastor_availability_delete error: ".$e->getMessage());
    echo json_encode(["success"=>false,"message"=>"Server error","error"=>$e->getMessage()]);
}
