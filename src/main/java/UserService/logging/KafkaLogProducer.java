package UserService.logging;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaLogProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaLogProducer.class); // Keep SLF4J logger
    private static KafkaLogProducer instance;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${logging.kafka.topic:user-service-logs}")
    private String topic;

    @PostConstruct
    private void initialize() {
        instance = this;
        log.info("KafkaLogProducer initialized. Logging to topic: {}", topic);
    }

    // Static method for the Logback appender to call
    public static void sendLog(String message) {
        System.out.println(
                ">>> KafkaLogProducer.sendLog CALLED with message: " + message.substring(0, Math.min(message.length(), 150)) + "..."
        ); // DEBUG LINE 1

        if (instance != null && instance.kafkaTemplate != null) {
            try {
                System.out.println(">>> KafkaLogProducer sending to topic '" + instance.topic + "'..."); // DEBUG LINE 2
                // Consider using sendDefault for simplicity if topic is always the same
                // instance.kafkaTemplate.sendDefault(message);
                instance.kafkaTemplate.send(instance.topic, message);
                System.out.println(">>> KafkaLogProducer send successful (async)"); // DEBUG LINE 3 (Note: send is async)
            } catch (Exception e) {
                // Log error to console if Kafka sending fails
                System.err.println("!!! ERROR sending log message to Kafka topic '" + instance.topic + "': " + e.getMessage());
                e.printStackTrace(); // Print the full stack trace to stderr
            }
        } else {
            // Log to console if Kafka producer isn't ready yet
            System.err.println(
                    "!!! KafkaLogProducer not ready. Instance is " + (instance == null ? "null" : "NOT null") +
                    ", KafkaTemplate is " + (instance != null ? "null" : "NOT null") +
                    ". Log message NOT sent to Kafka."
            );
        }
    }

    public static String getTopic() {
        return (instance != null) ? instance.topic : "kafka-topic-not-set";
    }
}