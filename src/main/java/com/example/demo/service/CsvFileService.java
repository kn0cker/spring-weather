package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class CsvFileService {

    private static final Logger logger = LoggerFactory.getLogger(CsvFileService.class);

    private static final String BASE_URL = "https://www.ncei.noaa.gov/data/global-historical-climatology-network-daily/access/";
    private final RestTemplate restTemplate = new RestTemplate();

    public List<String> getCsvFileUrls() {
        List<String> urls = new ArrayList<>();

        logger.info("Fetching CSV file URLs from {}", BASE_URL);

        try {
            String html = restTemplate.getForObject(BASE_URL, String.class);
            if (html != null) {
                String[] lines = html.split("\n");
                for (String line : lines) {
                    if (line.contains("<a href=\"") && line.contains(".csv")) {
                        int start = line.indexOf("<a href=\"") + 9;
                        int end = line.indexOf("\"", start);
                        if (start > 0 && end > start) {
                            String fileName = line.substring(start, end);
                            if (fileName.endsWith(".csv")) {
                                String fullUrl = BASE_URL + "/" + fileName;
                                urls.add(fullUrl);
                                logger.debug("Found CSV: {}", fullUrl);
                            }
                        }
                    }
                }
            } else {
                logger.warn("Received empty response from {}", BASE_URL);
            }
        } catch (Exception e) {
            logger.error("Error fetching or parsing data from {}: {}", BASE_URL, e.getMessage(), e);
        }

        logger.info("Total CSV files found: {}", urls.size());

        return urls;
    }
}
