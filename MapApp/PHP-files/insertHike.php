<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {

	include_once 'connection.php';

	$title = $_POST["title"];	
	$name = $_POST["name"];
	$participants = $_POST["participants"];
	$weather = $_POST["weather"];	
	$description = $_POST["description"];
	$startdate = $_POST["startdate"];
	$enddate = $_POST["enddate"];	
	$mapfile = $_POST["mapfile"];
	$distance = $_POST["distance"];
	$observationPoints = $_POST["observationPoints"];	
	$track = $_POST["track"];
	$userId = $_POST["userId"];
	$localId = $_POST["localId"];
	
	$sql = "SELECT * FROM hike WHERE userId='$userId' AND localId='$localId';";
	$result = mysqli_query($connect, $sql);
	if (mysqli_num_rows($result) > 0) {
		$json['error'] = 'Turen er allerede synkronisert';
		echo json_encode($json);
		mysqli_close($connect);
	} else {
		$sql = "INSERT INTO hike (title, name, participants, weather, description, startdate, enddate, mapfile, distance, observationPoints, track, userId, localId) VALUES ('$title', '$name', '$participants', '$weather', '$description', '$startdate', '$enddate', '$mapfile', '$distance', '$observationPoints', '$track', '$userId', '$localId');";

		$result = mysqli_query($connect, $sql);
		if ($result == 1 ) {
			$json['success'] = 'Tur synkronisert';
		} else {
			$json['error'] = 'Det skjedde en feil ved synkronisering';
		}
		echo json_encode($json);
		mysqli_close($connect);
	}
	
} else {
	// Echo json error
	$json['error'] = 'Det skjedde en feil ved synkronisering';
	echo json_encode($json);
	mysqli_close($connect);
}