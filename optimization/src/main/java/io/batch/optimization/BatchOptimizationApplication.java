package io.batch.optimization;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableBatchProcessing
@SpringBootApplication
public class BatchOptimizationApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchOptimizationApplication.class, args);
	}
}
