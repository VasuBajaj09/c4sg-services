package org.c4sg.service.impl;

import org.c4sg.constant.Constants;

import static org.c4sg.constant.Directory.RESUME_UPLOAD;
import static org.c4sg.constant.Directory.AVATAR_UPLOAD;
import static org.c4sg.constant.Format.RESUME;
import static org.c4sg.constant.Format.IMAGE;
import org.c4sg.dao.UserDAO;
import org.c4sg.dto.UserDTO;
import org.c4sg.entity.User;
import org.c4sg.mapper.UserMapper;
import org.c4sg.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private UserMapper userMapper;

	@Override
	public List<UserDTO> findAll() {
		List<User> users = userDAO.findAllByOrderByIdDesc();
		return userMapper.getDtosFromEntities(users);
	}
	
	@Override
	public Page<UserDTO> findActiveVolunteers(Pageable pageable) {
		Page<User> users = userDAO.findActiveVolunteers(pageable);
		Page<UserDTO> userDTOS = users.map(p -> userMapper.getUserDtoFromEntity(p));
		return userDTOS;
	}

	@Override
	public UserDTO findById(int id) {
		return userMapper.getUserDtoFromEntity(userDAO.findById(id));
	}

	@Override
	public UserDTO findByEmail(String email) {
		return userMapper.getUserDtoFromEntity(userDAO.findByEmail(email));
	}

	@Override
	public UserDTO saveUser(UserDTO userDTO) {
		User user = userMapper.getUserEntityFromDto(userDTO);
		return userMapper.getUserDtoFromEntity(userDAO.save(user));
	}

    public void deleteUser(Integer id) {
        User user = userDAO.findById(id);
        user.setStatus(Constants.USER_STATUS_DELETED);
        user.setEmail(user.getEmail() + "-deleted");
        user.setAvatarUrl(null);
        userDAO.save(user);
        userDAO.deleteUserProjects(id);
        userDAO.deleteUserSkills(id);        
    }
		
	@Override
	public List<UserDTO> getApplicants(Integer projectId) {
		List<User> users = userDAO.findByUserProjectId(projectId, Constants.USER_PROJECT_STATUS_APPLIED);
		return userMapper.getDtosFromEntities(users);
	}

	@Override
	public List<UserDTO> search(String keyWord, List<Integer> skills) {
		long skillCount = 0;
		if (skills != null)
			skillCount = skills.size();
		List<User> users = userDAO.findByKeyword(keyWord, skills, skillCount);
		return mapUsersToUserDtos(users);
	}

	private List<UserDTO> mapUsersToUserDtos(List<User> users) {
		return users.stream().map(p -> userMapper.getUserDtoFromEntity(p)).collect(Collectors.toList());
	}

	@Override
	public String getAvatarUploadPath(Integer userId) {
		return AVATAR_UPLOAD.getValue() + File.separator + userId + IMAGE.getValue();
	}

	@Override
	public String getResumeUploadPath(Integer userId) {
		return RESUME_UPLOAD.getValue() + File.separator + userId + RESUME.getValue();
	}
}
