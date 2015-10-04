<?php
  
#**********************************************************
#Main script
#**********************************************************

#set target path for storing audio files on the server
$audio_upload_path = "/home/dingz/dataset/audiofiles/";
$audio_upload_path = $audio_upload_path. basename( $_FILES['uploadedfile']['name']); 

#get and store uploaded audio files on the server
if(copy($_FILES['uploadedfile']['tmp_name'], $audio_upload_path)) {
    echo "Upload success!";
} else{
    echo "There was an error uploading the file to $audio_upload_path !";
}

?>



