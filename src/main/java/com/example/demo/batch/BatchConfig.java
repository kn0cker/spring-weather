package com.example.demo.batch;

import com.example.demo.entity.Measurement;
import com.example.demo.entity.Station;
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
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private static final Logger logger = LoggerFactory.getLogger(BatchConfig.class);
    private static final String CTX_KEY_CSV_STRING = "csvString";

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

                    JobParameters jobParameters =
                            contribution.getStepExecution().getJobParameters();

                    ExecutionContext ctx    = contribution.getStepExecution()
                            .getJobExecution()
                            .getExecutionContext();

                    List<String> paths = jobParameters.getString("paths").isEmpty()
                            ? List.of() : List.of(jobParameters.getString("paths").split(","));

                    logger.info("Loading data from URLs: {}", paths);
                    if (paths.isEmpty()) {
                        logger.warn("No URLs provided for loading data.");
                        return RepeatStatus.FINISHED;
                    }

                    List<String> csvData = new ArrayList<>();
                    for (String url : paths) {
                        String csv = csvFileService.streamCsvFile(url)
                                .collect(Collectors.joining("\n"));
                        csvData.add(csv);
                    }

                    // Put the list into the Job-wide ExecutionContext
                    ctx.put(CTX_KEY_CSV_STRING, csvData);
                    logger.debug("Stored {} CSV file(s) in execution context.", csvData.size());

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step validate(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("validateStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    ExecutionContext ctx = contribution.getStepExecution()
                            .getJobExecution()
                            .getExecutionContext();

                    @SuppressWarnings("unchecked")
                    List<String> csvData =
                            (List<String>) ctx.get(CTX_KEY_CSV_STRING);

                    if (csvData == null || csvData.isEmpty()) {
                        logger.warn("No CSV data found – skipping validation.");
                        return RepeatStatus.FINISHED;
                    }

                    logger.info("Validating {} CSV file(s)...", csvData.size());

                    ArrayList<Measurement> validatedMeasurements = new ArrayList<Measurement>();
                    HashSet<Station> validatedStations = new HashSet<Station>();

                    for (String csvFileString : csvData) //csvDateienAlsStrings
                    {
                        for (String csvFileRow : csvFileString.split("\n")) //rows
                        {
                            String[] csvRowSplit = csvFileRow.split(",");

                            try {
                                Station station = new Station();
                                station.setStationId(csvRowSplit[0]);
                                station.setName(csvRowSplit[5]);
                                station.setLatitude(Double.parseDouble(csvRowSplit[2]));
                                station.setLatitude(Double.parseDouble(csvRowSplit[3]));
                                station.setElevation(Double.parseDouble(csvRowSplit[4]));

                                if (!validatedStations.add(station))
                                    station = validatedStations.


                               // Measurement measurement = new Measurement();
//measurement.setStation(station);


                                validatedStations.add(station);
                            } catch (Exception e) //station fehler
                            {
                                logger.error("Zeile konnte nicht gelesen werden, Zeile: " + csvFileRow);
                            }
                        }
                    }


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
