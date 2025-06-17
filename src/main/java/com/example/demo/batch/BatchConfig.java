package com.example.demo.batch;

import com.example.demo.service.CsvFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private static final Logger logger = LoggerFactory.getLogger(BatchConfig.class);

    @Bean
    public Job simpleJob(JobRepository repository, Step load, Step validate, Step persist) {
        return new JobBuilder("weatherJob", repository)
                .start(load)
                .next(validate)
                .next(persist)
                .build();
    }

    @Bean
    public Step load(JobRepository jobRepository, PlatformTransactionManager transactionManager, CsvFileService csvFileService) {
        return new StepBuilder("loadStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    //TODO: For each URL, fetch the CSV file and start Stream/download it

                    JobParameters jobParameters =
                            contribution.getStepExecution().getJobParameters();

                    List<String> urls = jobParameters.getString("urls").isEmpty()
                            ? List.of() : List.of(jobParameters.getString("urls").split(","));

                    logger.info("Loading data from URLs: {}", urls);
                    if (urls.isEmpty()) {
                        logger.warn("No URLs provided for loading data.");
                        return RepeatStatus.FINISHED;
                    }

                    for (String url : urls) {
                        byte[] bytes = csvFileService.streamCsvFile(url)
                                        .collect(Collectors.joining("\n"))
                                        .getBytes(StandardCharsets.UTF_8);

                        InputStreamResource resource = new InputStreamResource(
                                new ByteArrayResource(bytes)
                        );
                    }

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step validate(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("validateStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    logger.info("Validating data...");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step persist(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("persistStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    logger.info("Persisting data...");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
