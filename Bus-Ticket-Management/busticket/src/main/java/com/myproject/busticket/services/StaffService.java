package com.myproject.busticket.services;

import java.util.List;

import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.myproject.busticket.mapper.StaffMapper;
import com.myproject.busticket.models.Account;
import com.myproject.busticket.models.Staff;
import com.myproject.busticket.repositories.StaffRepository;

@Service
public class StaffService {
    @Autowired
    private StaffRepository staffRepository;
    StaffMapper staffMapper = Mappers.getMapper(StaffMapper.class);

    public List<Staff> findAll() {
        return staffRepository.findAll();
    }

    public Staff getStaffById(int staff_id) {
        return staffRepository.findById(staff_id).isPresent() ? staffRepository.findById(staff_id).get() : null;
    }

    public List<Staff> getStaffByAccount(Account account) {
        return staffRepository.findByAccount(account);
    }

    public Page<Staff> getAllStaffs(Pageable pageable) {
        return staffRepository.findAll(pageable);
    }

    public void deleteStaff(Staff staff) {
        staffRepository.delete(staff);
    }

    public Staff save(Staff staff) {
        return staffRepository.save(staff);
    }

    public Page<Staff> searchStaffsByNameOrEmail(Pageable pageable, String searchValue) {
        return staffRepository.findByAccountFullNameContainingOrAccountEmailContainingAllIgnoreCase(searchValue,
                searchValue, pageable);
    }
}
