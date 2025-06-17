package com.example.demo.batch;

import com.example.demo.entity.Measurement;
import com.example.demo.entity.Station;
import com.example.demo.repository.MeasurementRepository;
import com.example.demo.repository.StationRepository;
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
import org.springframework.transaction.PlatformTransactionManager;
import java.io.BufferedReader;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

                    List<String> urls = jobParameters.getString("urls").isEmpty()
                            ? List.of() : List.of(jobParameters.getString("urls").split(","));

                    logger.info("Loading data from URLs: {}", urls);
                    if (urls.isEmpty()) {
                        logger.warn("No URLs provided for loading data.");
                        return RepeatStatus.FINISHED;
                    }
                    List<String> csvData = new ArrayList<>();
                    for (String url : urls) {
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

                    List<Station> stations = new ArrayList<>();
                    List<Measurement> measurements = new ArrayList<>();

                    DateTimeFormatter dateAtMidnight = new DateTimeFormatterBuilder()
                            .appendPattern("yyyy-MM-dd")
                            .parseDefaulting(ChronoField.HOUR_OF_DAY,    0)
                            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                            .toFormatter();

                    for (String csv : csvData) {
                        if (csv.isBlank()) {
                            logger.warn("Found empty CSV data, skipping.");
                            continue;
                        }

                        try (BufferedReader br = new BufferedReader(new StringReader(csv))) {
                            br.lines()
                                    .skip(1)
                                    .forEach(line -> {
                                        String[] fields = line.split(",");
                                        if(stations.stream().noneMatch(station -> Objects.equals(station.getStationId(), fields[0].trim().replaceAll("\"", "")))) {
                                            try{
                                                Station station = new Station();
                                                station.setStationId(fields[0].trim().replaceAll("\"", ""));
                                                station.setName(fields[5].trim().replaceAll("\"", ""));
                                                station.setLatitude(Double.parseDouble(fields[2].trim().replaceAll("\"", "")));
                                                station.setLongitude(Double.parseDouble(fields[3].trim().replaceAll("\"", "")));
                                                station.setElevation(Double.parseDouble(fields[4].trim().replaceAll("\"", "")));
                                                station.setCreated(LocalDateTime.now());
                                                station.setUpdated(LocalDateTime.now());
                                                stations.add(station);
                                            }catch (Exception e){
                                                logger.debug("Failed to parse station data: {}", e.getMessage());
                                            }
                                        }

                                        try{
                                            Measurement measurement = new Measurement();
                                            measurement.setStation(stations.stream().filter(station -> Objects.equals(station.getStationId(), fields[0].trim().replaceAll("\"", ""))).findFirst().orElse(null));
                                            measurement.setDate(LocalDateTime.parse(fields[1].trim().replaceAll("\"", ""), dateAtMidnight));
                                            measurement.setMaxTemperature(parseDoubleOrZero(fields[12]));
                                            measurement.setMinTemperature(parseDoubleOrZero(fields[13]));
                                            measurement.setPrecipitation(parseDoubleOrZero(fields[6]));

                                            measurement.setCreated(LocalDateTime.now());
                                            measurement.setUpdated(LocalDateTime.now());
                                            measurements.add(measurement);
                                        }catch (Exception e){
                                            logger.debug("Failed to parse measurement data: {}", e.getMessage());
                                        }
                                    });
                        }
                    }

                    ctx.remove(CTX_KEY_CSV_STRING);
                    ctx.put("measurements", measurements);
                    ctx.put("stations", stations);

                    logger.info("Processed CSV data with {} stations and {} measurements.",
                            stations.size(), measurements.size());

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private static double parseDoubleOrZero(String raw) {
        String cleaned = raw.trim().replace("\"", "");
        if (cleaned.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    @Bean
    public Step persist(JobRepository jobRepository, PlatformTransactionManager transactionManager, StationRepository stationRepository, MeasurementRepository measurementRepository) {
        return new StepBuilder("persistStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    ExecutionContext ctx = contribution.getStepExecution()
                            .getJobExecution()
                            .getExecutionContext();

                    @SuppressWarnings("unchecked")
                    List<Measurement> measurements =
                            (List<Measurement>) ctx.get("measurements");
                    @SuppressWarnings("unchecked")
                    List<Station> stations =
                            (List<Station>) ctx.get("stations");

                    if (measurements == null || measurements.isEmpty()) {
                        logger.warn("No measurements found – skipping persistence.");
                        return RepeatStatus.FINISHED;
                    }

                    if (stations == null || stations.isEmpty()) {
                        logger.warn("No stations found – skipping persistence.");
                        return RepeatStatus.FINISHED;
                    }

                    logger.info("Persisting {} measurements and {} stations...",
                            measurements.size(), stations.size());

                    for (Station station : stations) {
                        if(!stationRepository.existsById(station.getStationId())) {
                            stationRepository.save(station);
                        }
                    }

                    measurementRepository.saveAll(measurements);

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
