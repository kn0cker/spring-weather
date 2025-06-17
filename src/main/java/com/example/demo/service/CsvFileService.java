package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvFileService {

    private static final Logger logger = LoggerFactory.getLogger(CsvFileService.class);

    private static final String BASE_URL = "https://www.ncei.noaa.gov/data/global-historical-climatology-network-daily/access/";
    //private final RestTemplate restTemplate = new RestTemplate();

    public List<String> getCsvFilePaths()
    {
        List<String> csvPaths = new ArrayList<>();

        //logger.info("Fetching CSV file URLs from {}", BASE_URL);
        logger.info("Fetching CSV file paths from main/resources/csv/");

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:csv/*.csv");

            for (Resource value : resources) {
                logger.info("Datei gefunden: " + value);
                csvPaths.add(value.getFile().getAbsolutePath());
            }
        } catch (IOException e)
        {
            logger.info("CSV Dateien nicht gefunden in main/resources/csv/");
        }

/*
        try {
            String html = restTemplate.getForObject(BASE_URL, String.class);
            if (html != null) {
                Pattern anchor = Pattern.compile("<a\\s+href=\"([^\"]+?\\.csv)\"", Pattern.CASE_INSENSITIVE);
                Matcher matcher = anchor.matcher(html);

                while (matcher.find()) {
                    String href = matcher.group(1);          // the value inside href="…"

                    String relative = href.startsWith(BASE_URL)
                            ? href.substring(BASE_URL.length())
                            : href;

                    if (relative.startsWith("/")) {
                        relative = relative.substring(1);
                    }

                    csvPaths.add(relative);
                    logger.debug("Found CSV path: {}", relative);
                }
            } else {
                logger.warn("Received empty response from {}", BASE_URL);
            }
        } catch (Exception e) {
            logger.error("Error fetching or parsing data from {}: {}", BASE_URL, e.getMessage(), e);
        }

        logger.info("Total CSV files found: {}", csvPaths.size());

        */

        return csvPaths;
    }
}
