package ynu.pet.service;

import ynu.pet.dto.PetDTO;
import ynu.pet.dto.Result;
import java.util.List;
//import entity.*;

public interface PetService {
    Result<Void> createPet(PetDTO dto, Long ownerId);
    Result<PetDTO> getPetById(Long id);
    Result<List<PetDTO>> getUserPets(Long ownerId);
    Result<PetDTO> getPetByChipNumber(String chipNumber);
    Result<Void> updatePet(PetDTO dto);
    Result<Void> deletePet(Long id, Long ownerId);
}