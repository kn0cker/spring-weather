package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CsvFileService {

    private static final Logger logger = LoggerFactory.getLogger(CsvFileService.class);

    private static final String BASE_URL = "https://www.ncei.noaa.gov/data/global-historical-climatology-network-daily/access/";
    private final RestTemplate restTemplate = new RestTemplate();

    public List<String> getCsvFileUrls() {
        List<String> csvPaths = new ArrayList<>();

        logger.info("Fetching CSV file URLs from {}", BASE_URL);

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
        return csvPaths;
    }
}
