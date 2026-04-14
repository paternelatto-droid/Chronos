<?php
require "config.php";
header("Content-Type: application/json; charset=UTF-8");

try {
    $pdo = get_db();

    // $query = $pdo->prepare("SELECT * FROM members ORDER BY first_name ASC");
    // $query->execute();
    // $members = $query->fetchAll(PDO::FETCH_ASSOC);
    $query = "
        SELECT 
            m.*,
            u.id as user_id,
            u.username,
            u.is_active,
            r.name AS role_name,
            r.id AS role_id
        FROM members m
        LEFT JOIN users u ON u.member_id = m.id
        LEFT JOIN roles r ON r.id = u.role_id
        ORDER BY m.id DESC
    ";
    $stmt = $pdo->query($query);
    $members = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        "success" => true,
        "members" => $members
    ], JSON_UNESCAPED_UNICODE);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "error" => $e->getMessage()
    ], JSON_UNESCAPED_UNICODE);
}
