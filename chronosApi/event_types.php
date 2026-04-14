<?php
require "config.php"; // Connexion à la base $pdo

// Forcer le type de contenu JSON
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Accept, Authorization");

$pdo = get_db();
$method = $_SERVER['REQUEST_METHOD'];

switch($method){
    case 'GET':
        $stmt = $pdo->query("SELECT * FROM event_types");
        echo json_encode($stmt->fetchAll());
        break;

    case 'POST':
        $data = json_decode(file_get_contents("php://input"), true);
        $stmt = $pdo->prepare("INSERT INTO event_types (name, description, has_duration) VALUES (:name, :description, :has_duration)");
        $stmt->execute([
            ':name'=>$data['name'],
            ':description'=>$data['description'] ?? '',
            ':has_duration'=>$data['has_duration'] ?? 0
        ]);
        echo json_encode(["success"=>true, "id"=>$pdo->lastInsertId()]);
        break;

    case 'PUT':
        $data = json_decode(file_get_contents("php://input"), true);
        $stmt = $pdo->prepare("UPDATE event_types SET name=:name, description=:description, has_duration=:has_duration WHERE id=:id");
        $stmt->execute([
            ':name'=>$data['name'],
            ':description'=>$data['description'] ?? '',
            ':has_duration'=>$data['has_duration'] ?? 0,
            ':id'=>$data['id']
        ]);
        echo json_encode(["success"=>true]);
        break;

    case 'DELETE':
        $id = intval($_GET['id'] ?? 0);
        if($id>0){
            $stmt = $pdo->prepare("DELETE FROM event_types WHERE id=:id");
            $stmt->execute([':id'=>$id]);
            echo json_encode(["success"=>true]);
        }else{
            http_response_code(400);
            echo json_encode(["error"=>"Invalid id"]);
        }
        break;
}
?>
