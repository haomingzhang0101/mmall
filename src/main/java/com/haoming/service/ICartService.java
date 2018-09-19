package com.haoming.service;

import com.haoming.common.ServerResponse;
import com.haoming.vo.CartVO;

public interface ICartService {

    ServerResponse<CartVO> add(Integer userId, Integer productId, Integer count);
}
