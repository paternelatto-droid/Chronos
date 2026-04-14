<?php
require "config.php";

// Désactive tout affichage inutile pour ne renvoyer que du JSON
error_reporting(0);

// En-têtes
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Accept, Authorization");

$method = $_SERVER['REQUEST_METHOD'];
if ($method === 'OPTIONS') { http_response_code(200); exit; }
if ($method !== 'POST') { http_response_code(405); echo json_encode(["error"=>"Method not allowed"]); exit; }

// Lecture JSON
$data = json_decode(file_get_contents("php://input"), true);
if (!$data || !isset($data['username']) || !isset($data['password'])) {
    http_response_code(400);
    echo json_encode(["success"=>false,"message"=>"Username et password requis"]);
    exit;
}

$username = trim($data['username']);
$password = trim($data['password']);

// Clé secrète pour JWT
define("JWT_SECRET", $password);
// Durée du token en secondes (ex : 1 heure)
define("JWT_EXPIRATION", 3600);

try {
    $pdo = get_db();

    // Vérifier utilisateur
    $stmt = $pdo->prepare("SELECT id, username, password, name, email, is_active, role_id FROM users WHERE username = :username LIMIT 1");
    $stmt->execute([':username'=>$username]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);
    if (!$user) { echo json_encode(["success"=>false,"message"=>"Utilisateur introuvable"]); exit; }
    if ((int)$user['is_active'] !== 1) { echo json_encode(["success"=>false,"message"=>"Compte désactivé"]); exit; }
    if (!password_verify($password, $user['password'])) { echo json_encode(["success"=>false,"message"=>"Mot de passe incorrect"]); exit; }

    // Récupérer rôle et permissions
    $roleStmt = $pdo->prepare("SELECT name FROM roles WHERE id=?");
    $roleStmt->execute([$user['role_id']]);
    $roleName = $roleStmt->fetchColumn() ?: "";

    $permStmt = $pdo->prepare("
        SELECT p.key 
        FROM role_permissions rp
        INNER JOIN permissions p ON p.id = rp.permission_id
        WHERE rp.role_id = ?
    ");
    $permStmt->execute([$user['role_id']]);
    $permissions = $permStmt->fetchAll(PDO::FETCH_COLUMN);

    // Générer JWT local
    $header = base64_encode(json_encode(["alg"=>"HS256","typ"=>"JWT"]));
    $exp = time() + JWT_EXPIRATION;
    $payload = base64_encode(json_encode([
        "sub"=>$user['id'],
        "username"=>$user['username'],
        "role"=>$roleName,
        "permissions"=>$permissions,
        "iat"=>time(),
        "exp"=>$exp
    ]));
    $signature = hash_hmac('sha256', "$header.$payload", JWT_SECRET, true);
    $jwt = "$header.$payload." . base64_encode($signature);

    // Réponse JSON
    echo json_encode([
        "success"=>true,
        "token"=>$jwt,
        "token_expires_in"=>JWT_EXPIRATION,
        "user"=>[
            "id"=>intval($user['id']),
            "username"=>$user['username'],
            "name"=>$user['name'],
            "email"=>$user['email'],
            "role_id"=>intval($user['role_id']),
            "role_name"=>$roleName,
            "permissions"=>$permissions
        ]
    ]);

} catch(Exception $e){
    http_response_code(500);
    echo json_encode(["success"=>false,"message"=>"Erreur serveur: ".$e->getMessage()]);
}
?>
