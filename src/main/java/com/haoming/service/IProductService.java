package com.haoming.service;

import com.haoming.common.ServerResponse;
import com.haoming.pojo.Product;
import com.haoming.vo.ProductDetailVO;

public interface IProductService {

    ServerResponse saveOrUpdateProduct(Product product);

    ServerResponse<String> setSaleStatus(Integer productId, Integer status);

    ServerResponse<ProductDetailVO> manageProductDetail(Integer productId);
}
