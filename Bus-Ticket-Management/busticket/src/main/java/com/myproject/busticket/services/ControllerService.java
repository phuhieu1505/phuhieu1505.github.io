package com.myproject.busticket.services;

import java.util.List;

import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.myproject.busticket.mapper.ControllerMapper;
import com.myproject.busticket.models.Account;
import com.myproject.busticket.models.Controller;
import com.myproject.busticket.repositories.ControllerRepository;

@Service
public class ControllerService {

    @Autowired
    private ControllerRepository controllerRepository;

    ControllerMapper controllerMapper = Mappers.getMapper(ControllerMapper.class);

    public List<Controller> findAll() {
        return controllerRepository.findAll();
    }

    public Page<Controller> getAll(Pageable pageable) {
        return controllerRepository.findAll(pageable);
    }

    public Controller getControllerById(int id) {
        return controllerRepository.findById(id).get();
    }

    public List<Controller> getControllerByAccount(Account account) {
        return controllerRepository.findByAccount(account);
    }

    public void deleteController(Controller controller) {
        controllerRepository.delete(controller);
    }

    public Controller save(Controller controller) {
        return controllerRepository.save(controller);
    }

    public Page<Controller> searchControllersByNameOrEmail(Pageable pageable, String searchValue) {
        return controllerRepository.findByAccountFullNameContainingOrAccountEmailContainingAllIgnoreCase(searchValue,
                searchValue, pageable);
    }
}
