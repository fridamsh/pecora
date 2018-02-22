<?php
include_once 'connection2.php';
	
	class User {
		
		private $db;
		private $connection;
		
		function __construct() {
			$this -> db = new DB_Connection();
			$this -> connection = $this->db->getConnection();
		}
		
		public function does_user_exist($email,$password)
		{
			$query = "Select * from user where username='$email' and password='$password'";
			$result = mysqli_query($this->connection, $query);
			if (mysqli_num_rows($result)>0) {
				$row = mysqli_fetch_assoc($result)
				$json['success'] = 'Id:' . $row["id"] . 'Welcome ' . $email;
				echo json_encode($json);
				mysqli_close($this -> connection);
			} else {
				$query = "Insert into user(username, password) values('$email','$password')";
				$inserted = mysqli_query($this -> connection, $query);
				if ($inserted == 1) {
					$id = mysqli_insert_id($this -> connection);
					$json['success'] = 'Id:'. $id . ' Account created';
				} else {
					$json['error'] = 'Wrong password';
				}
				echo json_encode($json);
				mysqli_close($this->connection);
			}
			
		}
		
	}
	
	
	$user = new User();
	if(isset($_POST['username'],$_POST['password'])) {
		$email = $_POST['username'];
		$password = $_POST['password'];
		
		if (!empty($email) && !empty($password)) {
			
			$encrypted_password = md5($password);
			$user-> does_user_exist($email,$encrypted_password);
			
		} else {
			$json['error']='You must type both inputs.';
			echo json_encode($json);
		}	
	}
?>