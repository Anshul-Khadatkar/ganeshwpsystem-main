package com.example.ganpativargani.repository;

import com.example.ganpativargani.entity.MessageTemplate;
import com.example.ganpativargani.entity.MessageTemplateType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Long> {

    List<MessageTemplate> findByDeletedFalseOrderByNameAsc();

    List<MessageTemplate> findByDeletedFalseAndTypeOrderByNameAsc(MessageTemplateType type);

    List<MessageTemplate> findByDeletedFalseAndActiveTrueAndTypeOrderByIdAsc(MessageTemplateType type);

    Optional<MessageTemplate> findByIdAndDeletedFalse(Long id);

    Optional<MessageTemplate> findFirstByDeletedFalseAndActiveTrueAndTypeOrderByIdAsc(MessageTemplateType type);

    Optional<MessageTemplate> findByDeletedFalseAndMetaTemplateNameAndMetaLanguageCode(
            String metaTemplateName, String metaLanguageCode);

    boolean existsByDeletedFalseAndMetaTemplateNameAndMetaLanguageCode(
            String metaTemplateName, String metaLanguageCode);
}
