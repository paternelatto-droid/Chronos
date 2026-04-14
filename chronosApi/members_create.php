<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

require "config.php";

header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Accept, Authorization");

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
    $pdo = get_db();

    // Récupération des champs
    $first_name = trim($data['first_name'] ?? '');
    $last_name  = trim($data['last_name'] ?? '');
    $gender     = $data['gender'] ?? null;
    $phone      = $data['phone'] ?? null;
    $email      = $data['email'] ?? null;
    $address    = $data['address'] ?? null;
    $dob        = $data['date_of_birth'] ?? null;
    $baptism    = $data['baptism_date'] ?? null;
    $status     = $data['status'] ?? 'member';

    if (empty($first_name) || empty($last_name)) {
        http_response_code(400);
        echo json_encode(["success" => false, "message" => "Missing required fields (first_name/last_name)"]);
        exit;
    }

    $stmt = $pdo->prepare("
        INSERT INTO members 
        (first_name, last_name, gender, phone, email, address, date_of_birth, baptism_date, status) 
        VALUES 
        (:first_name, :last_name, :gender, :phone, :email, :address, :dob, :baptism, :status)
    ");

    $stmt->execute([
        ':first_name' => $first_name,
        ':last_name'  => $last_name,
        ':gender'     => $gender,
        ':phone'      => $phone,
        ':email'      => $email,
        ':address'    => $address,
        ':dob'        => $dob,
        ':baptism'    => $baptism,
        ':status'     => $status
    ]);

    $memberId = intval($pdo->lastInsertId());

    echo json_encode(["success" => true, "id" => $memberId, "message" => "Membre créé avec succès"]);

} catch (Exception $e) {
    http_response_code(500);
    error_log("members_create error: " . $e->getMessage());
    echo json_encode(["success" => false, "message" => "Server error", "error" => $e->getMessage()]);
}
