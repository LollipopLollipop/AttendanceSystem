package com.example.jodiezz.simplerecord;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

public class RecordActivity extends FragmentActivity implements AddNewMemberFragment.AddNewMemberListener {
    //configuration params for AudioRecord
    private AudioRecord recorder;
    private int buffSize;
    private final static int RECORDER_BPP = 16;
    private final static int SOURCE = MediaRecorder.AudioSource.MIC;
    //refers to the microphone audio source
    private final static int SAMPLE_RATE = 44100;
    //human vocie frequency range: 300-3.5K Hz
    private final static int CHANNEL_MODE = AudioFormat.CHANNEL_IN_STEREO;
    //describes the configuration of the audio channels
    private final static int ENCODING_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    //the audio data is represented PCM 16 bit per sample
    private static final String TAG = "RecordActivity";
    private Thread recordingThread = null;
    private boolean isRecording = false;
    private Context thisCtx = this;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.pcm";
    private static String wavFilePath = null;
    private static String wavFileName = null;
    private static String tempFilePath = null;
    private static String namePrefix = "Unknown_";
    public static InteractionView iv;
    private static boolean isCalledFromNewUserActivity = true;
    private String[] itemsToSelect = new String[4];

    private static AlertDialog alert;
    private static AlertDialog result;
    private static DialogFragment newFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        isCalledFromNewUserActivity = intent.getBooleanExtra("Calling Activity", true);
        System.out.println("is called from new activity " + isCalledFromNewUserActivity);
        //update nameprefix if called by new member
        if(isCalledFromNewUserActivity)
            namePrefix = intent.getStringExtra(MainActivity.NEW_USER_NAME)+"_";
        //System.out.println("bp1");
        setContentView(R.layout.activity_record);
        //System.out.println("bp2");
        LinearLayout ll = (LinearLayout)findViewById(R.id.dynamic_view);
        //System.out.println("bp3");
        iv = new InteractionView(this);
        //System.out.println("bp4");
        ll.addView(iv);
        //System.out.println("bp5");
        Button recordBtn = (Button)findViewById(R.id.record_btn);
        recordBtn.setOnClickListener(btnClick);




        //result.setMessage("Choose your student ID below");

        //add action bar, though no difference actually
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            System.out.println("isRecording is " + isRecording);
            if (!isRecording) {
                startRecording();
                ((Button) findViewById(R.id.record_btn)).setText("Stop Recording");
            } else {
                stopRecording();
                ((Button) findViewById(R.id.record_btn)).setText("Start Recording");
            }
            System.out.println("isRecording is " + isRecording);
        }
    };

    public void showPopupWindow(String response){
        //popupText.setText(response);
        //popupMessage.showAsDropDown(RecordActivity.iv, 0, -300);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Message From Server");
        alertBuilder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Intent backIntent = new Intent(RecordActivity.this, MainActivity.class);
                startActivity(backIntent);
            }
        });
        alertBuilder.setMessage(response);
        alert = alertBuilder.create();
        alert.show();
    }

    public void showVerificationResult(String response){
        AlertDialog.Builder resultBuilder = new AlertDialog.Builder(this);
        resultBuilder.setTitle("Verification Results(Choose your user ID below)");
        String[] top3results = response.split("/");
        itemsToSelect[0] = top3results[0];
        itemsToSelect[1] = top3results[1];
        itemsToSelect[2] = top3results[2];
        itemsToSelect[3] = "None of the Above";
        resultBuilder.setItems(itemsToSelect, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                //TalkToServer task = new TalkToServer(thisCtx, namePrefix, false);
                System.out.println("Selected item is " + item);

                if(item<3) {
                    TalkToServer task = new TalkToServer(2);
                    task.execute(itemsToSelect[item]);

                }
                else {
                    //TalkToServer task = new TalkToServer(2);
                    //task.execute("-1");
                    result.dismiss();
                    askForExplicitUserInfo();

                }
            }
        });
        result = resultBuilder.create();
        result.show();

    }

    private void askForExplicitUserInfo() {
        newFragment = new AddNewMemberFragment();
        FragmentManager fragmentManager = getFragmentManager();
        newFragment.show(fragmentManager, "explicit user info");
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String nameRecorded) {
        // User touched the dialog's positive button
        System.out.println("positive click detected");
        //proceedToRecord(nameRecorded);
        TalkToServer task = new TalkToServer(2);
        task.execute("explicit:"+nameRecorded);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        newFragment.dismiss();

    }

    private void startRecording(){
        System.out.println("Starting Recording");
        //set MIC as occupied when recording starts

        //calc min buffSize given above params
        buffSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_MODE, ENCODING_FORMAT);
        System.out.println("mini buff size is " + buffSize);
        //initialize recorder_ instance
        recorder = new AudioRecord(SOURCE,SAMPLE_RATE, CHANNEL_MODE, ENCODING_FORMAT, buffSize);
        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "cant initialize");
            return;
        }
        recorder.startRecording();
        isRecording = true;
        //thread to store recorded audio data to local file
        recordingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                writeAudioDataToFile();
            }
        },"AudioRecorder Thread");

        recordingThread.start();
        System.out.println("Exit Starting Recording");
    }


    private void stopRecording(){
        System.out.println("Stopping Recording");

        if(null != recorder) {
            isRecording = false;
            iv.stopUpdate();
            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }
        //convert .pcm to .wav
        tempFilePath = getTempFilename();
        wavFilePath = getFilename();
        copyWaveFile(tempFilePath,wavFilePath);
        removeFile(tempFilePath);
        System.out.println("starts to talk to server");
        TalkToServer task;
        if(isCalledFromNewUserActivity)
            task = new TalkToServer(0);
        else
            task = new TalkToServer(1);
        System.out.println(wavFilePath);
        task.execute();
        System.out.println("done talking to server");
        //removeFile(wavFileName);
        //Intent intent = new Intent(this, MainActivity.class);
        //startActivity(intent);
    }

    private void removeFile(String filePath) {
        File file = new File(filePath);
        file.delete();
    }

    private void copyWaveFile(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = 0;
        long longSampleRate = SAMPLE_RATE;
        int channels = 2;
        long byteRate = (RECORDER_BPP * SAMPLE_RATE * channels)/8;

        byte[] data = new byte[buffSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            AppLog.logString("File size: " + totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while(in.read(data) != -1){
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    private void writeAudioDataToFile() {
        //check if external storage is accessible
        if(!isExternalStorageReadable()||!isExternalStorageWritable()){
            Log.e(TAG,"External Storage Not Accessible");
            System.exit(1);
        }
        String filePath = getTempFilename();
        short data[] = new short[buffSize/2];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int read = 0;

        if(null != os){
            while(isRecording){
                read = recorder.read(data, 0, data.length);
                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    try {
                        byte bData[] = short2byte(data);
                        //pass audio data to UI view for update
                        iv.updateRealtimeData(bData);
                        os.write(bData, 0, buffSize);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,Environment.DIRECTORY_MUSIC);

        if(!file.exists()){
            file.mkdirs();
        }
        wavFileName = namePrefix + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV;
        return (file.getAbsolutePath() + "/" + wavFileName);
    }

    private String getTempFilename(){
        String filePath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filePath,Environment.DIRECTORY_MUSIC);

        if(!file.exists()){
            file.mkdirs();
        }

        File tempFile = new File(filePath, AUDIO_RECORDER_TEMP_FILE);

        if(tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }


    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_new_user, menu);
        return true;

        /* example code from Android Developer Page
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
        */
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //openSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class TalkToServer extends AsyncTask<String, Integer, Void> {
        //Task State
        private static final int UPLOADING_STATE = 0;
        private static final int SERVER_PROC_STATE = 1;
        private static final int SENDING_FEEDBACK_STATE = 2;
        private ProgressDialog dialog;
        private String SERVER_URL = null;
        private int callingActivity = -1;
        private final String TAG = "ServerActivity";
        private String  response;

        public TalkToServer(int callingActivity) {
            this.dialog = new ProgressDialog(thisCtx);
            this.callingActivity = callingActivity;
            if(callingActivity==0)
                this.SERVER_URL = "http://128.2.112.182/addnew.php";
            else if(callingActivity==1)
                this.SERVER_URL = "http://128.2.112.182/verify.php";
            else
                this.SERVER_URL = "http://128.2.112.182/processFB.php";
        }

        @Override
        protected Void doInBackground(String... params) {			//background operation
            if(callingActivity==2){
                System.out.println("before sending feedback");
                //int feedbackItem = Integer.parseInt(params[0]);
                processFeedback(params[0]);
                System.out.println("after sending feedback");
            }
            else {
                //String uploadFilePath = params[0];
                //String[] pathElements = uploadFilePath.split("/");
                //audioFileProcessed = pathElements[pathElements.length - 1];
                //System.out.println("audio file processed is " + audioFileProcessed);
                System.out.println("before processing audio");
                processAudio(wavFilePath);
                System.out.println("after processing audio");
                //File file = new File(wavFileName);
                //file.delete();
                //recordedAudioProcessed = true;
            }
            return null;
        }

        //Main code for processing audio algorithm on the server
        void processAudio(String inputAudioFilePath){
            System.out.println("TalkToServer: " + inputAudioFilePath);
            publishProgress(UPLOADING_STATE);
            File inputFile = new File(inputAudioFilePath);
            try {
                //create file stream for captured image file
                FileInputStream fis  = new FileInputStream(inputFile);

                //upload photo
                final HttpURLConnection conn = uploadAudio(fis);

                //get processed result from server
                if (conn != null){
                    getServerResponse(conn);
                }
                fis.close();
            }
            catch (FileNotFoundException ex){
                Log.e(TAG, ex.toString());
            }
            catch (IOException ex){
                Log.e(TAG, ex.toString());
            }
        }

        //main code for processing user feedback on the server
        void processFeedback(String matchid){
            publishProgress(SENDING_FEEDBACK_STATE);
            final HttpURLConnection conn = sendFeedback(matchid);
            if(conn != null){
                getServerResponse(conn);
            }
        }

        //send user feedback to server
        HttpURLConnection sendFeedback(String matchid){
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //conn.setReadTimeout(10000);
                //conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                String param="matchid=" + URLEncoder.encode(matchid, "UTF-8")+
                        "&filename="+URLEncoder.encode(wavFileName,"UTF-8");
                System.out.println("feedback sent is " + matchid + " | " + wavFileName);
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(param);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();
                return conn;
            }catch (MalformedURLException ex){
                Log.e(TAG, "error: " + ex.getMessage(), ex);
                return null;
            }
            catch (IOException ioe){
                Log.e(TAG, "error: " + ioe.getMessage(), ioe);
                return null;
            }
            /*
            final String lineEnd = "\r\n";
            final String twoHyphens = "--";
            final String boundary = "*****";
            final String postData = Integer.toString(item) + ":" + wavFileName;
            System.out.println("inside sendFeedback postData is " + postData);
            try
            {
                URL url = new URL("http://128.2.112.182/processFB.php");
                // Open a HTTP connection to the URL
                final HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                //conn.setRequestProperty("Set-Cookie", sessionCookie);
                conn.setRequestProperty("Content-Length", "" + Integer.toString(postData.getBytes().length));

                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                //String str =  "some string goes here";
                byte[] outputInBytes = postData.getBytes("UTF-8");
                OutputStream os = conn.getOutputStream();
                os.write( outputInBytes );
                os.close();

                return conn;
            }
            catch (MalformedURLException ex){
                Log.e(TAG, "error: " + ex.getMessage(), ex);
                return null;
            }
            catch (IOException ioe){
                Log.e(TAG, "error: " + ioe.getMessage(), ioe);
                return null;
            }*/
        }


        //upload audio to server
        HttpURLConnection uploadAudio(FileInputStream fis){

            final String lineEnd = "\r\n";
            final String twoHyphens = "--";
            final String boundary = "*****";

            try
            {
                System.out.println(SERVER_URL);
                URL url = new URL(SERVER_URL);
                // Open a HTTP connection to the URL
                final HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                // Allow Inputs
                conn.setDoInput(true);
                // Allow Outputs
                conn.setDoOutput(true);
                // Don't use a cached copy.
                conn.setUseCaches(false);

                // Use a post method.
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

                DataOutputStream dos = new DataOutputStream( conn.getOutputStream() );

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + wavFileName +"\"" + lineEnd);
                dos.writeBytes(lineEnd);

                // create a buffer of maximum size
                int bytesAvailable = fis.available();
                int maxBufferSize = 1024;
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                byte[] buffer = new byte[bufferSize];

                // read file and write it into form...
                int bytesRead = fis.read(buffer, 0, bufferSize);

                while (bytesRead > 0)
                {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fis.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fis.read(buffer, 0, bufferSize);
                }

                // send multipart form data after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                publishProgress(SERVER_PROC_STATE);
                // close streams
                fis.close();
                dos.flush();

                return conn;
            }
            catch (MalformedURLException ex){
                Log.e(TAG, "error: " + ex.getMessage(), ex);
                return null;
            }
            catch (IOException ioe){
                Log.e(TAG, "error: " + ioe.getMessage(), ioe);
                return null;
            }
        }

        //get response from server and display it
        void getServerResponse(HttpURLConnection conn){
            System.out.println("starts to get response from server");
            // retrieve the response from server
            InputStream is;
            try {
                is = conn.getInputStream();
                //get result feedback from server
                response = convertStreamToString(is);
                is.close();

            } catch (IOException e) {
                Log.e(TAG,e.toString());
                e.printStackTrace();
            }
        }

        private String convertStreamToString(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }


        //progress update, display dialogs
        protected void onPreExecute() {
            this.dialog.setMessage("Audio captured");
            this.dialog.show();
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
            if(progress[0] == UPLOADING_STATE){
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                dialog.setMessage("Uploading");
                dialog.show();
            }
            else if (progress[0] == SERVER_PROC_STATE){
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                dialog.setMessage("Processing");
                dialog.show();
            }
            else {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                dialog.setMessage("Sending Feedback");
                dialog.show();
            }
        }
        @Override
        protected void onPostExecute(Void param) {
            System.out.println("onPostExe called");
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            System.out.println("before showing pop");
            if(callingActivity==1){
                //OK: header indicates the response as in the category of verification top3 results
                if(response.startsWith("RESULTS:")){
                    //verification results get shown in different format
                    System.out.println("show results");
                    showVerificationResult(response.substring(8));
                }
                else{
                    showPopupWindow(response);
                }
            }
            else {
                showPopupWindow(response);
            }

        }

    }

}
