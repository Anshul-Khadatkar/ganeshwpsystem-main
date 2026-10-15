package com.example.ganpativargani.whatsapp;

import com.example.ganpativargani.entity.Contributor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ContributorMessageVariables {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy");

    private ContributorMessageVariables() {
    }

    public static Map<String, String> build(Contributor contributor, String organizationName) {
        Map<String, String> vars = new LinkedHashMap<>();
        vars.put("name", contributor.getName());
        vars.put("mobile", contributor.getMobileNumber());
        vars.put("building", contributor.getBuilding().getName());
        vars.put("floor", contributor.getFloorNumber());
        vars.put("flat", contributor.getFlatNumber());
        vars.put("amount", null);
        vars.put("date", LocalDate.now().format(DATE_FORMAT));
        vars.put("organizationName", organizationName);
        return vars;
    }
}
