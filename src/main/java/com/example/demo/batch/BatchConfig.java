package com.example.demo.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

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
    public Step load(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("loadStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    logger.info("Loading data...");
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
