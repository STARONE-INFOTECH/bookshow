package com.starone.bookshow.movie.service.impl;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.starone.bookshow.movie.dto.CastAndCrewDto;
import com.starone.bookshow.movie.entity.CastAndCrew;
import com.starone.bookshow.movie.exception.custom.InvalidInputException;
import com.starone.bookshow.movie.exception.custom.ResourceNotFoundException;
import com.starone.bookshow.movie.repository.ICastAndCrewRepository;
import com.starone.bookshow.movie.service.ICastAndCrewService;
import com.starone.bookshow.movie.util.MovieMapper;
import com.starone.bookshow.movie.util.MovieUtils;

public class CastAndCrewServiceImpl implements ICastAndCrewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CastAndCrewServiceImpl.class);
    private final ICastAndCrewRepository castRepository;
    private final MovieMapper mapper;

    public CastAndCrewServiceImpl(ICastAndCrewRepository castRepository, MovieMapper mapper) {
        this.castRepository = castRepository;
        this.mapper = mapper;
    }

    @Override
    public CastAndCrewDto addCast(CastAndCrewDto castDto) {
        if (MovieUtils.isNull(castDto)) {
            LOGGER.warn("Failed to save Cast! Cast must not be null");
            throw new InvalidInputException("Cast");
        }
        CastAndCrew savedCast = castRepository.save(mapper.mapToCastAndCrew(castDto));

        LOGGER.info("Cast saved successfully");
        return mapper.mapToCastAndCrewDto(savedCast);
    }

    @Override
    public CastAndCrewDto updateCast(String castId, CastAndCrewDto castDto) {
        if (MovieUtils.isNullOrEmpty(castId) || MovieUtils.isNull(castDto)) {
            LOGGER.warn("Failed to update Cast! CastId or Cast must not be null");
            throw new InvalidInputException("Cast or CastId");
        }
        CastAndCrew existingCast = castRepository.findById(UUID.fromString(castId)).orElseThrow(() -> {
            LOGGER.warn("Failed to get Cast! Cast not found.");
            return new ResourceNotFoundException("Cast", castId);
        });
        LOGGER.debug("CastDto mapping with existing Cast");
        mapper.mapToExistingCastAndCrew(castDto, existingCast);

        CastAndCrew updatedCast = castRepository.save(mapper.mapToCastAndCrew(castDto));

        LOGGER.info("Cast updated successfully.");
        return mapper.mapToCastAndCrewDto(updatedCast);
    }

    @Override
    public List<CastAndCrewDto> getAllCast() {
        return castRepository.findAll()
                .stream()
                .map(mapper::mapToCastAndCrewDto)
                .toList();
    }

    @Override
    public CastAndCrewDto getCastById(String castId) {
        if (MovieUtils.isNullOrEmpty(castId)) {
            LOGGER.warn("Failed to get Cast! CastId must not be null");
            throw new InvalidInputException("CastId");
        }
        CastAndCrew existingCast = castRepository.findById(UUID.fromString(castId)).orElseThrow(() -> {
            LOGGER.warn("Failed to get Cast! Cast not found.");
            return new ResourceNotFoundException("Cast", castId);
        });
        LOGGER.info("Cast fetched successfully!");
        return mapper.mapToCastAndCrewDto(existingCast);
    }

    @Override
    public void deleteCastById(String castId) {
        if (MovieUtils.isNullOrEmpty(castId)) {
            LOGGER.warn("Failed to delete Cast! CastId must not be null");
            throw new InvalidInputException("CastId");
        }
        castRepository.deleteById(UUID.fromString(castId));
        LOGGER.info("Cast deleted successfully!");
    }

}
