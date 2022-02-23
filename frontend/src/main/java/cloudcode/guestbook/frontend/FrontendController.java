package cloudcode.guestbook.frontend;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.http.HttpHeaders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.catalina.core.ApplicationPart;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;


/**
 * defines the REST endpoints managed by the server.
 */
@Controller
public class FrontendController {
    private String backendUri = String.format("http://%s/messages2",
        System.getenv("GUESTBOOK_API_ADDR"));
    private String backendUri2 = String.format("http://%s/speech",
        System.getenv("GUESTBOOK_API_ADDR"));

    private String calendarURL = "https://calendar.google.com/calendar/render";


    /**
     * endpoint for the landing page
     * @param model defines model for html template
     * @return the name of the html template to render
     */
    @GetMapping("/Snoodle")
    public final String main(final Model model) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            CalendarEntry[] response = restTemplate.getForObject(backendUri,
            CalendarEntry[].class);
            model.addAttribute("messages", response);
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error retrieving messages from backend.");
            model.addAttribute("noBackend", true);
        }
        return "home";
    }

    @GetMapping("/Callback")
    public final String callback(final Model model) {
        return "callback";
    }

    /**
     * endpoint for handling form submission
     * @param formMessage holds date entered in html form
     * @return redirects back to home page
     * @throws URISyntaxException when there is an issue with the backend uri
     */
    @RequestMapping(value = "/post", method = RequestMethod.POST)
    public final String post(final CalendarEntry formMessage)
            throws URISyntaxException {
        URI url = new URI(backendUri);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", "application/json");

        formMessage.setEventLink(formatCalendarURL(formMessage.getEventSummary(), formMessage.getStartDate(), formMessage.getEndDate(), "test charlie", "location", formMessage.getEmailList()));
        HttpEntity<CalendarEntry> httpEntity =
            new HttpEntity<CalendarEntry>(formMessage, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.postForObject(url, httpEntity, String.class);
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error posting message to backend.");
        }

        return "redirect:/Snoodle";
    }


    public static byte[] toByteArray(InputStream in) throws IOException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
 
        byte[] buffer = new byte[16384];
        int len;
 
        // read bytes from the input stream and store them in the buffer
        while ((len = in.read(buffer)) != -1)
        {
            // write bytes from the buffer into the output stream
            os.write(buffer, 0, len);
        }
 
        return os.toByteArray();
    }


    // saves the voice to cloud storage
    @RequestMapping(value = "/postRecord", method = RequestMethod.POST)
    public final String postRecord(HttpServletRequest req, HttpServletResponse res)
            throws URISyntaxException {
        String returnURL = "redirect:/Snoodle?";
        URI url = new URI(backendUri2);

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Content-Type", "application/json");
            Part filePart = req.getPart("audio_data");
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
            InputStream fileContent = filePart.getInputStream();
            byte[] targetArray = toByteArray(fileContent);
            // byte[] targetArray = fileContent.readAllBytes();


            HttpEntity<byte[]> httpEntity = new HttpEntity<byte[]>(targetArray, httpHeaders);
            RestTemplate restTemplate = new RestTemplate();
            Hashtable<String, String> eventData = restTemplate.postForObject(url, httpEntity, Hashtable.class);
            Enumeration<String> e = eventData.keys();
            while (e.hasMoreElements()) { 
                String key = e.nextElement();
                String value = eventData.get(key);
                returnURL += key + "=" + value + "&";
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ServletException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error posting message to backend.");
        }
        return returnURL;
    }


    // Obtains a hash table to pre-populate the input fields based on speech to text
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public final String get(final String formMessage)
            throws URISyntaxException {
        String returnURL = "redirect:/Snoodle?";
        URI url = new URI(backendUri2);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", "application/json");

        RestTemplate restTemplate = new RestTemplate();
        try {
            Hashtable<String, String> eventData = restTemplate.getForObject(url, Hashtable.class);
            Enumeration<String> e = eventData.keys();

            while (e.hasMoreElements()) { 
                String key = e.nextElement();
                String value = eventData.get(key);
                returnURL += key + "=" + value + "&";
            }

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error posting message to backend.");
        }

        return returnURL;
    }



    // helper that formats the calendar link url
    private String formatCalendarURL(String text, String dateStart, String dateEnd, String details, String location, String add){
        String newCalendarURL = calendarURL+ "?";
        String action = "TEMPLATE";
        String dates = dateStart + "/" + dateEnd;
        newCalendarURL += "action=" + action + "&";
        newCalendarURL += "text=" + text + "&";
        newCalendarURL += "dates=" + "20201231T193000Z/20201231T223000Z" + "&";
        newCalendarURL += "details=" + details + "&";
        newCalendarURL += "location=" + location + "&";
        newCalendarURL += "add=" + add;
        return newCalendarURL;
    }

    

}
