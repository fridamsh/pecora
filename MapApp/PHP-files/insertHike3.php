<?php
if($_SERVER["REQUEST_METHOD"]=="POST"){
	require 'connection.php';
	createHike();
}
function createHike()
{
	global $connect;
	
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

	$query = "Select * from hike where userId='$userId' and localId = '$localId' ";
	$result = mysqli_query($connect, $query);
	if (mysqli_num_rows($result)>0) {
		$json['error'] = 'Hike already exist in DB';
		echo json_encode($json);
		mysqli_close($connect);
	} else {
		$query = "Insert into hike(title,name,participants,weather,description,startdate,enddate,mapfile,distance,observationPoints,track,userId,localId) values('$title','$name','$participants','$weather','$description','$startdate','$enddate','$mapfile','$distance','$observationPoints','$track','$userId','$localId');";

		$inserted = mysqli_query($connect, $query);
		if ($inserted == 1) {
			$json['success'] = 'Inserted hike';
		} else {
			$json['error'] = 'Something went wrong';
		}
		echo json_encode($json);
		mysqli_close($connect);
	}

	#mysqli_query($connect, $query) or die (mysqli_error($connect));
	#mysqli_close($connect);
	
}
?>