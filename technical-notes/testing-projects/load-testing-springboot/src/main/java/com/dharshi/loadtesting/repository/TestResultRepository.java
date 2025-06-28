package com.dharshi.loadtesting.repository;

import com.dharshi.loadtesting.model.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    
    List<TestResult> findByTestName(String testName);
    
    List<TestResult> findByTestNameAndTimestampBetween(String testName, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT AVG(t.responseTimeMs) FROM TestResult t WHERE t.testName = :testName AND t.success = true")
    Double getAverageResponseTime(@Param("testName") String testName);
    
    @Query("SELECT COUNT(t) FROM TestResult t WHERE t.testName = :testName AND t.success = true")
    Long getSuccessCount(@Param("testName") String testName);
    
    @Query("SELECT COUNT(t) FROM TestResult t WHERE t.testName = :testName AND t.success = false")
    Long getFailureCount(@Param("testName") String testName);
    
    @Query("SELECT MIN(t.responseTimeMs) FROM TestResult t WHERE t.testName = :testName AND t.success = true")
    Long getMinResponseTime(@Param("testName") String testName);
    
    @Query("SELECT MAX(t.responseTimeMs) FROM TestResult t WHERE t.testName = :testName AND t.success = true")
    Long getMaxResponseTime(@Param("testName") String testName);
    
    @Query("SELECT t FROM TestResult t WHERE t.testName = :testName ORDER BY t.timestamp DESC")
    List<TestResult> findLatestResults(@Param("testName") String testName);
} 