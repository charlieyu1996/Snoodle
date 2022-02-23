package cloudcode.guestbook.backend;

import com.google.cloud.spring.data.spanner.repository.SpannerRepository;
import org.springframework.stereotype.*;

@Repository
public interface EventRepository extends SpannerRepository<CalendarEntry, String> {
}