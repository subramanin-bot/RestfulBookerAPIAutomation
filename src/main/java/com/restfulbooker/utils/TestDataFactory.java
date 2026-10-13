package com.restfulbooker.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restfulbooker.models.BookingDates;
import com.restfulbooker.models.BookingRequest;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates random but valid booking data. Names carry a random suffix so a
 * booking can be found again through the name filters without clashing with
 * bookings created by other people on the shared public API.
 */
public final class TestDataFactory {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final List<String> FIRST_NAMES = List.of("Alice", "Bruno", "Chitra", "Dev", "Elena", "Farid", "Grace", "Hiro");
    private static final List<String> LAST_NAMES = List.of("Brown", "Iyer", "Novak", "Okafor", "Silva", "Tanaka", "Walker", "Zhang");
    private static final List<String> EXTRAS = List.of("Breakfast", "Late checkout", "Airport pickup", "Extra bed", "Sea view");

    private TestDataFactory() {
    }

    public static BookingRequest validBooking() {
        LocalDate checkin = LocalDate.now().plusDays(randomBetween(1, 30));
        LocalDate checkout = checkin.plusDays(randomBetween(1, 14));
        return new BookingRequest(
                randomFirstName(),
                randomLastName(),
                randomPrice(),
                ThreadLocalRandom.current().nextBoolean(),
                new BookingDates(checkin.toString(), checkout.toString()),
                pick(EXTRAS));
    }

    /** First name plus a random suffix, e.g. "Alice7k2q". */
    public static String randomFirstName() {
        return pick(FIRST_NAMES) + randomSuffix(4);
    }

    /** Last name plus a random suffix, e.g. "Brown9x1m4zq8". Long enough to be unique on the shared API. */
    public static String randomLastName() {
        return pick(LAST_NAMES) + randomSuffix(8);
    }

    public static int randomPrice() {
        return randomBetween(100, 2000);
    }

    /** Converts a booking to a mutable map so tests can remove or alter individual fields. */
    public static Map<String, Object> toMap(BookingRequest booking) {
        return MAPPER.convertValue(booking, new TypeReference<LinkedHashMap<String, Object>>() {
        });
    }

    private static int randomBetween(int minInclusive, int maxInclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    private static String pick(List<String> values) {
        return values.get(ThreadLocalRandom.current().nextInt(values.size()));
    }

    private static String randomSuffix(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(ALPHANUMERIC.charAt(ThreadLocalRandom.current().nextInt(ALPHANUMERIC.length())));
        }
        return builder.toString();
    }
}
