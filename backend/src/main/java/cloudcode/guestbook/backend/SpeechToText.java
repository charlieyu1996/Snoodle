package cloudcode.guestbook.backend;

import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.longrunning.OperationTimedPollAlgorithm;
import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.retrying.TimedRetryAlgorithm;
import com.google.cloud.speech.v1.LongRunningRecognizeMetadata;
import com.google.cloud.speech.v1.LongRunningRecognizeResponse;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.cloud.speech.v1.SpeechSettings;
import java.util.List;
import org.threeten.bp.Duration;
import java.util.Calendar;
import java.util.Hashtable;
import java.time.format.DateTimeFormatter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;


public class SpeechToText {

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
                    e.printStackTrace();
                }
                eventData.put("startDate", currText);
            }else if (currText.startsWith("end date")){
                currText = currText.substring(9, currText.length());
                try {
                    currText = formatDate(currText.trim());
                } catch (ParseException e) {
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
            }else if (currText.startsWith("event details")){
                currText = currText.substring(14, currText.length());
                eventData.put("eventDetails", currText);
            }
        }
        return eventData;
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
}
