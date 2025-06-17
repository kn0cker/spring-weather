package com.example.demo.batch;

import com.example.demo.service.CsvFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class JobLuncherConfig {

    private final CsvFileService csvFileService;
    private static final Logger logger = LoggerFactory.getLogger(JobLuncherConfig.class);
    private final JobLauncher jobLauncher;
    private final Job fetchCsvChunkJob;

    public JobLuncherConfig(JobLauncher jobLauncher,
                             Job fetchCsvChunkJob,
                             CsvFileService csvFileService) {
        this.jobLauncher      = jobLauncher;
        this.fetchCsvChunkJob = fetchCsvChunkJob;
        this.csvFileService   = csvFileService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void launchJobsWhenReady() {
        logger.info("Application started – launching batch jobs asynchronously…");

        var paths = csvFileService.getCsvFilePaths();
        int chunkSize = 3;
        int totalJobs = (int) Math.ceil((double) paths.size() / chunkSize);

        for (int i = 0; i < totalJobs; i++) {
            int start = i * chunkSize;
            int end   = Math.min(start + chunkSize, paths.size());

            JobParameters params = new JobParametersBuilder()
                    .addString("path", String.join(",", paths.subList(start, end)))
                    .addLong("time", System.currentTimeMillis())   // unique instance
                    .toJobParameters();

            try {
                logger.info("→ Launching job for Paths {}-{}", start, end - 1);
                jobLauncher.run(fetchCsvChunkJob, params);        // returns immediately
            } catch (Exception ex) {
                logger.error("✗ Failed to launch job {}: {}", i, ex.getMessage(), ex);
            }
        }
    }


    @Bean
    public TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor tpe = new ThreadPoolTaskExecutor();
        tpe.setThreadNamePrefix("batch-");
        tpe.setCorePoolSize(4);
        tpe.setMaxPoolSize(10);
        tpe.initialize();
        return tpe;
    }

    @Bean
    public JobLauncher asyncJobLauncher(JobRepository repo,
                                        TaskExecutor batchTaskExecutor) throws Exception {
        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher(); // <—
        launcher.setJobRepository(repo);
        launcher.setTaskExecutor(batchTaskExecutor);  // makes run() non-blocking
        launcher.afterPropertiesSet();
        return launcher;
    }
}
