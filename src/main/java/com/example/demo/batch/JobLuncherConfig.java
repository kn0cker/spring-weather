package com.example.demo.batch;

import com.example.demo.service.CsvFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.List;

@Configuration
public class JobLuncherConfig {

    private final CsvFileService csvFileService;
    private static final Logger logger = LoggerFactory.getLogger(JobLuncherConfig.class);
    private final JobLauncher jobLauncher;
    private final Job fetchCsvChunkJob;

    public JobLuncherConfig(JobLauncher jobLauncher, Job fetchCsvChunkJob, CsvFileService csvFileService) {
        this.jobLauncher = jobLauncher;
        this.fetchCsvChunkJob = fetchCsvChunkJob;
        this.csvFileService = csvFileService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void launchJobsWhenReady() {
        logger.info("Application started. Launching batch jobs...");

        List<String> allCsvUrls = csvFileService.getCsvFileUrls();
        int chunkSize = 10;
        int totalJobs = (int) Math.ceil((double) allCsvUrls.size() / chunkSize);

        for (int i = 0; i < totalJobs; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, allCsvUrls.size());
            List<String> chunk = allCsvUrls.subList(start, end);

            String urlsParam = String.join(",", chunk);
            JobParameters params = new JobParametersBuilder()
                    .addString("urls", urlsParam)
                    .addLong("time", System.currentTimeMillis())  // Ensure unique instance
                    .toJobParameters();

            try {
                logger.info("Launching job for URLs {} to {}", start, end - 1);
                jobLauncher.run(fetchCsvChunkJob, params);
            } catch (Exception e) {
                logger.error("Failed to launch job for chunk {}: {}", i, e.getMessage(), e);
            }
        }
    }
}
