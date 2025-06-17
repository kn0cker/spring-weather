package com.example.demo.batch;

import com.example.demo.service.CsvFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupJobRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupJobRunner.class);

    private final JobLauncher jobLauncher;
    private final Job fetchCsvChunkJob;
    private final CsvFileService csvFileService;

    public StartupJobRunner(JobLauncher jobLauncher,
                            Job fetchCsvChunkJob,
                            CsvFileService csvFileService) {
        this.jobLauncher      = jobLauncher;
        this.fetchCsvChunkJob = fetchCsvChunkJob;
        this.csvFileService   = csvFileService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void launchJobsWhenReady() {
        log.info("Application started – launching batch jobs asynchronously…");

        var paths = csvFileService.getCsvFilePaths();
        int chunkSize = 100;
        int totalJobs = (int) Math.ceil((double) paths.size() / chunkSize);

        for (int i = 0; i < totalJobs; i++) {
            int start = i * chunkSize;
            int end   = Math.min(start + chunkSize, paths.size());

            JobParameters params = new JobParametersBuilder()
                    .addString("paths", String.join(",", paths.subList(start, end)))
                    .addLong("time", System.currentTimeMillis())   // unique instance
                    .toJobParameters();

            try {
                log.info("→ Launching job for URLs {}-{}", start, end - 1);
                jobLauncher.run(fetchCsvChunkJob, params);         // returns immediately
            } catch (Exception ex) {
                log.error("✗ Failed to launch job {}: {}", i, ex.getMessage(), ex);
            }
        }
    }
}
