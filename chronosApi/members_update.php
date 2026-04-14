<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

require "config.php";

header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: PUT, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Accept, Authorization");

$method = $_SERVER['REQUEST_METHOD'];
if ($method !== 'PUT') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Method not allowed"]);
    exit;
}

$data = json_decode(file_get_contents("php://input"), true);
if (!$data || !is_array($data) || empty($data['id'])) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Invalid JSON or missing member ID"]);
    exit;
}

try {
    $pdo = get_db();

    $id = intval($data['id']);
    $first_name = trim($data['first_name'] ?? null);
    $last_name  = trim($data['last_name'] ?? null);
    $gender     = $data['gender'] ?? null;
    $phone      = $data['phone'] ?? null;
    $email      = $data['email'] ?? null;
    $address    = $data['address'] ?? null;
    $dob        = $data['date_of_birth'] ?? null;
    $baptism    = $data['baptism_date'] ?? null;
    $status     = $data['status'] ?? null;

    // Vérifier si le membre existe
    $check = $pdo->prepare("SELECT * FROM members WHERE id = :id");
    $check->execute([':id' => $id]);
    $member = $check->fetch(PDO::FETCH_ASSOC);
    if (!$member) {
        http_response_code(404);
        echo json_encode(["success" => false, "message" => "Membre introuvable"]);
        exit;
    }

    // Construire la requête UPDATE dynamiquement
    $fields = [];
    $params = [':id' => $id];

    if ($first_name !== null) { $fields[] = "first_name=:first_name"; $params[':first_name']=$first_name; }
    if ($last_name  !== null) { $fields[] = "last_name=:last_name";   $params[':last_name']=$last_name; }
    if ($gender     !== null) { $fields[] = "gender=:gender";         $params[':gender']=$gender; }
    if ($phone      !== null) { $fields[] = "phone=:phone";           $params[':phone']=$phone; }
    if ($email      !== null) { $fields[] = "email=:email";           $params[':email']=$email; }
    if ($address    !== null) { $fields[] = "address=:address";       $params[':address']=$address; }
    if ($dob        !== null) { $fields[] = "date_of_birth=:dob";     $params[':dob']=$dob; }
    if ($baptism    !== null) { $fields[] = "baptism_date=:baptism";  $params[':baptism']=$baptism; }
    if ($status     !== null) { $fields[] = "status=:status";         $params[':status']=$status; }

    if (!empty($fields)) {
        $sql = "UPDATE members SET ".implode(", ", $fields)." WHERE id=:id";
        $stmt = $pdo->prepare($sql);
        $stmt->execute($params);
    }

    echo json_encode(["success" => true, "message" => "Membre mis à jour avec succès"]);

} catch (Exception $e) {
    http_response_code(500);
    error_log("members_update error: ".$e->getMessage());
    echo json_encode(["success" => false, "message" => "Server error", "error"=>$e->getMessage()]);
}
