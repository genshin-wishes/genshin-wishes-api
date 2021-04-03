package com.uf.genshinwishes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ImportingStateCleanerService {
    public final static int STATES_CLEANER_DELAY = 10 * 1000;

    @Autowired
    private ImportingStateService importingStateService;

    @Scheduled(fixedDelay = STATES_CLEANER_DELAY)
    public void cleaning() {
        this.importingStateService.removeOldStates();
    }
}
