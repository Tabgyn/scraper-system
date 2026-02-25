package com.scraper.processor.repository;

import com.scraper.common.enums.JobStatus;
import com.scraper.processor.document.ScrapeResultDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScrapeResultRepository extends MongoRepository<ScrapeResultDocument, String> {
    Optional<ScrapeResultDocument> findByJobId(UUID jobId);
    List<ScrapeResultDocument> findByStatus(JobStatus status);
    List<ScrapeResultDocument> findByUrl(String url);
}