<?php
require "config.php";

header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Accept, Authorization");

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'OPTIONS') {
    http_response_code(200);
    exit;
}

if ($method !== 'POST') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Method not allowed"]);
    exit;
}

$data = json_decode(file_get_contents("php://input"), true);

if (!$data || !isset($data['username']) || !isset($data['password']) || !isset($data['email']) || !isset($data['name'])) {
    echo json_encode(["success" => false, "message" => "Tous les champs sont obligatoires"]);
    exit;
}

$username = trim($data['username']);
$password = trim($data['password']);
$email = trim($data['email']);
$name = trim($data['name']);

try {
    $pdo = get_db();

    // Vérifier si username ou email existe déjà
    $stmt = $pdo->prepare("SELECT id FROM users WHERE username = :username OR email = :email");
    $stmt->execute([':username' => $username, ':email' => $email]);

    if ($stmt->fetch()) {
        echo json_encode(["success" => false, "message" => "Nom d'utilisateur ou email déjà utilisé"]);
        exit;
    }

    // Enregistrement
    $hashed_password = password_hash($password, PASSWORD_DEFAULT);
    $stmt = $pdo->prepare("INSERT INTO users (name, email, username, password, is_active) VALUES (:name, :email, :username, :password, 1)");
    $stmt->execute([
        ':name' => $name,
        ':email' => $email,
        ':username' => $username,
        ':password' => $hashed_password
    ]);

    echo json_encode(["success" => true, "message" => "Utilisateur créé avec succès"]);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Erreur serveur: " . $e->getMessage()]);
}
?>
