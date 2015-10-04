<?php
	if($_POST){
		$matchid = $_POST["matchid"];
		$findme = ":";
		$pos = strpos($matchid, $findme);
		if($pos===false){
		}
		else{
			#$matchid contains ":" means it is a explicit username
			$usernamesplits = split("[:]", $matchid);
			$username = $usernamesplits[1];
			#echo $username;
			#$fileid = $splits[1];
			$hashedname = md5($username);
			
			#interact with MySQL database to check if usr exists
			$servername = "localhost";
			$mysqlusername = "dingz";
			$mysqlpassword = "150311";
			$dbname = "AttendanceSystem";
			#echo "bp1\n";
			$conn = new mysqli($servername, $mysqlusername, $mysqlpassword, $dbname);
			if ($conn->connect_error) {
    			die("Connection to MySQL database failed: " . $conn->connect_error . "\nThe  developer is notified with this problem. Sorry for the inconvenience!\n");
        		error_log("Attendance System:\nFeedback Handling: Connection to MySQL database failed!", 1, "dingz@andrew.cmu.edu");
			}
			$readID_sql = "SELECT * FROM Users WHERE Username = '".$hashedname."';";
        	$readID_result = $conn->query($readID_sql);
        	$readID_row = $readID_result->fetch_assoc();
        	$matchid = $readID_row["UserID"];
			echo "Your user id in records is ".$matchid."\n";
		}
		$filename = $_POST["filename"];
		$splits = split("[._]", $filename);
		$fileid = $splits[1];
		$tmp_upload_path = "/home/dingz/dataset/audiofiles/0/".$filename;	
		$match_path = "/home/dingz/dataset/audiofiles/".$matchid."/".$fileid.".wav";
		if(copy($tmp_upload_path, $match_path)){
			unlink($tmp_upload_path);
			echo "Audio file relocated!\n";
			$listfile = "/home/dingz/bob.spear-1.1.8/eggs/xbob.db.voxforge-0.1.0-py2.7.egg/xbob/db/voxforge/lists/eval/for_probes.lst";
			$content = file_get_contents($listfile);
			$content .= ($matchid . "/" . $fileid . " " . $matchid . "\n");
			file_put_contents($listfile,$content);
			echo "Probes file list updated!\nThanks very much for your feedback!\n";
		}
		else {
			echo "There is an error relocating the audio file...\nThe developer is notified with this problem.\nSorry for the inconvenience!\n";
			error_log("Attendance System:\nFeedback Handling: Relocating file failed!", 1, "dingz@andrew.cmu.edu");
		}	
	}
	else {
		echo "There is an error sending feedback to the server...\nThe developer is notified with this problem.\nSorry for the inconvenience!\n";
		error_log("Attendance System:\nFeedback Handling: Receiving _POST data failed!", 1, "dingz@andrew.cmu.edu");
	}
?>
