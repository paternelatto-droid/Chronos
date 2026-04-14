<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

require "config.php";

header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Accept, Authorization");

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
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

    // Champs membre
    $first_name = trim($data['first_name'] ?? '');
    $last_name  = trim($data['last_name'] ?? '');
    $gender     = $data['gender'] ?? null;
    $phone      = $data['phone'] ?? null;
    $email      = $data['email'] ?? null;
    $address    = $data['address'] ?? null;
    $dob        = $data['date_of_birth'] ?? null;
    $baptism    = $data['baptism_date'] ?? null;
    $status     = $data['status'] ?? 'member';

    $create_user = isset($data['create_user']) && $data['create_user'] === true;

    $selectedRoleId    = $data['selectedRoleId'] ?? 3;

    if (empty($first_name) || empty($last_name)) {
        http_response_code(400);
        echo json_encode(["success" => false, "message" => "Missing required fields (first_name/last_name)"]);
        exit;
    }

    // ✅ INSERT membre
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

    $userCreated = false;
    $generatedUsername = null;
    $generatedPassword = null;

    // ✅ Création user SI demandé
    if ($create_user) {

        //verification username
        $username = trim($data['username'] ?? null);
        if ($username == null) {
            // Génération username de base
            $baseUsername = strtolower(preg_replace('/[^a-z]/i', '', $first_name)) . "." . strtolower(preg_replace('/[^a-z]/i', '', $last_name));
            $generatedUsername = $baseUsername;
        }else{
            $generatedUsername = $username;
        }

        $suffix = 1;

        // Vérifier doublons username
        $checkUser = $pdo->prepare("SELECT id FROM users WHERE username = ?");
        while (true) {
            $checkUser->execute([$generatedUsername]);
            if ($checkUser->fetch()) {
                $generatedUsername = $baseUsername . "." . str_pad($suffix, 3, "0", STR_PAD_LEFT);
                $suffix++;
            } else {
                break;
            }
        }

        //verification password
        // Génération mot de passe du style Nom@JJMMAAAA
        $password = trim($data['password'] ?? null);
        if ($password == null) {
            $generatedPassword = ucfirst(strtolower($last_name)) . "@" . date("dmY");
            $hashedPassword = password_hash($generatedPassword, PASSWORD_BCRYPT);
        }else{
            $hashedPassword = password_hash($password, PASSWORD_BCRYPT);
        }

        try {
            $stmtUser = $pdo->prepare("
                INSERT INTO users (name, email, username, password, role_id, member_id, is_active)
                VALUES (:name, :email, :username, :password, :role_id, :member_id, 1)
            ");

            $stmtUser->execute([
                ':name' => $first_name . " " . $last_name,
                ':email' => $email,
                ':username' => $generatedUsername,
                ':password' => $hashedPassword,
                ':role_id' => $selectedRoleId,
                ':member_id' => $memberId
            ]);

            $userCreated = true;

        } catch (Exception $e) {
            // ❌ Erreur user → on ne supprime PAS le membre (selon ta consigne)
            error_log("User creation failed but member kept: " . $e->getMessage());
        }
    }

    echo json_encode([
        "success" => true,
        "member_id" => $memberId,
        "user_created" => $userCreated,
        "generated_username" => $generatedUsername,
        "generated_password" => $generatedPassword,
        "message" => $userCreated ? "Membre + utilisateur créés" : "Membre créé (user non créé ou non demandé)"
    ]);

} catch (Exception $e) {
    http_response_code(500);
    error_log("members_create_user error: " . $e->getMessage());
    echo json_encode(["success" => false, "message" => "Server error", "error" => $e->getMessage()]);
}
