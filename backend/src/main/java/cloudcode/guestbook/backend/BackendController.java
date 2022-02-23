package cloudcode.guestbook.backend;

import org.springframework.web.bind.annotation.RestController;

import io.grpc.InternalChannelz.ChannelTrace.Event;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.Part;

/**
 * defines the REST endpoints managed by the server.
 */
@RestController
public class BackendController {

   @Autowired private MessageRepository repository;

   @Autowired private EventRepository eventRepository;

    BackendController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping("/messages2")
    public List<CalendarEntry> getEvents() {
      return (List<CalendarEntry>) eventRepository.findAll();
    }

    @PostMapping("/messages2")
    public String createEvent(@RequestBody CalendarEntry event) {
            // Spanner currently does not auto generate IDs
            // Generate UUID on new orders
            event.setId(UUID.randomUUID().toString());
            event.setCreateDate(Long.toString(System.currentTimeMillis()));
      CalendarEntry saved = eventRepository.save(event);
      return saved.getId();
    }

    /**
     * endpoint for retrieving all guest book entries stored in database
     * @return a list of GuestBookEntry objects
     */
    @GetMapping("/messages")
    public final List<CalendarEntry3> getMessages() {
        Sort byCreation = Sort.by(Sort.Direction.DESC, "_id");
        List<CalendarEntry3> msgList = repository.findAll(byCreation);
        return msgList;
    }

    /**
     * endpoint for adding a new guest book entry to the database
     * @param message a message object passed in the HTTP POST request
     */
    @PostMapping("/messages")
    public final void addMessage(@RequestBody CalendarEntry3 message) {
        message.setCreateDate(System.currentTimeMillis());
        repository.save(message);
    }

    // post that saves voice to cloud storage and uses speech to text to return a hash
    @PostMapping("/speech")
    public final Hashtable<String, String> setSpeechFile(@RequestBody byte[] audio){
        Hashtable<String, String> eventData = new Hashtable<String, String> ();
        try {
            CloudStorage cs = new CloudStorage();
            cs.uploadObject("charliyu-demo", "snoodle-voice", "webVoice.flac", audio);
            SpeechToText stt = new SpeechToText();
            String eventDataStr = stt.asyncRecognizeGcs("gs://snoodle-voice/webVoice.flac");
            eventData = stt.returnEventData(eventDataStr);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return eventData;
    }


    @GetMapping("/speech")
    public final Hashtable<String, String> getSpeechString( ){
        SpeechToText stt = new SpeechToText();
        Hashtable<String, String> eventData = new Hashtable<String, String> ();
        try {
            // transcribeString = stt.streamingMicRecognize();
            //transcribeString = stt.asyncRecognizeGcs("gs://snoodle-voice/snoodle-test.flac");


            String temp = "set event summary today is a good day " +
            "set email list charlie y u at gmail.com " +
            "set start date today " + 
            "set end date today " +
            "set start time 12:05 p.m. " +
            "set end time 3 p.m." +
            "set location waterloo";
            
            eventData = stt.returnEventData(temp);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return eventData;
    }
}
