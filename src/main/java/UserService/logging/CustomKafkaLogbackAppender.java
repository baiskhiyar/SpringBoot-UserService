package UserService.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Setter
@Getter
public class CustomKafkaLogbackAppender extends AppenderBase<ILoggingEvent> {

    private Encoder<ILoggingEvent> encoder;

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!isStarted() || this.encoder == null) {
            return;
        }
        try {
            // Encode the event to a byte array
            byte[] encodedBytes = this.encoder.encode(eventObject);

            // Determine the charset - Use encoder's or fallback to UTF-8
            Charset charset = null;
            if (this.encoder instanceof LayoutWrappingEncoder) {
                Layout<?> layout = ((LayoutWrappingEncoder<ILoggingEvent>) this.encoder).getLayout();
                charset = ((LayoutWrappingEncoder<ILoggingEvent>) this.encoder).getCharset();

            } else {
                try {
                    // Attempt reflection or check specific encoder type if needed
                    // For simplicity, we'll just default if getCharset() isn't available or returns null after start()
                } catch (Exception e) { /* ignore potential NoSuchMethodError etc */ }
            }

            // Fallback to UTF-8 if charset couldn't be determined
            if (charset == null) {
                addWarn("Encoder's charset was null, falling back to UTF-8. Ensure encoder is configured and started correctly.");
                charset = StandardCharsets.UTF_8;
            }

            // Convert bytes to String using the determined charset
            String formattedMessage = new String(encodedBytes, charset);


            // Send the formatted message using our static producer method
            KafkaLogProducer.sendLog(formattedMessage);

        } catch (Exception e) {
            // Handle exceptions during formatting or sending
            System.err.println("Error logging event: " + eventObject.getFormattedMessage());
            addError("Error occurred while appending event to Kafka: " + eventObject, e);
        }
    }

    @Override
    public void start() {
        if (this.encoder == null) {
            addError("No encoder set for the Appender [" + name + "].");
            return; // Do not start if encoder is missing
        }
        // **** CRITICAL FIX ****
        // Ensure the encoder itself is started before using it.
        if (!this.encoder.isStarted()) {
            addInfo("Starting encoder for Appender [" + name + "].");
            this.encoder.start();
        }
        // Now call the superclass start method
        super.start();
    }

    @Override
    public void stop() {
        // Stop the encoder first if it's running
        if (this.encoder != null && this.encoder.isStarted()) {
            this.encoder.stop();
        }
        // Then stop the appender
        super.stop();
    }
}