package cl.duoc.ms_barriodigital_report.config;

import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic reportDltTopic() {
        return TopicBuilder.name("report.DLT")
                .partitions(3)
                .config("cleanup.policy", "delete")
                .config("retention.ms", "1209600000")
                .build();
    }
}
