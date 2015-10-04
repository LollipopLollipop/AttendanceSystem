<?php
  
#**********************************************************
#Main script
#**********************************************************
#interact with MySQL database to check if usr exists 
$servername = "localhost";
$mysqlusername = "dingz";
$mysqlpassword = "150311";
$dbname = "AttendanceSystem";
#echo "bp1\n";
$conn = new mysqli($servername, $mysqlusername, $mysqlpassword, $dbname);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
} 
#echo "bp2\n";
$filename = basename( $_FILES['uploadedfile']['name']); 
#$filename = "Z_Zhao_0320.wav";
$splits = split("[._]", $filename);
$username = $splits[0] . $splits[1];
$fileid = $splits[2];
$hashedname = md5($username);
#echo $hashed_usrname;
$check_sql = "SELECT COUNT(*) AS num_exist FROM Users WHERE Username = '".$hashedname."';";
$check_result = $conn->query($check_sql);
$check_row = $check_result->fetch_assoc();
$num_exist = $check_row["num_exist"];
#echo $num_exist."\n";
if($num_exist==0){
	$max_sql = "SELECT MAX(UserID) AS cur_max FROM Users;";
	$max_result = $conn->query($max_sql);
	$max_row = $max_result->fetch_assoc();
	$userid = $max_row["cur_max"] + 1;
	#echo $user_id;
	$insert_sql = "INSERT INTO Users VALUES('".$hashedname."',".$userid.");";
	if($conn->query($insert_sql) === TRUE){
		echo "MySQL table updated!\n";
		if(copyFileToLocal($userid, $fileid)===TRUE){
			updateProbesList($userid, $fileid);
			echo "Your student id for this course is ".$userid.".\n";
		}
		else{
			$delete_sql = "DELETE FROM Users WHERE Username = '".$hashedname."';";
			$delete_result = $conn->query($delete_sql);
			rmdir("/home/dingz/dataset/audiofiles/" .$userid. "/");
			echo "Please try again...\n";
		}
	}else{
		echo "Insert data to MySQL table failed...\n";
	}
}else{
	echo "You are not a new user...\n";
}
$conn->close();


#function for copying file to local directory 
function copyFileToLocal($userid, $fileid){
#set target path for storing audio files on the server
$audio_upload_path = "/home/dingz/dataset/audiofiles/" .$userid. "/";
mkdir($audio_upload_path);
$audio_upload_path = $audio_upload_path. $fileid . ".wav"; 
#get and store uploaded audio files on the server
if(copy($_FILES['uploadedfile']['tmp_name'], $audio_upload_path)) {
    	echo "File uploaded!\n";
	return TRUE;
} else{
	echo "There was an error uploading the file to $audio_upload_path !\n";
	return FALSE;
}
}
#function to update /eval/probes list
function updateProbesList($userid, $fileid)
{
#echo $userid;
#echo $fileid;
#update eval/model list 
$listfile = "/home/dingz/bob.spear-1.1.8/eggs/xbob.db.voxforge-0.1.0-py2.7.egg/xbob/db/voxforge/lists/eval/for_probes.lst";
$content = file_get_contents($listfile);
#echo "bp2";
$content .= ($userid . "/" . $fileid . " " . $userid . " " . $userid . "\n");
file_put_contents($listfile,$content);
echo "File list updated!\n";
#echo "bp3";
}

?>



