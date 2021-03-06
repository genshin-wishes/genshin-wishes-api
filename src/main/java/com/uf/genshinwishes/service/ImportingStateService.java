package com.uf.genshinwishes.service;

import com.uf.genshinwishes.config.MihoyoRestTemplate;
import com.uf.genshinwishes.dto.BannerImportStateDTO;
import com.uf.genshinwishes.dto.mapper.ImportingBannerStateMapper;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.ImportingBannerState;
import com.uf.genshinwishes.model.ImportingState;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.repository.ImportingBannerStateRepository;
import com.uf.genshinwishes.repository.ImportingStateRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ImportingStateService {
    private MihoyoRestTemplate mihoyoRestTemplate;
    private ImportingBannerStateRepository importingBannerStateRepository;
    private ImportingStateRepository importingStateRepository;
    private ImportingBannerStateMapper bannerStateMapper;

    public Map<BannerType, BannerImportStateDTO> getImportingStateDtoFor(User user) {
        ImportingState state = getByUser(user);

        if (state == null) return null;

        return state.getBannerStates()
            .stream()
            .map(bannerStateMapper::toDto)
            .collect(Collectors.toMap(BannerImportStateDTO::getBannerType, Function.identity()));
    }

    @Transactional
    public Map<Integer, ImportingBannerState> initializeImport(User user) {
        Instant now = Instant.now();
        ImportingState importState = new ImportingState();

        importState.setUser(user);
        importState.setCreationDate(now);
        importState.setLastModifiedDate(now);

        List<ImportingBannerState> bannerStates = BannerType.getBannersExceptAll()
            .map(b -> {
                ImportingBannerState bannerState = new ImportingBannerState();

                bannerState.setImportingState(importState);
                bannerState.setGachaType(b.getType());
                bannerState.setCount(0);
                bannerState.setFinished(false);
                bannerState.setSaved(false);

                return bannerState;
            })
            .collect(Collectors.toList());

        importState.setBannerStates(bannerStates);

        if (importingStateRepository.findFirstByUserOrderByCreationDateAsc(user) != null) {
            return null;
        }

        importingStateRepository.save(importState);

        ImportingState mostRecentState = importingStateRepository.findFirstByUserOrderByCreationDateAsc(user);

        if(mostRecentState.getId() != importState.getId()) {
            importingStateRepository.delete(importState);

            return null;
        }

        return bannerStates.stream()
            .collect(Collectors.toMap(ImportingBannerState::getGachaType, Function.identity()));
    }

    public void finish(ImportingBannerState bannerState) {
        bannerState.setFinished(true);

        importingBannerStateRepository.save(bannerState);
    }

    public void markSaved(ImportingBannerState bannerState) {
        bannerState.setSaved(true);

        importingBannerStateRepository.save(bannerState);
    }

    public void markError(ImportingBannerState bannerState, ApiError error) {
        bannerState.setError(error.getErrorType().name());

        importingBannerStateRepository.save(bannerState);
    }

    public void increment(ImportingBannerState bannerState, int increment) {
        if (bannerState.getCount() == -1)
            bannerState.setCount(increment);
        else
            bannerState.setCount(bannerState.getCount() + increment);

        updateState(bannerState);
    }

    @Transactional
    public void deleteImportStateOf(User user) {
        ImportingState state = this.importingStateRepository.findFirstByUserOrderByCreationDateAsc(user);

        if (state != null
            && (state.getBannerStates().stream().allMatch(s -> s.getSaved())
            || state.getBannerStates().stream().anyMatch(s -> s.getError() != null))) {
            this.importingStateRepository.deleteAllByUser(user);
        } else {
            throw new ApiError(ErrorType.ALREADY_IMPORTING);
        }
    }

    @Transactional
    public void removeOldStates() {
        Instant beforeMillis = Instant.now().minus(mihoyoRestTemplate.getReadTimeout() + mihoyoRestTemplate.getConnectTimeout(), ChronoUnit.MILLIS);

        this.importingStateRepository.deleteAllByLastModifiedDateLessThan(beforeMillis);
    }

    private ImportingState getByUser(User user) {
        return this.importingStateRepository.findFirstByUserOrderByCreationDateAsc(user);
    }

    private void updateState(ImportingBannerState bannerState) {
        bannerState.getImportingState().setLastModifiedDate(Instant.now());

        importingStateRepository.save(bannerState.getImportingState());
    }
}
