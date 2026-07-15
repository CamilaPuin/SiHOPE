package edu.uptc.swii.sihope.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.uptc.swii.sihope.domain.Carrera;
import edu.uptc.swii.sihope.repository.CarreraRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

@Service
public class CarreraService {

    private final CarreraRepository carreraRepository;
    private final UserRepository userRepository;

    public CarreraService(CarreraRepository carreraRepository, UserRepository userRepository) {
        this.carreraRepository = carreraRepository;
        this.userRepository = userRepository;
    }

    public List<Carrera> listCatalog() {
        return carreraRepository.findAllByOrderByNameAsc();
    }

    @Transactional
    public List<String> createCareer(String name) {
        List<String> errors = new ArrayList<>();
        String clean = name == null ? "" : name.trim();
        if (clean.isEmpty()) {
            errors.add("El nombre de la carrera es obligatorio.");
            return errors;
        }
        if (carreraRepository.existsByNameIgnoreCase(clean)) {
            errors.add("Ya existe una carrera con ese nombre.");
            return errors;
        }
        carreraRepository.save(new Carrera(clean));
        return errors;
    }

    @Transactional
    public List<String> deleteCareer(Integer id) {
        List<String> errors = new ArrayList<>();
        Carrera carrera = carreraRepository.findById(id).orElse(null);
        if (carrera == null) {
            errors.add("La carrera no existe.");
            return errors;
        }
        if (userRepository.countByCareer_Id(id) > 0) {
            errors.add("No se puede eliminar: hay usuarios asociados a esta carrera.");
            return errors;
        }
        carreraRepository.delete(carrera);
        return errors;
    }
}
