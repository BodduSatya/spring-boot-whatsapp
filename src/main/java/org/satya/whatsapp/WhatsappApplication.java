package org.satya.whatsapp;

import jakarta.annotation.PostConstruct;
import org.satya.whatsapp.config.FileStorageProperties;
import org.satya.whatsapp.modal.MessageDTO;
import org.satya.whatsapp.modal.ResponseMessage;
import org.satya.whatsapp.service.MessageService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.ApplicationArguments;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@RestController
@EnableConfigurationProperties({
        FileStorageProperties.class
})
//@EnableScheduling
public class WhatsappApplication {

    private final ApplicationArguments applicationArguments;
    private final ScheduledExecutorService taskExecutor;
    private final MessageService messageService;

    public WhatsappApplication(ApplicationArguments applicationArguments, ScheduledExecutorService taskExecutor,
                               MessageService messageService) {
        this.applicationArguments = applicationArguments;
        this.taskExecutor = taskExecutor;
        this.messageService = messageService;
    }

    public static void main(String[] args) {
        SpringApplication.run(WhatsappApplication.class, args);
    }




    /*
     * One time job : after server startup process queued messages if any.
     * */

    @PostConstruct
    public void sendQueuedMessages(){
        try{
            if (applicationArguments.containsOption("sqm")) {
                System.out.println("****************************** Scheduled Task sendQueuedMessages() ");
                scheduleOneTimeTask(2 * 60);
            }
        }catch (Exception e){
            System.out.println("e = " + e);
        }
    }

    public void scheduleOneTimeTask(long delayInSeconds) {

        if (delayInSeconds < 0) {
            throw new IllegalArgumentException("Delay must be non-negative");
        }

        ScheduledFuture<?> scheduledFuture = taskExecutor.schedule(this::performTask, delayInSeconds, TimeUnit.SECONDS);

        // Optionally, you can cancel the task if needed
        // scheduledFuture.cancel(false);
        System.out.printf("*** One-time task scheduled to run after %s seconds\n", delayInSeconds);
    }

    private void performTask( ) {
        System.out.println("One-time task executing... ");

        try {
            String currentDate = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDate.now());
            List<MessageDTO> queuedMessages = messageService.getAllMessagesBetweenDates(currentDate, currentDate, null, "2");

            long successCount = 0;
            long failureCount = 0;

            if (!queuedMessages.isEmpty()) {
                System.out.printf("Queued Messages Count: %s \n", queuedMessages.size());

                ResponseMessage responseMessage = null;
                for (MessageDTO m : queuedMessages) {
                    try {
                        responseMessage = messageService.sendMessageV3(m);
                        if (responseMessage.getStatus() == HttpStatus.OK) {
                            successCount++;
                        } else {
                            failureCount++;
                        }

                        if ( failureCount == 2 ){
                            System.out.println("Failed count reached the configured limit,so stop processing the queued messages");
                            break;
                        }

                        // Add a delay to avoid hitting rate limits or overloading the server
                        Thread.sleep(Duration.ofSeconds(10));

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Restore interrupted status
                        System.out.printf("Task execution interrupted: %s", e.getMessage());
                    } catch (Exception e) {
                        System.out.printf("Error sending message: %s", e.getMessage());
                    }
                }

                System.out.printf("\nTotal messages: %s", queuedMessages.size());
                System.out.printf("\nSuccess count: %s", successCount);
                System.out.printf("\nFailure count: %s", failureCount+"\n");

            } else {
                System.out.println("No queued messages to process.");
            }
        } catch (Exception e){
            System.out.println("e = " + e.getMessage());
        }
    }

}
