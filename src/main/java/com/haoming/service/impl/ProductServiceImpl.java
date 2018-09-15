package com.haoming.service.impl;

import com.haoming.common.ResponseCode;
import com.haoming.common.ServerResponse;
import com.haoming.dao.CategoryMapper;
import com.haoming.dao.ProductMapper;
import com.haoming.pojo.Category;
import com.haoming.pojo.Product;
import com.haoming.service.IProductService;
import com.haoming.util.DateTimeUtil;
import com.haoming.util.PropertiesUtil;
import com.haoming.vo.ProductDetailVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product != null) {
            if (StringUtils.isNotBlank(product.getSubImages())) {
                String[] subImageArray = product.getSubImages().split(",");
                if (subImageArray.length > 0) {
                    product.setMainImage(subImageArray[0]);
                }
            }
            // Check whether the operation is save or update.
            if (product.getId() != null) {
                int row = productMapper.updateByPrimaryKey(product);
                if (row > 0) {
                    return ServerResponse.createBySuccess("Success");
                }
                return ServerResponse.createByErrorMessage("Update failed.");
            } else {
                int row = productMapper.insert(product);
                if (row > 0) {
                    return ServerResponse.createBySuccess("Success");
                }
                return ServerResponse.createByErrorMessage("Insert failed.");
            }
        }

        return ServerResponse.createByErrorMessage("Invalid praram");
    }

    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product update = new Product();
        update.setId(productId);
        update.setStatus(status);
        int row = productMapper.updateByPrimaryKeySelective(update);
        if (row > 0) {
            return ServerResponse.createBySuccess("success");
        }
        return ServerResponse.createByErrorMessage("Something wrong happened...");
    }

    public ServerResponse<ProductDetailVO> manageProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("This product has been deleted or not on sale");
        }

        // Returns a VO object.
        ProductDetailVO productDetailVO = assembleProductDetailVO(product);
        return ServerResponse.createBySuccess(productDetailVO);
    }

    private ProductDetailVO assembleProductDetailVO (Product product) {
        ProductDetailVO productDetailVO = new ProductDetailVO();
        productDetailVO.setId(product.getId());
        productDetailVO.setSubtitle(product.getSubtitle());
        productDetailVO.setPrice(product.getPrice());
        productDetailVO.setMainImage(product.getMainImage());
        productDetailVO.setSubImages(product.getSubImages());
        productDetailVO.setCategoryId(product.getCategoryId());
        productDetailVO.setDetail(product.getDetail());
        productDetailVO.setName(product.getName());
        productDetailVO.setStatus(product.getStatus());
        productDetailVO.setStock(product.getStock());

        productDetailVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://image.haoming.com/"));

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category == null) {
            productDetailVO.setParentCategoryId(0);
        } else {
            productDetailVO.setParentCategoryId(category.getParentId());
        }

        productDetailVO.setCreateTime(DateTimeUtil.DateToStr(product.getCreateTime()));
        productDetailVO.setUpdateTime(DateTimeUtil.DateToStr(product.getUpdateTime()));

        return productDetailVO;
    }


}
