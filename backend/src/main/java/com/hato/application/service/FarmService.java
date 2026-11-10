package com.hato.application.service;

import com.hato.domain.model.Farm;
import com.hato.domain.model.GeoPoint;
import com.hato.infrastructure.persistence.entity.FarmEntity;
import com.hato.infrastructure.persistence.repository.SpringDataFarmRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FarmService {

    private final SpringDataFarmRepository farmRepository;

    public FarmService(SpringDataFarmRepository farmRepository) {
        this.farmRepository = farmRepository;
    }

    @PostConstruct
    public void seedInitialFarms() {
        if (farmRepository.count() == 0) {
            FarmEntity f1 = new FarmEntity();
            f1.setId("farm-01");
            f1.setName("Hato Principal");
            f1.setLocation("San Carlos, Alajuela");
            f1.setTotalAreaHa(150.0f);
            f1.setOwnerName("Juan Propietario");
            f1.setCenterLat(10.4660);
            f1.setCenterLng(-84.6450);
            farmRepository.save(f1);

            FarmEntity f2 = new FarmEntity();
            f2.setId("farm-02");
            f2.setName("Lote Norte");
            f2.setLocation("Upala, Alajuela");
            f2.setTotalAreaHa(200.0f);
            f2.setOwnerName("Pedro Administrador");
            f2.setCenterLat(10.8000);
            f2.setCenterLng(-85.0000);
            farmRepository.save(f2);
        }
    }

    @Transactional(readOnly = true)
    public List<Farm> findAll() {
        return farmRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    public Optional<Farm> findById(String id) {
        return farmRepository.findById(id).map(this::toDomain);
    }

    private Farm toDomain(FarmEntity e) {
        return new Farm(
                e.getId(),
                e.getName(),
                e.getLocation(),
                e.getTotalAreaHa(),
                e.getOwnerName(),
                new GeoPoint(e.getCenterLat(), e.getCenterLng())
        );
    }
}
