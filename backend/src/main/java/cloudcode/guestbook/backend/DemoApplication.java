package cloudcode.guestbook.backend;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@SpringBootApplication
public class DemoApplication {
        public static void mainC(String[] args) {
                SpringApplication.run(DemoApplication.class, args);
        }
}

@RestController
class OrderController {
  private final OrderRepository orderRepository;

        OrderController(OrderRepository orderRepository) {
                this.orderRepository = orderRepository;
        }

        @GetMapping("/api/orders/{id}")
        public Order getOrder(@PathVariable String id) {
          return orderRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, id + " not found"));
        }

        @PostMapping("/api/orders")
        public String createOrder(@RequestBody Order order) {
                // Spanner currently does not auto generate IDs
                // Generate UUID on new orders
                order.setId(UUID.randomUUID().toString());
                order.setTimestamp(LocalDateTime.now());

                order.getItems().forEach(item -> {
                        // Assign parent ID, and also generate child ID
                        item.setOrderId(order.getId());
                        item.setOrderItemId(UUID.randomUUID().toString());
                });

          Order saved = orderRepository.save(order);
          return saved.getId();
        }
}