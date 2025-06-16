package com.example.demo.batch;

import com.example.demo.service.CsvFileService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class JobLuncherConfig {

    private final CsvFileService csvFileService;

    public JobLuncherConfig(CsvFileService csvFileService) {
        this.csvFileService = csvFileService;
    }

    @Bean
    public CommandLineRunner runJob(JobLauncher jobLauncher, Job simpleJob) {
        List<String> allCsvUrls = csvFileService.getCsvFileUrls();
        System.out.println("CSV Urls: " + allCsvUrls.size());
        return args -> {
            JobParameters parameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis()) // Unique ID
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(simpleJob, parameters);
            System.out.println("Job Status: " + execution.getStatus());
        };
    }
}
