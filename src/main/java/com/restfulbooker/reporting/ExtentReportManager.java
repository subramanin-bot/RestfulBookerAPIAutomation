package com.restfulbooker.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.restfulbooker.constants.FrameworkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Owns the single ExtentReports instance and the current test's log.
 * It also keeps pass/fail/skip counters and writes a plain-text summary next to the HTML report.
 */
public final class ExtentReportManager {

    private static final Logger LOG = LoggerFactory.getLogger(ExtentReportManager.class);
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();
    private static final AtomicInteger PASSED = new AtomicInteger();
    private static final AtomicInteger FAILED = new AtomicInteger();
    private static final AtomicInteger SKIPPED = new AtomicInteger();

    private static ExtentReports extent;

    private ExtentReportManager() {
    }

    /** Creates the report once; later calls are ignored. */
    public static synchronized void initReports() {
        if (extent != null) {
            return;
        }
        ExtentSparkReporter spark = new ExtentSparkReporter(FrameworkConstants.REPORT_FILE);
        spark.config().setDocumentTitle("Restful Booker API Automation");
        spark.config().setReportName("Restful Booker - REST Assured Test Execution");
        spark.config().setTheme(Theme.STANDARD);
        spark.config().setTimeStampFormat("dd MMM yyyy HH:mm:ss");

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Java", System.getProperty("java.version"));
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Application", "Restful Booker");
        extent.setSystemInfo("Started", LocalDateTime.now().toString());
        LOG.info("Extent report will be written to {}", FrameworkConstants.REPORT_FILE);
    }

    public static synchronized ExtentTest startTest(String name, String description) {
        initReports();
        ExtentTest test = extent.createTest(name, description);
        CURRENT_TEST.set(test);
        return test;
    }

    /** Returns the running test's report node, or null when called outside a test method. */
    public static ExtentTest getTest() {
        return CURRENT_TEST.get();
    }

    public static void info(String message) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) {
            test.log(Status.INFO, message);
        }
    }

    /** Adds a code block (already sanitised by the caller) to the current test. */
    public static void infoBlock(String title, String content) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) {
            test.log(Status.INFO, title);
            test.info(MarkupHelper.createCodeBlock(content));
        }
    }

    public static void pass(String message) {
        PASSED.incrementAndGet();
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) {
            test.log(Status.PASS, message);
        }
    }

    public static void fail(Throwable throwable) {
        FAILED.incrementAndGet();
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) {
            if (throwable != null) {
                test.fail(throwable);
            } else {
                test.log(Status.FAIL, "Test failed");
            }
        }
    }

    public static void skip(Throwable throwable) {
        SKIPPED.incrementAndGet();
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) {
            test.log(Status.SKIP, throwable == null ? "Test skipped" : throwable.toString());
        }
    }

    /** Writes the HTML report and the text summary. Safe to call more than once. */
    public static synchronized void flush() {
        if (extent == null) {
            return;
        }
        extent.flush();
        writeSummary();
    }

    private static void writeSummary() {
        int passed = PASSED.get();
        int failed = FAILED.get();
        int skipped = SKIPPED.get();
        String summary = "Restful Booker API Automation - execution summary" + System.lineSeparator()
                + "Finished : " + LocalDateTime.now() + System.lineSeparator()
                + "Total    : " + (passed + failed + skipped) + System.lineSeparator()
                + "Passed   : " + passed + System.lineSeparator()
                + "Failed   : " + failed + System.lineSeparator()
                + "Skipped  : " + skipped + System.lineSeparator()
                + "Report   : " + FrameworkConstants.REPORT_FILE + System.lineSeparator();
        try {
            Path summaryPath = Paths.get(FrameworkConstants.SUMMARY_FILE);
            Files.createDirectories(summaryPath.getParent());
            Files.writeString(summaryPath, summary, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.warn("Could not write the execution summary: {}", e.getMessage());
        }
        LOG.info("Execution summary -> passed={}, failed={}, skipped={}", passed, failed, skipped);
    }
}
