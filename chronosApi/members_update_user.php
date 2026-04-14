<?php
// members_update_user.php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

require "config.php";
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Accept, Authorization");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Method not allowed"]);
    exit;
}

// ✅ RÉCEPTION DU JSON
$input = file_get_contents("php://input");
$data = json_decode($input, true);

if (!$data || !isset($data['member_id'])) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Invalid JSON"]);
    exit;
}

try {
    $pdo = get_db();
    $pdo->beginTransaction();

    // Required member id for update
    $memberId = isset($data['member_id']) ? intval($data['member_id']) : 0;
    if ($memberId <= 0) {
        http_response_code(400);
        echo json_encode(["success" => false, "message" => "member_id required"]);
        exit;
    }

    // Member fields (nullable)
    $first_name = trim($data['first_name'] ?? '');
    $last_name  = trim($data['last_name'] ?? '');
    $gender     = $data['gender'] ?? null;
    $phone      = $data['phone'] ?? null;
    $email      = $data['email'] ?? null;
    $address    = $data['address'] ?? null;
    $dob        = $data['date_of_birth'] ?? null;
    $baptism    = $data['baptism_date'] ?? null;
    $status     = $data['status'] ?? null;

    // Update members (only provided fields)
    $fields = [];
    $params = [':member_id' => $memberId];

    if ($first_name !== '') { $fields[] = "first_name = :first_name"; $params[':first_name'] = $first_name; }
    if ($last_name  !== '') { $fields[] = "last_name = :last_name";   $params[':last_name']  = $last_name; }
    if ($gender     !== null) { $fields[] = "gender = :gender";       $params[':gender'] = $gender; }
    if ($phone      !== null) { $fields[] = "phone = :phone";         $params[':phone'] = $phone; }
    if ($email      !== null) { $fields[] = "email = :email";         $params[':email'] = $email; }
    if ($address    !== null) { $fields[] = "address = :address";     $params[':address'] = $address; }
    if ($dob        !== null) { $fields[] = "date_of_birth = :dob";   $params[':dob'] = $dob; }
    if ($baptism    !== null) { $fields[] = "baptism_date = :baptism"; $params[':baptism'] = $baptism; }
    if ($status     !== null) { $fields[] = "status = :status";       $params[':status'] = $status; }

    if (count($fields) > 0) {
        $sql = "UPDATE members SET " . implode(", ", $fields) . " WHERE id = :member_id";
        $stmt = $pdo->prepare($sql);
        $stmt->execute($params);
    }

    // User handling
    $create_user = isset($data['create_user']) ? (bool)$data['create_user'] : false;
    $providedUsername = isset($data['username']) ? trim($data['username']) : null;
    $providedPassword = isset($data['password']) ? $data['password'] : null; // may be null -> do not update
    $selectedRoleId = isset($data['role_id']) ? intval($data['role_id']) : 1;
    $providedIsActive = isset($data['is_active']) ? intval($data['is_active']) : 0;

    // Check if a user already exists for this member
    $stmtCheck = $pdo->prepare("SELECT id, username, password FROM users WHERE member_id = :member_id LIMIT 1");
    $stmtCheck->execute([':member_id' => $memberId]);
    $existingUser = $stmtCheck->fetch(PDO::FETCH_ASSOC);

    $userUpdated = false;
    $userCreated = false;
    $generatedPassword = null;

    if ($existingUser) {
        // There is an associated user
        $userId = intval($existingUser['id']);
        // If create_user is false, we still allow updating user fields if provided (or you can choose to skip)
        // Build update for user with provided fields
        $userFields = [];
        $userParams = [':id' => $userId];

        if ($providedUsername !== null && $providedUsername !== '') {
            $userFields[] = "username = :username";
            $userParams[':username'] = $providedUsername;
        }
        if ($email !== null) {
            $userFields[] = "email = :email";
            $userParams[':email'] = $email;
        }
        if ($first_name !== '' || $last_name !== '') {
            $name = trim(($first_name !== '' ? $first_name : '') . ' ' . ($last_name !== '' ? $last_name : ''));
            if ($name === '') $name = null;
            if ($name !== null) {
                $userFields[] = "name = :name";
                $userParams[':name'] = $name;
            }
        }
        if ($selectedRoleId !== null) {
            $userFields[] = "role_id = :role_id";
            $userParams[':role_id'] = $selectedRoleId;
        }
        if ($providedIsActive !== null) {
            $userFields[] = "is_active = :is_active";
            $userParams[':is_active'] = $providedIsActive ? 1 : 0;
        }
        // password: only update if provided AND non-empty
        if ($providedPassword !== null && $providedPassword !== '') {
            $userFields[] = "password = :password";
            $userParams[':password'] = password_hash($providedPassword, PASSWORD_BCRYPT);
        }

        if (count($userFields) > 0) {
            $sqlUser = "UPDATE users SET " . implode(", ", $userFields) . " WHERE id = :id";
            $stmtUpd = $pdo->prepare($sqlUser);
            $stmtUpd->execute($userParams);
            $userUpdated = true;
        }
    } else {
        // No user exists for member
        if ($create_user) {
            // create a username if not provided
            if (!$providedUsername || $providedUsername === '') {
                $baseUsername = strtolower(preg_replace('/[^a-z0-9]/i', '', $first_name)) . "." . strtolower(preg_replace('/[^a-z0-9]/i', '', $last_name));
                $generatedUsername = $baseUsername;
                $suffix = 1;
                $checkUserStmt = $pdo->prepare("SELECT id FROM users WHERE username = ?");
                while (true) {
                    $checkUserStmt->execute([$generatedUsername]);
                    if ($checkUserStmt->fetch()) {
                        $generatedUsername = $baseUsername . "." . str_pad($suffix, 3, "0", STR_PAD_LEFT);
                        $suffix++;
                    } else {
                        break;
                    }
                }
                $providedUsername = $generatedUsername;
            }

            // generate password if not provided
            if (!$providedPassword || $providedPassword === '') {
                $generatedPassword = ucfirst(strtolower($last_name)) . "@" . date("dmY");
                $hashedPassword = password_hash($generatedPassword, PASSWORD_BCRYPT);
            } else {
                $hashedPassword = password_hash($providedPassword, PASSWORD_BCRYPT);
            }

            $stmtIns = $pdo->prepare("
                INSERT INTO users (name, email, username, password, role_id, member_id, is_active)
                VALUES (:name, :email, :username, :password, :role_id, :member_id, :is_active)
            ");

            $nameForUser = trim($first_name . ' ' . $last_name);
            $isActive = ($providedIsActive !== null) ? ($providedIsActive ? 1 : 0) : 1;

            try {
                $stmtIns->execute([
                    ':name' => $nameForUser,
                    ':email' => $email,
                    ':username' => $providedUsername,
                    ':password' => $hashedPassword,
                    ':role_id' => $selectedRoleId ?: 1,
                    ':member_id' => $memberId,
                    ':is_active' => $isActive
                ]);
                $userCreated = true;
            } catch (Exception $e) {
                // If user creation fails, we keep member and propagate a warning message
                error_log("User creation failed: " . $e->getMessage());
            }
        }
    }

    $pdo->commit();

    $msg = "Membre mis à jour";
    if ($userCreated) $msg .= " et utilisateur créé";
    elseif ($userUpdated) $msg .= " et utilisateur mis à jour";

    echo json_encode([
        "success" => true,
        "member_id" => $memberId,
        "user_created" => $userCreated,
        "user_updated" => $userUpdated,
        "generated_password" => $generatedPassword,
        "message" => $msg
    ]);

} catch (Exception $e) {
    if ($pdo->inTransaction()) $pdo->rollBack();
    http_response_code(500);
    error_log("members_update_user error: " . $e->getMessage());
    echo json_encode(["success" => false, "message" => "Server error", "error" => $e->getMessage()]);
}
