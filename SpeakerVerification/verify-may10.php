<?php
#function to update /eval/probes list
function addToModelsList($fileid)
{
#echo $userid;
#echo $fileid;
#update eval/model list 
$listfile = "/home/dingz/bob.spear-1.1.8/eggs/xbob.db.voxforge-0.1.0-py2.7.egg/xbob/db/voxforge/lists/eval/for_models.lst";
$content = file_get_contents($listfile);
#echo "bp2";
$content .= ("0/" . $fileid . " Unknown Unknown\n");
file_put_contents($listfile,$content);
echo "File list updated!\n";
#echo "bp3";
}

function removeFromModelsList()
{
$listfile = "/home/dingz/bob.spear-1.1.8/eggs/xbob.db.voxforge-0.1.0-py2.7.egg/xbob/db/voxforge/lists/eval/for_models.lst";
# add error checking!!!!!!!!!!!!!!!!!!!! only remove if exist
# load the data and delete the line from the array 
$lines = file($listfile); 
$last = sizeof($lines) - 1 ; 
unset($lines[$last]); 

# write the new data to the file 
$fp = fopen($listfile, 'w'); 
fwrite($fp, implode('', $lines)); 
fclose($fp); 
}

#function for copying file to local directory 
function copyFileToLocal($fileid){
#set target path for storing audio files on the server
$audio_upload_path = "/home/dingz/dataset/audiofiles/0/" .$fileid. ".wav";
#get and store uploaded audio files on the server
if(copy($_FILES['uploadedfile']['tmp_name'], $audio_upload_path)) {
        echo "File uploaded!\n";
        return TRUE;
} else{
        echo "There was an error uploading the file to $audio_upload_path !\n";
        return FALSE;
}
}

#cmp critetia when sorting the result
function cmp($line1, $line2){
	$line1elements = explode(" ", $line1);
	$line2elements = explode(" ", $line2);
	$score1 = $line1elements[3];
	$score2 = $line2elements[3];
	if($score1>$score2)
		return -1;
	else if($score1<$score2)
		return 1;
	else	
		return 0;
}

#clear history data that may disturb current verification run 
function clearHistoryData(){
	deleteAllFiles('/home/dingz/exp5/output/ubm_gmm/scores/nonorm/eval/*');
	$scoresevalfile = '/home/dingz/exp5/output/ubm_gmm/scores/nonorm/scores-eval';
	if(file_exists($scoresevalfile))
		unlink($scoresevalfile);
	echo "delete scores-eval file";
	deleteAllFiles('/home/dingz/exp5/tmp/ubm_gmm/models/eval/*');
	deleteAllFiles('/home/dingz/exp5/tmp/ubm_gmm/scores/zt_norm_A/eval/*');
}

#delete all files in given dir
function deleteAllFiles($dir){
	$files = glob($dir); // get all file names
        foreach($files as $file){ // iterate files
		#echo $file;
                if(is_file($file)){
                        unlink($file); // delete file
			#echo "unlink ".$file."\n";
		}
        }
        echo "delete all files in ".$dir." \n";
}
#**********************************************************
#Main script
#**********************************************************
$filename = basename( $_FILES['uploadedfile']['name']);
#$filename = "Fun_None_1.wav";
echo $filename;
$splits = split("[.]", $filename);
$fileid = $splits[0];
$scorefile = "/home/dingz/exp5/output/ubm_gmm/scores/nonorm/eval/Unknown.txt";
#copy uploaded file to local tmp dir
if(copyFileToLocal($fileid)===TRUE){
	#update eval/models lst
	removeFromModelsList();
	addToModelsList($fileid);
	#clear output and tmp dir if not empty
	clearHistoryData();
	#if(file_exists($scorefile)===TRUE){
	#	unlink($scorefile);
	#	echo "delete old unknown\n";
	#}
	#enter spear dir
	#$command = "cd /home/dingz/bob.spear-1.1.8";
	#exec($command);
	#echo exec("ls");
	chdir("/home/dingz/bob.spear-1.1.8/");
	#$myfile = fopen("newtextfile.txt","w");
	#echo "open new file\n";
	#$txt = "testtest\n";
	#fwrite($myfile,$txt);
	#fclose($myfile);

	#$myscorefile = fopen("/home/dingz/exp5/output/ubm_gmm/scores/nonorm/eval/testscorefile.txt","w");
	#echo "open test score file \n";
	#$scoretxt = "test test test\n";
	#fwrite($myscorefile, $scoretxt);
	#fclose($myscorefile);
	#chdir("/home/dingz/bob.spear-1.1.8/");
	
	#run experiment
	$command = "./bin/spkverif_gmm.py -d config/database/voxforge.py -p config/preprocessing/energy.py -f config/features/mfcc_60.py -t config/tools/ubm_gmm/ubm_gmm_256G.py -b ubm_gmm -z --user-directory /home/dingz/exp5/output --temp-directory /home/dingz/exp5/tmp 2>&1";
	#$command = "./bin/spkverif_gmm.py";
	chdir("/home/dingz/bob.spear-1.1.8/");
	$runresult = shell_exec($command);
	echo $runresult;
	#read scores file 
	#$command = "cat " . $scorefile ." | sort -n -r -k 4 | head -n 1 sorted.txt | cut -d' ' -f2";
	$scorelines = file($scorefile);
	usort($scorelines, "cmp");
	$bestline = explode(" ", $scorelines[0]);
	$bestmatch = $bestline[1];
	echo $bestmatch."\n";
	echo "Best match is ".$bestmatch."\n";
	#unlink($scorefile);
	#unlink("sorted.txt");
	#removeFromModelsList();
	chdir("/var/www/");
}
else{
	echo "Copy File Error, please try again...\n";
}
?>
