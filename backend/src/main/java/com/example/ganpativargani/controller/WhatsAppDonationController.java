package com.example.ganpativargani.controller;

import com.example.ganpativargani.dto.DonationDto;
import com.example.ganpativargani.dto.whatsapp.DonationSendRequest;
import com.example.ganpativargani.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/whatsapp/donations")
@RequiredArgsConstructor
public class WhatsAppDonationController {

    private final WhatsAppService whatsAppService;

    @PostMapping("/{donationId}/send")
    public DonationDto send(@PathVariable Long donationId, @RequestBody(required = false) DonationSendRequest request) {
        return whatsAppService.sendDonationThankYou(donationId, request);
    }

    @PostMapping("/{donationId}/retry")
    public DonationDto retry(@PathVariable Long donationId) {
        return whatsAppService.retryDonationSend(donationId);
    }
}
