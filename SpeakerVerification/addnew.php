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
    die("Connection to MySQL database failed: " . $conn->connect_error . "\nThe  developer is notified with this problem. Sorry for the inconvenience!\n");
	error_log("Attendance System:\nAdding New Member: Connection to MySQL database failed!", 1, "dingz@andrew.cmu.edu");	
} 
#echo "bp2\n";
$filename = basename( $_FILES['uploadedfile']['name']); 
#$filename = "Z_Zhao_0320.wav";
$splits = split("[._]", $filename);
$username = $splits[0];
#echo $username;
$fileid = $splits[1];
$hashedname = md5($username);
#echo $hashedname;
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
	#echo "current userid is ".$userid;
	$insert_sql = "INSERT INTO Users VALUES('".$hashedname."',".$userid.");";
	if($conn->query($insert_sql) === TRUE){
		echo "MySQL Users table updated!\n";
		if(copyFileToLocal($userid, $fileid, 1)===TRUE){
			updateProbesList($userid, $fileid);
			echo "Thank you for registration! \nYour student id for this course is ".$userid.".\nPlease keep it for future reference =)";
		}
		else{
			$delete_sql = "DELETE FROM Users WHERE Username = '".$hashedname."';";
			$delete_result = $conn->query($delete_sql);
			#rmdir("/home/dingz/dataset/audiofiles/" .$userid. "/");
			echo "Restore MySQL Users table...\n";
		}
	}else{
		echo "Data insertion to MySQL table failed...\nThe developer is notified with this problem. Sorry for the inconvenience!\n";
		error_log("Attendance System:\nAdding New Member: Data insertion to MySQL table failed...", 1, "dingz@andrew.cmu.edu");
	}
}else{
	echo "Our records identifies you as an existing member.\n";
	$readID_sql = "SELECT * FROM Users WHERE Username = '".$hashedname."';";
	$readID_result = $conn->query($readID_sql);
   	$readID_row = $readID_result->fetch_assoc();
	$userid = $readID_row["UserID"];
   	echo "Your student id previously registered is ".$userid."\n";
	if(copyFileToLocal($userid, $fileid, 0)===TRUE){
		updateProbesList($userid, $fileid);
		echo "Newly recorded data is stored as part of the training samples.\n";
	}
	
}
$conn->close();


#function for copying file to local directory 
function copyFileToLocal($userid, $fileid, $isNew){
#set target path for storing audio files on the server
$audio_upload_path = "/home/dingz/dataset/audiofiles/" .$userid;
#echo $audio_upload_path;
#mkdir if not already exists
if($isNew==1){
	if(file_exists($audio_upload_path)){
		rmdir($audio_upload_path);
		#mkdir($audio_upload_path);
		#echo "not exits...mkdir";
	}
	#cretae new dir for new user
	mkdir($audio_upload_path);
}
else{
	if(!file_exists($audio_upload_path)){
		mkdir($audio_upload_path);
		error_log("Attendance System:\nAdding New Member: Missing folder for existing user ".$userid, 1, "dingz@andrew.cmu.edu");
	}
}
$audio_upload_path = $audio_upload_path. "/" . $fileid . ".wav"; 
#echo $audio_upload_path;
#get and store uploaded audio files on the server
if(copy($_FILES['uploadedfile']['tmp_name'], $audio_upload_path)) {
    	echo "File uploaded to the server!\n";
	return TRUE;
} else{
	echo "There was an error uploading the file...\nThe developer is notified with this problem. Sorry for the inconvenience!\n";
	#error_log("Attendance System:\nAdding New Member: File uploading error.", 1, "dingz@andrew.cmu.edu");
	return FALSE;
}
}
#function to update /eval/probes list
function updateProbesList($userid, $fileid)
{
#echo $userid;
#echo $fileid;
#update eval/model list 
$listfile = "/home/dingz/bob.spear-1.1.8/eggs/xbob.db.voxforge-0.1.0-py2.7.egg/xbob/db/voxforge/lists/eval/for_models.lst";
$content = file_get_contents($listfile);
#echo "bp2";
$content .= ($userid . "/" . $fileid . " " . $userid . " " . $userid . "\n");
file_put_contents($listfile,$content);
#update eval/probes list
$listfile = "/home/dingz/bob.spear-1.1.8/eggs/xbob.db.voxforge-0.1.0-py2.7.egg/xbob/db/voxforge/lists/eval/for_probes.lst";
$content = file_get_contents($listfile);
#echo "bp2";
$content .= ($userid . "/" . $fileid . " " . $userid . "\n");
file_put_contents($listfile,$content);
echo "Models and probes file list updated!\n";
#echo "bp3";
#error_log("file list updated", 1, "dingz@andrew.cmu.edu");
}

?>



