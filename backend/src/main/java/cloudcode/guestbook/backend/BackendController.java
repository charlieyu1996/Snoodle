package cloudcode.guestbook.backend;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

/**
 * defines the REST endpoints managed by the server.
 */
@RestController
public class BackendController {

    @Autowired
    private MessageRepository repository;

    @Autowired
    private EventRepository eventRepository;

    BackendController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping("/messages2")
    public List<CalendarEntry> getEvents() {
        return (List<CalendarEntry>) eventRepository.findAll(Sort.by(Sort.Direction.DESC, "CreateDate"));
        // return (List<CalendarEntry>) eventRepository.findAll();
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

    @PostMapping("/deleteEvent")
    public String deleteEvent(@RequestBody String createDate) {
        try{
            eventRepository.deleteByCreateDate(createDate);
        }catch (Exception e){
            return "Deletion failed";
        }
        return "Event deleted";
    }

    /**
     * endpoint for retrieving all guest book entries stored in database
     * 
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
     * 
     * @param message a message object passed in the HTTP POST request
     */
    @PostMapping("/messages")
    public final void addMessage(@RequestBody CalendarEntry3 message) {
        message.setCreateDate(System.currentTimeMillis());
        repository.save(message);
    }

    // post that saves voice to cloud storage and uses speech to text to return a
    // hash
    @PostMapping("/speech")
    public final Hashtable<String, String> setSpeechFile(@RequestBody byte[] audio) {
        Hashtable<String, String> eventData = new Hashtable<String, String>();
        try {
            CloudStorage cs = new CloudStorage();
            cs.uploadObject("charliyu-demo", "snoodle-voice", "webVoice.flac", audio);
            SpeechToText stt = new SpeechToText();
            String eventDataStr = stt.asyncRecognizeGcs("gs://snoodle-voice/webVoice.flac");
            eventData = stt.returnEventData(eventDataStr);
        } catch (Exception e) {
            System.out.println("Something went wrong");
            e.printStackTrace();
        }
        return eventData;
    }
}
