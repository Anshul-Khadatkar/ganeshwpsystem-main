package com.example.ganpativargani.whatsapp;

import com.example.ganpativargani.entity.Donation;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DonationMessageVariables {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy");

    private DonationMessageVariables() {
    }

    public static Map<String, String> build(Donation donation, String organizationName) {
        var contributor = donation.getContributor();
        Map<String, String> vars = new LinkedHashMap<>();
        vars.put("name", contributor.getName());
        vars.put("mobile", contributor.getMobileNumber());
        vars.put("building", contributor.getBuilding().getName());
        vars.put("floor", contributor.getFloorNumber()); // null handled by TemplateVariableResolver
        vars.put("flat", contributor.getFlatNumber());   // null handled by TemplateVariableResolver
        vars.put("amount", donation.getAmount().toPlainString());
        vars.put("date", donation.getDonationDate().format(DATE_FORMAT));
        vars.put("organizationName", organizationName);
        return vars;
    }
}
