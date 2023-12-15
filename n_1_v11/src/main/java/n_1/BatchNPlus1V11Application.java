package n_1;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableBatchProcessing
@SpringBootApplication
public class BatchNPlus1V11Application {

	public static void main(String[] args) {
		SpringApplication.run(BatchNPlus1V11Application.class, args);
	}
}
