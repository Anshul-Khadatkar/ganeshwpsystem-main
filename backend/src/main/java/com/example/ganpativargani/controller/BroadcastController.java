package com.example.ganpativargani.controller;

import com.example.ganpativargani.dto.PageResponse;
import com.example.ganpativargani.dto.whatsapp.BroadcastCreateRequest;
import com.example.ganpativargani.dto.whatsapp.BroadcastDetailDto;
import com.example.ganpativargani.dto.whatsapp.BroadcastDto;
import com.example.ganpativargani.dto.whatsapp.BroadcastPreviewResponse;
import com.example.ganpativargani.service.BroadcastService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whatsapp/broadcasts")
@RequiredArgsConstructor
public class BroadcastController {

    private final BroadcastService broadcastService;

    /** Called before create() so the admin sees "42 contributors" and a name sample prior to confirming. */
    @PostMapping("/preview")
    public BroadcastPreviewResponse preview(@RequestBody BroadcastCreateRequest request) {
        return broadcastService.preview(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BroadcastDto create(@Valid @RequestBody BroadcastCreateRequest request) {
        return broadcastService.create(request);
    }

    @GetMapping
    public PageResponse<BroadcastDto> history(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return broadcastService.history(page, Math.min(size, 100));
    }

    @GetMapping("/{id}")
    public BroadcastDetailDto getDetail(@PathVariable Long id,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "50") int size) {
        return broadcastService.getDetail(id, page, Math.min(size, 100));
    }

    @PostMapping("/{id}/retry-failed")
    public BroadcastDto retryFailed(@PathVariable Long id) {
        return broadcastService.retryFailed(id);
    }
}
