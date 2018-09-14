package com.haoming.service;

import com.haoming.common.ServerResponse;
import com.haoming.pojo.User;

public interface IUserService {

    ServerResponse<User> login(String username, String password);

    ServerResponse<String> register(User user);

    ServerResponse<String> checkValid(String str, String type);

    ServerResponse<String> selectQuestion(String username);

    ServerResponse<String> checkAnswer(String username, String question, String answer);

    ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken);

    ServerResponse<String> resetPassword(String passwordNew, String passwordOld, User user);

    ServerResponse<User> updateInformation(User user);

    ServerResponse<User> get_information(Integer userId);

    ServerResponse<String> checkAdminRole(User user);
}
