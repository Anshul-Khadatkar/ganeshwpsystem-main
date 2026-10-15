package com.example.ganpativargani.repository;

import com.example.ganpativargani.entity.BroadcastRecipient;
import com.example.ganpativargani.entity.WhatsAppStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BroadcastRecipientRepository extends JpaRepository<BroadcastRecipient, Long> {

    List<BroadcastRecipient> findByBroadcast_IdOrderByIdAsc(Long broadcastId);

    Page<BroadcastRecipient> findByBroadcast_IdOrderByIdAsc(Long broadcastId, Pageable pageable);

    List<BroadcastRecipient> findByBroadcast_IdAndStatus(Long broadcastId, WhatsAppStatus status);

    java.util.Optional<BroadcastRecipient> findByProviderMessageId(String providerMessageId);
}
