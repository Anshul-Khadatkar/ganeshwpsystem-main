package com.example.ganpativargani.whatsapp;

import com.example.ganpativargani.entity.MessageTemplate;

import java.util.List;

public interface MetaTemplateClient {

    MetaTemplateSyncResult submit(MessageTemplate template);

    MetaTemplateSyncResult fetchStatus(String templateName, String languageCode);

    /** Lists templates registered in Meta (optionally approved only). */
    List<MetaRemoteTemplate> listFromMeta(boolean approvedOnly);

    /** Fetches one template by Meta name + language, if it exists. */
    java.util.Optional<MetaRemoteTemplate> findRemote(String templateName, String languageCode);

    /** Deletes a template from Meta by name (all languages for that name). */
    void deleteByName(String templateName);
}
