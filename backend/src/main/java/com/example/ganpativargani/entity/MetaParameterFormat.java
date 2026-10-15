package com.example.ganpativargani.entity;

/**
 * Matches the "Parameter Format" setting chosen when the template was
 * created in Meta's dashboard - visible on the template's edit screen in
 * WhatsApp Manager. Must match exactly, or Meta rejects the send with a
 * parameter-mismatch error.
 */
public enum MetaParameterFormat {
    NAMED,
    POSITIONAL
}
