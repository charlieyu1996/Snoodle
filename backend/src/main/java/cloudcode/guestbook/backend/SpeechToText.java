package cloudcode.guestbook.backend;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.longrunning.OperationTimedPollAlgorithm;
import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.retrying.TimedRetryAlgorithm;
import com.google.cloud.speech.v1.LongRunningRecognizeMetadata;
import com.google.cloud.speech.v1.LongRunningRecognizeResponse;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.TargetDataLine;
import org.threeten.bp.Duration;

import java.util.Calendar;
import java.util.Hashtable;

import java.time.format.DateTimeFormatter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

public class SpeechToText {
    static boolean stopTranscribe;
    static Hashtable<String, String> eventData;

    public static void mainC(String[] args) {
        String temp = "set event summary today is a good day " +
                      "set email list charlie y u at gmail.com " +
                      "set start date today " + 
                      "set end date today " +
                      "set start time 12:05 p.m. " +
                      "set end time 3 p.m." +
                      "set location waterloo";
                      
        // System.out.println(returnEventData(temp).toString());


        try {
            // streamingMicRecognize();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Hashtable<String, String> returnEventData(String data){
        Hashtable<String, String> eventData = new Hashtable<String, String>();

        String[] parsedData = data.split("set");

        for (String currData : parsedData){
            String currText = currData.trim();
            if (currText.startsWith("event summary")){
                currText = currText.substring(14, currText.length());
                eventData.put("eventSummary", currText);
            }else if (currText.startsWith("email list")){
                currText = currText.substring(11, currText.length());
                currText = formatEmailList(currText);
                eventData.put("emailList", currText);
            }else if (currText.startsWith("start date")){
                currText = currText.substring(11, currText.length());
                try {
                    currText = formatDate(currText.trim());
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                eventData.put("startDate", currText);
            }else if (currText.startsWith("end date")){
                currText = currText.substring(9, currText.length());
                try {
                    currText = formatDate(currText.trim());
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                eventData.put("endDate", currText);
            }else if (currText.startsWith("start time")){
                currText = currText.substring(11, currText.length());
                currText = formatTime(currText);
                eventData.put("startTime", currText);
            }else if (currText.startsWith("end time")){
                currText = currText.substring(9, currText.length());
                currText = formatTime(currText);
                eventData.put("endTime", currText);
            }else if (currText.startsWith("location")){
                currText = currText.substring(9, currText.length());
                eventData.put("location", currText);
            }
        }
        return eventData;
    }



    public SpeechToText() {
        stopTranscribe = false;
        eventData = new Hashtable<String, String>();
    }


    private String formatTime(String timeStr){
        timeStr = timeStr.trim();
        if (timeStr.contains("p.m.")){
            // convert to 24h format
            int pos = timeStr.indexOf("p.m.");
            timeStr = timeStr.substring(0, pos);
            timeStr = timeStr.trim();
            if (timeStr.contains(":")){
                String[] timeArr = timeStr.split(":");
                if (timeArr.length > 1){
                    int hourStr = Integer.parseInt(timeArr[0]);
                    if (hourStr < 12){
                        hourStr += 12;
                    }
                    return hourStr + ":" + timeArr[1];
                }
            }else{
                int hourStr = Integer.parseInt(timeStr);
                if (hourStr < 12){
                    hourStr += 12;
                }
                return hourStr + ":00";
            }
        }else if (timeStr.contains("a.m.")){
            int pos = timeStr.indexOf("a.m.");
            timeStr = timeStr.substring(0, pos);
            timeStr = timeStr.trim();
            if (timeStr.contains(":")){
                return timeStr;
            }else{
                return timeStr + ":00";
            }
        }
        return timeStr;
    }


    private String formatEmailList(String emailStr) {
        emailStr = emailStr.toLowerCase();
        emailStr = emailStr.replaceAll("\\s+", "");
        emailStr = emailStr.replace("at", "@");
        return emailStr;
    }

    private String formatDate(String dateStr) throws ParseException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        System.out.println("formatDate|" + dateStr + "|");
        if (dateStr.equals("today")) {
            dateStr = dtf.format(now);
        } else if (dateStr.equals("tomorrow")) {
            now = now.plusDays(1);
            dateStr = dtf.format(now);
        } else if (dateStr.equals("day after tomorrow")) {
            now = now.plusDays(2);
            dateStr = dtf.format(now);
        } else {
            // try to parse date phrase
            String month = "";
            String day = "";
            String year = "";
            String[] arr = dateStr.split(" ");
            int i = 0;
            for (String currStr : arr) {
                if (i == 0) {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM");
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(inputFormat.parse(currStr));
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MM"); // 01-12
                    month = outputFormat.format(cal.getTime());
                } else if (i == 1) {
                    day = currStr.replaceAll("[^0-9]", "");
                } else if (i == 2) {
                    year = currStr;
                }
                i++;
            }
            dateStr = year + "-" + month + "-" + day;
        }
        return dateStr;
    }

    public String asyncRecognizeGcs(String gcsUri) throws Exception {
        String returnString = "";
        // Configure polling algorithm
        SpeechSettings.Builder speechSettings = SpeechSettings.newBuilder();
        TimedRetryAlgorithm timedRetryAlgorithm = OperationTimedPollAlgorithm.create(
                RetrySettings.newBuilder()
                        .setInitialRetryDelay(Duration.ofMillis(500L))
                        .setRetryDelayMultiplier(1.5)
                        .setMaxRetryDelay(Duration.ofMillis(5000L))
                        .setInitialRpcTimeout(Duration.ZERO) // ignored
                        .setRpcTimeoutMultiplier(1.0) // ignored
                        .setMaxRpcTimeout(Duration.ZERO) // ignored
                        .setTotalTimeout(Duration.ofHours(24L)) // set polling timeout to 24 hours
                        .build());
        speechSettings.longRunningRecognizeOperationSettings().setPollingAlgorithm(timedRetryAlgorithm);

        // Instantiates a client with GOOGLE_APPLICATION_CREDENTIALS
        try (SpeechClient speech = SpeechClient.create(speechSettings.build())) {

            // Configure remote file request for FLAC
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setLanguageCode("en-US")
                    .setSampleRateHertz(48000)
                    .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder().setUri(gcsUri).build();

            // Use non-blocking call for getting file transcription
            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response = speech
                    .longRunningRecognizeAsync(config, audio);
            while (!response.isDone()) {
                System.out.println("Waiting for response...");
                Thread.sleep(10000);
            }

            List<SpeechRecognitionResult> results = response.get().getResultsList();

            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech.
                // Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                returnString += alternative.getTranscript() + " ";
                System.out.printf("Transcription: %s\n", alternative.getTranscript());
            }
        }
        return returnString;
    }

    /**
     * Performs microphone streaming speech recognition with a duration of 1 minute.
     */
    public String streamingMicRecognize() throws Exception {

        ResponseObserver<StreamingRecognizeResponse> responseObserver = null;
        try (SpeechClient client = SpeechClient.create()) {

            responseObserver = new ResponseObserver<StreamingRecognizeResponse>() {
                ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

                public void onStart(StreamController controller) {
                }

                public void onResponse(StreamingRecognizeResponse response) {
                    StreamingRecognitionResult result = response.getResultsList().get(0);
                    SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                    if (alternative.getTranscript().contains("stop")) {
                        stopTranscribe = true;
                    } else if (alternative.getTranscript().trim().startsWith("set event summary")) {
                        String currText = alternative.getTranscript();
                        currText = currText.substring(18, currText.length());
                        eventData.put("eventSummary", currText);
                        System.out.printf("During Transcript event summary : %s\n", currText);
                    } else if (alternative.getTranscript().trim().startsWith("set email list")) {
                        String currText = alternative.getTranscript();
                        currText = currText.substring(15, currText.length());
                        currText = formatEmailList(currText);
                        eventData.put("emailList", currText);
                        System.out.printf("During Transcript email list : %s\n", currText);
                    } else if (alternative.getTranscript().trim().startsWith("set date")) {
                        String currText = alternative.getTranscript();
                        currText = currText.substring(9, currText.length());
                        try {
                            currText = formatDate(currText.trim());
                        } catch (ParseException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        eventData.put("date", currText);
                        System.out.printf("During Transcript date : %s\n", currText);
                    }
                    System.out.printf("During Transcript : %s\n", alternative.getTranscript());
                    responses.add(response);
                }

                public void onComplete() {
                    for (StreamingRecognizeResponse response : responses) {
                        StreamingRecognitionResult result = response.getResultsList().get(0);
                        SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                        System.out.printf("Transcript : %s\n", alternative.getTranscript());
                    }
                }

                public void onError(Throwable t) {
                    System.out.println(t);
                }
            };

            ClientStream<StreamingRecognizeRequest> clientStream = client.streamingRecognizeCallable()
                    .splitCall(responseObserver);

            RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setLanguageCode("en-US")
                    .setSampleRateHertz(16000)
                    .build();
            StreamingRecognitionConfig streamingRecognitionConfig = StreamingRecognitionConfig.newBuilder()
                    .setConfig(recognitionConfig).build();

            StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder()
                    .setStreamingConfig(streamingRecognitionConfig)
                    .build(); // The first request in a streaming call has to be a config

            clientStream.send(request);
            // SampleRate:16000Hz, SampleSizeInBits: 16, Number of channels: 1, Signed:
            // true,
            // bigEndian: false
            AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info targetInfo = new Info(
                    TargetDataLine.class,
                    audioFormat); // Set the system information to read from the microphone audio stream

            if (!AudioSystem.isLineSupported(targetInfo)) {
                System.out.println("Microphone not supported");
                return "Microphone not supported";
            }
            // Target data line captures the audio stream the microphone produces.
            TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
            targetDataLine.open(audioFormat);
            targetDataLine.start();
            System.out.println("Start speaking");
            long startTime = System.currentTimeMillis();
            // Audio Input Stream
            AudioInputStream audio = new AudioInputStream(targetDataLine);
            stopTranscribe = false;
            eventData = new Hashtable<String, String>();
            while (true) {
                long estimatedTime = System.currentTimeMillis() - startTime;
                byte[] data = new byte[6400];
                audio.read(data);
                if (estimatedTime > 60000 || stopTranscribe) { // 60 seconds
                    if (stopTranscribe)
                        System.out.println("Stopped early");
                    System.out.println("Stop speaking.");
                    targetDataLine.stop();
                    targetDataLine.close();
                    break;
                }
                request = StreamingRecognizeRequest.newBuilder()
                        .setAudioContent(ByteString.copyFrom(data))
                        .build();
                clientStream.send(request);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        responseObserver.onComplete();

        return eventData.toString();
    }
}
