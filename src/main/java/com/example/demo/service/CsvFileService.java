package com.example.demo.service;

import org.springframework.core.io.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

    public Stream<String> streamCsvFile(String relativePath) {

        Assert.hasText(relativePath, "The relative path has to be defined");

        String url = BASE_URL + (relativePath.startsWith("/") ? relativePath.substring(1) : relativePath);
        logger.info("Streaming CSV file from URL: {}", url);

        try {
            ResponseEntity<Resource> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    Resource.class);

            Resource resource = response.getBody();
            if (resource == null) {
                logger.warn("No content found at URL: {}", url);
                return Stream.empty();
            }

            InputStream is = resource.getInputStream();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8));

            return reader.lines()
                    .onClose(() -> {
                        try {
                            reader.close();
                        } catch (Exception e) {
                            logger.error("Error closing reader: {}", e.getMessage(), e);
                        }
                    });
        }catch (Exception e) {
            logger.error("Error streaming CSV file from {}: {}", url, e.getMessage(), e);
            return Stream.empty();
        }
    }
}
