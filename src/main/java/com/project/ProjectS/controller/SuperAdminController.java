package com.project.ProjectS.controller;




import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.ProjectS.model.SuperAdminDTO;
import com.project.ProjectS.service.SuperAdminService;


@RestController
@RequestMapping("/api/super-admin")
public class SuperAdminController {

    @Autowired
    private SuperAdminService superAdminService;

  
    @PostMapping
    public ResponseEntity<String> save(@RequestBody SuperAdminDTO dto) {
    	 superAdminService.save(dto);
    	 return new ResponseEntity<>("User saved successfully", HttpStatus.CREATED);
    }

    
    @GetMapping
    public ResponseEntity<List<SuperAdminDTO>> findAll() {

        List<SuperAdminDTO> list = superAdminService.findAll();
        return ResponseEntity.ok(list);
    }


    @GetMapping("/{id}")
    public ResponseEntity<SuperAdminDTO> findById(@PathVariable Integer id) {

        SuperAdminDTO dto = superAdminService.findById(id);
        return ResponseEntity.ok(dto);
    }

  
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Integer id,
                                         @RequestBody SuperAdminDTO dto) {

        superAdminService.update(id, dto);
        return ResponseEntity.ok("User updated successfully");
    }

    // Delete Super Admin
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {

        superAdminService.delete(id);
        return ResponseEntity.ok("Super Admin deleted successfully.");
    }
}