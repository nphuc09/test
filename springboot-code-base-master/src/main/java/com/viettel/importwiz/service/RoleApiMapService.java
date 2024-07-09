package com.viettel.importwiz.service;

import com.viettel.importwiz.dto.roleApiMap.CreateRoleApiMapDto;
import com.viettel.importwiz.dto.roleApiMap.UpdateRoleApiMapDto;
import com.viettel.importwiz.entity.RoleApiMap;
import com.viettel.importwiz.exception.custom.RecordNotFoundException;
import com.viettel.importwiz.repository.RoleApiMapRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.viettel.importwiz.constant.error.ErrorCodes.ROLE_API_MAP_NOT_EXIST;


@Service
@AllArgsConstructor
public class RoleApiMapService {

    private final RoleApiMapRepository roleApiMapRepository;

    private final ModelMapper mapper;

    public Page<RoleApiMap> findAllPaging(Pageable pageable, String keyword) {
        return this.roleApiMapRepository.findAllApiPaging(keyword, pageable);
    }

    public RoleApiMap createRoleApiMap(CreateRoleApiMapDto createRoleApiMap) {

        RoleApiMap roleApiMap = mapper.map(createRoleApiMap, RoleApiMap.class);

        return this.roleApiMapRepository.save(roleApiMap);
    }

    public RoleApiMap updateRoleApiMap(Long roleApiMapId, UpdateRoleApiMapDto updateRoleApiMapDto) {
        RoleApiMap roleApiMap = this.roleApiMapRepository.findByRoleApiMapId(roleApiMapId);

        if (roleApiMap == null) {
            throw new RecordNotFoundException(ROLE_API_MAP_NOT_EXIST);
        }

        roleApiMap.setRole(updateRoleApiMapDto.getRole());
        roleApiMap.setApi(updateRoleApiMapDto.getApi());
        roleApiMap.setMethod(updateRoleApiMapDto.getMethod().name());

        this.roleApiMapRepository.save(roleApiMap);

        return roleApiMap;
    }

    public void deleteRoleApiMap(Long id) {
        RoleApiMap roleApiMap = this.roleApiMapRepository.findByRoleApiMapId(id);

        if (roleApiMap == null) {
            throw new RecordNotFoundException(ROLE_API_MAP_NOT_EXIST);
        }
        this.roleApiMapRepository.deleteRoleApiMapById(id);
    }
}
