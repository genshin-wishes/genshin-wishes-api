package com.uf.genshinwishes.service;

import com.google.common.collect.Maps;
import com.uf.genshinwishes.dto.BannerImportStateDTO;
import com.uf.genshinwishes.dto.mapper.ImportingBannerStateMapper;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.ImportingBannerState;
import com.uf.genshinwishes.model.ImportingState;
import com.uf.genshinwishes.model.User;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ImportingStateService {
    private final Map<User, ImportingState> statesByUser = Maps.newConcurrentMap();

    @Autowired
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

        if (this.getByUser(user) != null) {
            try {
                this.deleteImportStateOf(user);
            } catch (ApiError e) {
                return null;
            }
        }

        ImportingState previousState = statesByUser.put(user, importState);

        if (previousState != null) {
            statesByUser.put(user, previousState);

            return null;
        }

        return bannerStates.stream()
            .collect(Collectors.toMap(ImportingBannerState::getGachaType, Function.identity()));
    }

    public void finish(ImportingBannerState bannerState) {
        bannerState.setFinished(true);

        updateState(bannerState);
    }

    public void markSaved(ImportingBannerState bannerState) {
        bannerState.setSaved(true);

        updateState(bannerState);
    }

    public void markError(ImportingBannerState bannerState, ApiError error) {
        bannerState.setError(error.getErrorType().name());
    }

    public void increment(ImportingBannerState bannerState, int increment) {
        if (bannerState.getCount() == -1)
            bannerState.setCount(increment);
        else
            bannerState.setCount(bannerState.getCount() + increment);

        updateState(bannerState);
    }

    public void deleteImportStateOf(User user) {
        ImportingState state = this.getByUser(user);

        if (state != null
            && (state.getBannerStates().stream().allMatch(s -> s.getSaved())
            || state.getBannerStates().stream().anyMatch(s -> s.getError() != null))) {
            state.setDeleted(true);
            this.statesByUser.remove(user);
        } else {
            throw new ApiError(ErrorType.ALREADY_IMPORTING);
        }
    }

    private ImportingState getByUser(User user) {
        return this.statesByUser.get(user);
    }

    private void updateState(ImportingBannerState bannerState) {
        bannerState.getImportingState().setLastModifiedDate(Instant.now());

        if (bannerState.getImportingState().getDeleted() != null && bannerState.getImportingState().getDeleted()) {
            throw new ApiError(ErrorType.IMPORT_ERROR);
        }
    }

    public void forceRemove(User user) {
        ImportingState importingState = statesByUser.get(user);

        if (importingState != null) {
            importingState.setDeleted(true);
        }

        statesByUser.remove(user);
    }
}
