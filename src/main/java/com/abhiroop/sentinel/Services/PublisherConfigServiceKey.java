package com.abhiroop.sentinel.Services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PublisherConfigServiceKey {
    RECORD_COUNT("recordCount");

    private final String key;
}
