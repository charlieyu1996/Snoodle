package cloudcode.guestbook.backend;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import com.google.cloud.spring.data.spanner.core.mapping.Column;
import com.google.cloud.spring.data.spanner.core.mapping.Interleaved;
import com.google.cloud.spring.data.spanner.core.mapping.PrimaryKey;
import com.google.cloud.spring.data.spanner.core.mapping.Table;

@Table(name="orders")
@Data
public class Order {
  @PrimaryKey
  @Column(name="order_id")
  private String id;

  private String description;

  @Column(name="creation_timestamp")
  private LocalDateTime timestamp;

  @Interleaved
  private List<OrderItem> items;

public void setId(String id) {
    this.id = id;
}

public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
}

public List<OrderItem> getItems() {
    return items;
}

public String getId() {
    return id;
}
}

