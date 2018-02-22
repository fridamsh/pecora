<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {

	include_once 'connection.php';
	
	$first = mysqli_real_escape_string($connect, $_POST['first']);
	$last = mysqli_real_escape_string($connect, $_POST['last']);
	$email = mysqli_real_escape_string($connect, $_POST['email']);
	$uid = mysqli_real_escape_string($connect, $_POST['uid']);
	$pwd = mysqli_real_escape_string($connect, $_POST['pwd']);

	//Error handlers
	//Check for empty fields
	if (empty($first) || empty($last) || empty($email) || empty($uid) || empty($pwd)) {
		$json['error'] = 'E1';
		echo json_encode($json);
		mysqli_close($connect);
	} else {
		//Check if input characters are valid
		if (!preg_match("/^[a-zA-Z]*$/", $first) || !preg_match("/^[a-zA-Z]*$/", $last)) {
			// Echo json error - not valid first and lastname
			$json['error'] = 'E2';
			echo json_encode($json);
			mysqli_close($connect);
		} else {
			//Check if email is valid
			if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
				// Echo json error - email not valid
				$json['error'] = 'E3';
				echo json_encode($json);
				mysqli_close($connect);
			} else {
				$sql = "SELECT * FROM users WHERE user_uid='$uid'";
				$result = mysqli_query($connect, $sql);
				$resultCheck = mysqli_num_rows($result);

				if ($resultCheck > 0) {
					// Echo json error - user taken
					$json['error'] = 'E4';
					echo json_encode($json);
					mysqli_close($connect);
				} else {
					//Hashing the password
					$hashedPwd = password_hash($pwd, PASSWORD_DEFAULT);
					//Insert the user into the database
					$sql = "INSERT INTO users (user_first, user_last, user_email, user_uid, user_pwd) VALUES ('$first', '$last', '$email', '$uid', '$hashedPwd');";

					$result = mysqli_query($connect, $sql);
					if ($result == 1 ) {
						$json['success'] = 'Inserted user';
					} else {
						$json['error'] = 'E5';
					}
					echo json_encode($json);
					mysqli_close($connect);
				}
			}
		}
	}

} else {
	// Echo json error
	$json['error'] = 'E5';
	echo json_encode($json);
	mysqli_close($connect);
}

