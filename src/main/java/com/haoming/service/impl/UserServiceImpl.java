package com.haoming.service.impl;

import com.haoming.common.Const;
import com.haoming.common.ServerResponse;
import com.haoming.common.TokenCache;
import com.haoming.dao.UserMapper;
import com.haoming.pojo.User;
import com.haoming.service.IUserService;
import com.haoming.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int count = userMapper.checkUserName(username);
        if (count == 0) {
            return ServerResponse.createByErrorMessage("No such user");
        }

        String encodePassword = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, encodePassword);
        if (user == null) {
            return ServerResponse.createByErrorMessage("Wrong password");
        }
        user.setPassword(StringUtils.EMPTY);

        return ServerResponse.createBySuccess("Hello!", user);
    }

    public ServerResponse<String> register(User user) {
        ServerResponse<String> response = this.checkValid(user.getUsername(), Const.USERNAME);
        if (!response.isSuccess()) {
            return response;
        }
        response = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!response.isSuccess()) {
            return response;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int count = userMapper.insert(user);
        if (count == 0) {
            return ServerResponse.createByErrorMessage("Failed to register");
        }
        return ServerResponse.createBySuccess("Success!");
    }

    public ServerResponse<String> checkValid(String str, String type) {
        if (!StringUtils.isBlank(type)) {
            if (Const.USERNAME.equals(type)) {
                int count = userMapper.checkUserName(str);
                if (count > 0) {
                    return ServerResponse.createByErrorMessage("Username has existed");
                }
            }
            else if (Const.EMAIL.equals(type)) {
                int count = userMapper.checkEmail(str);
                if (count > 0) {
                    return ServerResponse.createByErrorMessage("Email has existed");
                }
            }
            else {
                return ServerResponse.createByErrorMessage("Wrong parameter");
            }
        }
        return ServerResponse.createBySuccess("Validation successfully!");
    }

    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse<String> response = checkValid(username, Const.USERNAME);
        if (response.isSuccess()) {
            return ServerResponse.createByErrorMessage("Username does not exist");
        }

        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("Empty safety question");
    }

    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int count = userMapper.checkAnswer(username, question, answer);
        if (count > 0) {
            String token = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username, token);
            return ServerResponse.createBySuccess(token);
        }
        return ServerResponse.createByErrorMessage("Wrong answer");
    }

    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        if (StringUtils.isBlank(forgetToken)) {
            ServerResponse.createByErrorMessage("Empty token");
        }
        ServerResponse<String> response = checkValid(username, Const.USERNAME);
        if (response.isSuccess()) {
            return ServerResponse.createByErrorMessage("Username does not exist");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("Invalid token or your token has expired");
        }

        if (StringUtils.equals(forgetToken, token)) {
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int row = userMapper.updatePasswordByUsername(username, md5Password);
            if (row > 0) {
                return ServerResponse.createBySuccessMessage("Success!");
            }
        } else {
            return ServerResponse.createByErrorMessage("Wrong token, please acquire another reset token.");
        }
        return ServerResponse.createByErrorMessage("Error occurred when resetting password");
    }

    public ServerResponse<String> resetPassword(String passwordNew, String passwordOld, User user) {
        int count = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if (count == 0) {
            return ServerResponse.createByErrorMessage("Please enter the correct password");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        count = userMapper.updateByPrimaryKeySelective(user);
        if (count > 0) {
            return ServerResponse.createBySuccessMessage("Success!");
        }
        return ServerResponse.createByErrorMessage("Error occurred when resetting password.");
    }

    public ServerResponse<User> updateInformation(User user) {
        // username attribute can not be updated.
        // Check whether the email belongs to another user or not.
        int count = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (count > 0) {
            return ServerResponse.createByErrorMessage("The email has existed, please enter a new one.");
        }
        User update = new User();
        update.setId(user.getId());
        update.setEmail(user.getEmail());
        update.setPhone(user.getPhone());
        update.setQuestion(user.getQuestion());
        update.setAnswer(user.getAnswer());

        count = userMapper.updateByPrimaryKeySelective(update);
        if (count > 0) {
            return ServerResponse.createBySuccess("Success", user);
        }
        return ServerResponse.createByErrorMessage("Error occurred when updating personal info.");
    }

    public ServerResponse<User> get_information(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMessage("Can not find the current user.");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    public ServerResponse<String> checkAdminRole(User user) {
        if (user != null && user.getRole() == Const.Role.ROLE_ADMIN) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }
}
