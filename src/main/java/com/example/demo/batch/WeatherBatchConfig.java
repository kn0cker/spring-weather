package com.example.demo.batch;

import com.example.demo.service.CsvFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@EnableBatchProcessing
public class WeatherBatchConfig {

    private static final Logger logger = LoggerFactory.getLogger(WeatherBatchConfig.class);

    @Bean
    public Job fetchCsvChunkJob(JobRepository jobRepository, Step processCsvChunkStep) {
        return new JobBuilder("fetchCsvChunkJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(processCsvChunkStep)
                .build();
    }

    @Bean
    public Step processCsvChunkStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("processCsvChunkStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    JobParameters params = chunkContext.getStepContext().getStepExecution().getJobParameters();
                    String urls = params.getString("urls");

                    if (urls != null) {
                        List<String> urlList = List.of(urls.split(","));
                        urlList.forEach(url -> logger.info("Processing CSV: {}", url.trim()));
                    } else {
                        logger.warn("No URLs found in job parameters.");
                    }

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
