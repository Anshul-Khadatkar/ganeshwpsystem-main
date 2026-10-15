package com.example.ganpativargani.controller;

import com.example.ganpativargani.dto.whatsapp.ImportMetaTemplateRequest;
import com.example.ganpativargani.dto.whatsapp.MessageTemplateDto;
import com.example.ganpativargani.dto.whatsapp.MessageTemplateRequest;
import com.example.ganpativargani.dto.whatsapp.MetaRemoteTemplateDto;
import com.example.ganpativargani.entity.MessageTemplateType;
import com.example.ganpativargani.service.MessageTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/whatsapp/templates")
@RequiredArgsConstructor
public class MessageTemplateController {

    private final MessageTemplateService templateService;

    @GetMapping
    public List<MessageTemplateDto> list(@RequestParam(required = false) MessageTemplateType type) {
        return templateService.list(type);
    }

    @GetMapping("/meta/approved")
    public List<MetaRemoteTemplateDto> listApprovedFromMeta() {
        return templateService.listApprovedFromMeta();
    }

    @PostMapping("/import-from-meta")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageTemplateDto importFromMeta(@Valid @RequestBody ImportMetaTemplateRequest request) {
        return templateService.importFromMeta(request);
    }

    @GetMapping("/{id}")
    public MessageTemplateDto getById(@PathVariable Long id) {
        return templateService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageTemplateDto create(@Valid @RequestBody MessageTemplateRequest request) {
        return templateService.create(request);
    }

    @PutMapping("/{id}")
    public MessageTemplateDto update(@PathVariable Long id, @Valid @RequestBody MessageTemplateRequest request) {
        return templateService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public MessageTemplateDto setStatus(@PathVariable Long id, @RequestParam boolean active) {
        return templateService.setActive(id, active);
    }

    @PostMapping("/{id}/submit-to-meta")
    public MessageTemplateDto submitToMeta(@PathVariable Long id) {
        return templateService.submitToMeta(id);
    }

    @PostMapping("/{id}/sync-meta-status")
    public MessageTemplateDto syncMetaStatus(@PathVariable Long id) {
        return templateService.syncMetaStatus(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        templateService.delete(id);
    }
}
