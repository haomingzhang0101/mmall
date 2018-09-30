package com.haoming.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.haoming.common.ServerResponse;
import com.haoming.dao.ShippingMapper;
import com.haoming.pojo.Shipping;
import com.haoming.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int count = shippingMapper.insert(shipping);
        if (count > 0) {
            Map result = Maps.newHashMap();
            result.put("shippingId", shipping.getId());
            return ServerResponse.createBySuccess("success", result);
        }
        return ServerResponse.createByErrorMessage("Failed to create a new address.");
    }

    public ServerResponse<String> del(Integer userId, Integer shippingId) {
//        int resultCount = shippingMapper.deleteByPrimaryKey(shippingId);
        int count = shippingMapper.deleteByShippingIdUserId(userId, shippingId);
        if (count > 0) {
            return ServerResponse.createBySuccess("success");
        }
        return ServerResponse.createByErrorMessage("Failed to delete the address.");
    }

    public ServerResponse update(Integer userId, Shipping shipping){
        shipping.setUserId(userId); // For safety concern.
        int count = shippingMapper.updateByShipping(shipping);
        if (count > 0) {
            return ServerResponse.createBySuccess("success");
        }
        return ServerResponse.createByErrorMessage("Failed to update the address.");
    }

    public ServerResponse<Shipping> select(Integer userId, Integer shippingId) {
        Shipping shipping = shippingMapper.selectByShippingIdUserId(userId, shippingId);
        if (shipping == null) {
            return ServerResponse.createByErrorMessage("The address does not exist");
        }
        return ServerResponse.createBySuccess("success", shipping);
    }

    public ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageinfo = new PageInfo<>(shippingList);
        return ServerResponse.createBySuccess(pageinfo);
    }
}
