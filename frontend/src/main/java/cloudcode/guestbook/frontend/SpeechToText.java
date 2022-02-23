// package cloudcode.guestbook.frontend;

// import java.io.IOException;

// import javax.servlet.ServletException;
// import javax.servlet.http.HttpServlet;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import com.google.api.gax.rpc.ClientStream;
// import com.google.api.gax.rpc.ResponseObserver;
// import com.google.api.gax.rpc.StreamController;
// import com.google.cloud.speech.v1.RecognitionConfig;
// import com.google.cloud.speech.v1.SpeechClient;
// import com.google.cloud.speech.v1.SpeechContext;
// import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
// import com.google.cloud.speech.v1.StreamingRecognitionConfig;
// import com.google.cloud.speech.v1.StreamingRecognitionResult;
// import com.google.cloud.speech.v1.StreamingRecognizeRequest;
// import com.google.cloud.speech.v1.StreamingRecognizeResponse;
// import com.google.protobuf.ByteString;
// import java.util.ArrayList;
// import java.util.Calendar;
// import java.util.Date;
// import java.util.Hashtable;
// import java.util.Locale;

// import javax.sound.sampled.AudioFormat;
// import javax.sound.sampled.AudioInputStream;
// import javax.sound.sampled.AudioSystem;
// import javax.sound.sampled.DataLine;
// import javax.sound.sampled.DataLine.Info;
// import javax.sound.sampled.TargetDataLine;
// import java.time.format.DateTimeFormatter;
// import java.text.ParseException;
// import java.text.SimpleDateFormat;
// import java.time.LocalDateTime;   

// public class SpeechToText extends HttpServlet{
//     static boolean stopTranscribe = false;
//     static Hashtable<String, String> eventData = new Hashtable<String, String>();

//     public SpeechToText(){
//         super();
//     }

//     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//         doPost(request,response);
//     }

//     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//         try {
//             streamingMicRecognize();
//         } catch (Exception e) {
//             // TODO Auto-generated catch block
//             e.printStackTrace();
//         }

//     }
    





//     private String formatEmailList(String emailStr) {
//         emailStr = emailStr.toLowerCase();
//         emailStr = emailStr.replaceAll("\\s+","");
//         emailStr = emailStr.replace("at", "@");
//         return emailStr;
//     }

//     private String formatDate(String dateStr) throws ParseException{
//         DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
//         LocalDateTime now = LocalDateTime.now();
//         System.out.println("formatDate|" + dateStr + "|");
//         if (dateStr.equals("today")){
//             dateStr = dtf.format(now);
//         }else if (dateStr.equals("tomorrow")){
//             now = now.plusDays(1);
//             dateStr = dtf.format(now);
//         }else if (dateStr.equals("day after tomorrow")){
//             now = now.plusDays(2);
//             dateStr = dtf.format(now);
//         }else{
//             // try to parse date phrase
//             String month = "";
//             String day = "";
//             String year = "";
//             String[] arr = dateStr.split(" ");
//             int i = 0;
//             for (String currStr : arr){
//                 if (i == 0){
//                     SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM");
//                     Calendar cal = Calendar.getInstance();
//                     cal.setTime(inputFormat.parse(currStr));
//                     SimpleDateFormat outputFormat = new SimpleDateFormat("MM"); // 01-12
//                     month = outputFormat.format(cal.getTime());
//                 }else if (i == 1){
//                     day = currStr.replaceAll("[^0-9]", "");
//                 }else if (i == 2){
//                     year = currStr;
//                 }
//                 i++;
//             }
//             dateStr = year + "-" + month + "-" + day;
//         }
//         return dateStr;
//     }

//     /**
//      * Performs microphone streaming speech recognition with a duration of 1 minute.
//      */
//     public void streamingMicRecognize() throws Exception {

//         ResponseObserver<StreamingRecognizeResponse> responseObserver = null;
//         try (SpeechClient client = SpeechClient.create()) {

//             responseObserver = new ResponseObserver<StreamingRecognizeResponse>() {
//                 ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

//                 public void onStart(StreamController controller) {
//                 }

//                 public void onResponse(StreamingRecognizeResponse response) {
//                     StreamingRecognitionResult result = response.getResultsList().get(0);
//                     SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
//                     if (alternative.getTranscript().contains("stop")) {
//                         stopTranscribe = true;
//                     } else if (alternative.getTranscript().trim().startsWith("set event summary")) {
//                         String currText = alternative.getTranscript();
//                         currText = currText.substring(18, currText.length());
//                         eventData.put("eventSummary", currText);
//                         System.out.printf("During Transcript event summary : %s\n", currText);
//                     } else if (alternative.getTranscript().trim().startsWith("set email list")) {
//                         String currText = alternative.getTranscript();
//                         currText = currText.substring(15, currText.length());
//                         currText = formatEmailList(currText);
//                         eventData.put("emailList", currText);
//                         System.out.printf("During Transcript email list : %s\n", currText);
//                     } else if (alternative.getTranscript().trim().startsWith("set date")) {
//                         String currText = alternative.getTranscript();
//                         currText = currText.substring(9, currText.length());
//                         try {
//                             currText = formatDate(currText.trim());
//                         } catch (ParseException e) {
//                             // TODO Auto-generated catch block
//                             e.printStackTrace();
//                         }
//                         eventData.put("date", currText);
//                         System.out.printf("During Transcript date : %s\n", currText);
//                     }
//                     System.out.printf("During Transcript : %s\n", alternative.getTranscript());
//                     responses.add(response);
//                 }

//                 public void onComplete() {
//                     for (StreamingRecognizeResponse response : responses) {
//                         StreamingRecognitionResult result = response.getResultsList().get(0);
//                         SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
//                         System.out.printf("Transcript : %s\n", alternative.getTranscript());
//                     }
//                 }

//                 public void onError(Throwable t) {
//                     System.out.println(t);
//                 }
//             };

//             ClientStream<StreamingRecognizeRequest> clientStream = client.streamingRecognizeCallable()
//                     .splitCall(responseObserver);


//             RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
//                     .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
//                     .setLanguageCode("en-US")
//                     .setSampleRateHertz(16000)
//                     .build();
//             StreamingRecognitionConfig streamingRecognitionConfig = StreamingRecognitionConfig.newBuilder()
//                     .setConfig(recognitionConfig).build();

//             StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder()
//                     .setStreamingConfig(streamingRecognitionConfig)
//                     .build(); // The first request in a streaming call has to be a config

//             clientStream.send(request);
//             // SampleRate:16000Hz, SampleSizeInBits: 16, Number of channels: 1, Signed:
//             // true,
//             // bigEndian: false
//             AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
//             DataLine.Info targetInfo = new Info(
//                     TargetDataLine.class,
//                     audioFormat); // Set the system information to read from the microphone audio stream

//             if (!AudioSystem.isLineSupported(targetInfo)) {
//                 System.out.println("Microphone not supported");
//                 System.exit(0);
//             }
//             // Target data line captures the audio stream the microphone produces.
//             TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
//             targetDataLine.open(audioFormat);
//             targetDataLine.start();
//             System.out.println("Start speaking");
//             long startTime = System.currentTimeMillis();
//             // Audio Input Stream
//             AudioInputStream audio = new AudioInputStream(targetDataLine);
//             stopTranscribe = false;
//             eventData = new Hashtable<String, String>();
//             while (true) {
//                 long estimatedTime = System.currentTimeMillis() - startTime;
//                 byte[] data = new byte[6400];
//                 audio.read(data);
//                 if (estimatedTime > 60000 || stopTranscribe) { // 60 seconds
//                     if (stopTranscribe)
//                         System.out.println("Stopped early");
//                     System.out.println("Stop speaking.");
//                     targetDataLine.stop();
//                     targetDataLine.close();
//                     break;
//                 }
//                 request = StreamingRecognizeRequest.newBuilder()
//                         .setAudioContent(ByteString.copyFrom(data))
//                         .build();
//                 clientStream.send(request);
//             }
//         } catch (Exception e) {
//             System.out.println(e);
//         }
//         responseObserver.onComplete();
//     }
// }