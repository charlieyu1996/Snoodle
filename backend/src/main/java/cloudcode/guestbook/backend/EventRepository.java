package cloudcode.guestbook.backend;

import com.google.cloud.spring.data.spanner.repository.SpannerRepository;
//import org.springframework.cloud.gcp.data.spanner.repository.*;  this import also works
import org.springframework.stereotype.*;

@Repository
public interface EventRepository extends SpannerRepository<CalendarEntry, String> {
    long deleteByCreateDate (String createDate);
}