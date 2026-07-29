package com.project.ProjectS.service;

import com.project.ProjectS.entity.Branch;
import com.project.ProjectS.entity.College;
import com.project.ProjectS.model.BranchRequestDTO;
import com.project.ProjectS.model.BranchResponseDTO;
import com.project.ProjectS.repository.BranchRepository;
import com.project.ProjectS.repository.CollegeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;

@Service
public class BranchService {

    private static final Logger logger =
            LogManager.getLogger(BranchService.class);

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    public String create(BranchRequestDTO request) {

        logger.info("Creating branch with name: {}", request.getBranchName());

        if (branchRepository.existsByBranchName(request.getBranchName())) {
            logger.warn("Branch already exists with name: {}", request.getBranchName());
            throw new RuntimeException("Branch already exists");
        }

        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> {
                    logger.warn("College not found with ID: {}", request.getCollegeId());
                    return new RuntimeException("College not found");
                });

        Branch entity = new Branch();

        entity.setCollege(college);
        entity.setBranchName(request.getBranchName());
        entity.setAddress(request.getAddress());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setEmail(request.getEmail());

        branchRepository.save(entity);

        logger.info("Branch created successfully with name: {}", request.getBranchName());

        return "Branch created successfully";
    }

    public List<BranchResponseDTO> getAll() {

        logger.info("Fetching all branches.");

        List<BranchResponseDTO> branches = branchRepository.findAll()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());

        logger.info("Fetched {} branches.", branches.size());

        return branches;
    }

    public Branch getById(Long id) {

        logger.info("Fetching branch with ID: {}", id);

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Branch not found with ID: {}", id);
                    return new RuntimeException("Branch not found");
                });

        logger.info("Branch fetched successfully with ID: {}", id);

        return branch;
    }

    public String update(Long id, BranchRequestDTO request) {

        logger.info("Updating branch with ID: {}", id);

        Branch entity = branchRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Branch not found with ID: {}", id);
                    return new RuntimeException("Branch not found");
                });

        College college = collegeRepository.findById(request.getCollegeId())
                .orElseThrow(() -> {
                    logger.warn("College not found with ID: {}", request.getCollegeId());
                    return new RuntimeException("College not found");
                });

        entity.setCollege(college);
        entity.setBranchName(request.getBranchName());
        entity.setAddress(request.getAddress());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setEmail(request.getEmail());

        branchRepository.save(entity);

        logger.info("Branch updated successfully with ID: {}", id);

        return "Branch updated successfully";
    }

    public String delete(Long id) {

        logger.info("Deleting branch with ID: {}", id);

        Branch entity = branchRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Branch not found with ID: {}", id);
                    return new RuntimeException("Branch not found");
                });

        branchRepository.delete(entity);

        logger.info("Branch deleted successfully with ID: {}", id);

        return "Branch deleted successfully";
    }

    private BranchResponseDTO convert(Branch entity) {

        BranchResponseDTO dto = new BranchResponseDTO();

        dto.setBranchId(entity.getBranchId());

        dto.setCollegeId(entity.getCollege().getCollegeId());
        dto.setCollegeName(entity.getCollege().getInstituteName());

        dto.setBranchName(entity.getBranchName());
        dto.setAddress(entity.getAddress());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setEmail(entity.getEmail());

        dto.setActiveRow(entity.getActiveRow());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    public String uploadBranch(MultipartFile file) {


        logger.info("Starting branch Excel upload process");


        if(file.isEmpty()) {
            throw new RuntimeException("File cannot be empty");
        }


        int savedCount = 0;
        int skippedCount = 0;


        try(InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream)) {


            Sheet sheet = workbook.getSheetAt(0);


            boolean header = true;


            for(Row row : sheet) {


                if(header) {
                    header = false;
                    continue;
                }


                String collegeName =
                        row.getCell(0)
                                .getStringCellValue()
                                .trim();


                String branchName =
                        row.getCell(1)
                                .getStringCellValue()
                                .trim();


                String address =
                        row.getCell(2)
                                .getStringCellValue()
                                .trim();


                String phone =
                        row.getCell(3)
                                .toString()
                                .trim();


                String email =
                        row.getCell(4)
                                .getStringCellValue()
                                .trim();



                College college =
                        collegeRepository
                                .findByInstituteName(collegeName)
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "College not found : "
                                                        + collegeName)
                                );



                if(branchRepository
                        .findByBranchNameAndCollege(
                                branchName,
                                college)
                        .isPresent()) {


                    skippedCount++;

                    continue;
                }



                Branch branch = new Branch();


                branch.setCollege(college);
                branch.setBranchName(branchName);
                branch.setAddress(address);
                branch.setPhoneNumber(phone);
                branch.setEmail(email);


                branchRepository.save(branch);


                savedCount++;


                logger.info(
                        "Branch saved : {}",
                        branchName);

            }



            return "Branch Excel upload completed. Saved: "
                    + savedCount
                    + ", Skipped: "
                    + skippedCount;



        }
        catch(Exception e){

            logger.error(
                    "Branch Excel upload failed",
                    e);

            throw new RuntimeException(
                    "Failed to upload branch Excel");

        }

    }
}