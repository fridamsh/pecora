<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {

	include_once 'connection.php';

	$uid = mysqli_real_escape_string($connect, $_POST['username']);
	$pwd = mysqli_real_escape_string($connect, $_POST['password']);

	//Error handlers
	//Check if inputs are empty
	if (empty($uid) || empty($pwd)) {
		// Echo json error
		$json['error'] = 'E1';
		echo json_encode($json);
		mysqli_close($connect);
	} else {
		$sql = "SELECT * FROM users WHERE user_uid='$uid' OR user_email='$uid';";
		$result = mysqli_query($connect, $sql);
		$resultCheck = mysqli_num_rows($result);
		if ($resultCheck < 1) {
			// Echo json error - user does not exist
			$json['error'] = 'E2';
			echo json_encode($json);
			mysqli_close($connect);
		} else {
			if ($row = mysqli_fetch_assoc($result)) {
				//De-hashing the password
				$hashedPwdChecked = password_verify($pwd, $row['user_pwd']);
				if ($hashedPwdChecked == false) {
					// Echo json error - wrong password
					$json['error'] = 'E3';
					echo json_encode($json);
					mysqli_close($connect);
				} elseif ($hashedPwdChecked == true) {
					//Log in the user here
					$json['success'] = $row['user_id'] . ' ' . $row['user_first'] . ' ' . $row['user_last'] . ' ' . $row['user_email'] . ' ' . $row['user_uid'] . ' Welcome ' . $row['user_first'];
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