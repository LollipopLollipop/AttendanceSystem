<?php
  
#**********************************************************
#Main script
#**********************************************************
#interact with MySQL database to check if usr exists 
echo "hello";
$servername = "localhost";
$username = "dingz";
$password = "150311";
$dbname = "AttendanceSystem";
echo "bp1\n";
$conn = new mysqli($servername, $username, $password, $dbname);
if($conn->connect_error){
	die("Connection failed: " . $conn->connect_error);
}
echo "bp2\n";
$filename = basename( $_FILES['uploadedfile']['name']); 
#$filename = "Ding_Zhao_0320.wav";
$splits = explode("_", $filename);
$usrname = $splits[0] . $splits[1];
$hashed_usrname = md5($usrname);
#echo $hashed_usrname;
$select_sql = "SELECT * FROM Users WHERE usrname = '".$hashed_usrname."';";
$select_result = $conn->query($select_sql);
if($select_result->num_rows<=0){
	$count_sql = "SELECT * FROM Users";
	$count_result = $conn->query($count_sql);
	#$user_count = (int)($count_result->fetch_assoc());
	$user_count = $count_result->num_rows;
	echo "row count ".$user_count
	$user_id = $user_count+1;
	$insert_sql = "INSERT INTO Users VALUES('".$hashed_usrname."',".$user_id.");";

	if($conn->query($insert_sql) === TRUE){
		echo "new record insertd";
	}else{
		echo "insert fails";
	}
}else{
	echo "user already existed";
}
$conn->close();

	#set target path for storing audio files on the server
	#$audio_upload_path = "/home/dingz/dataset/audiofiles/" .$hashed_usrname. "/";
	#mkdir($audio_upload_path);
	#$audio_upload_path = $audio_upload_path. basename( $_FILES['uploadedfile']['name']); 

	#get and store uploaded audio files on the server
	#if(copy($_FILES['uploadedfile']['tmp_name'], $audio_upload_path)) {
    	#	echo "Upload success!";
	#} else{
    	#	echo "There was an error uploading the file to $audio_upload_path !";
	#}

?>



