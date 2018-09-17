package com.haoming.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.haoming.common.Const;
import com.haoming.common.ResponseCode;
import com.haoming.common.ServerResponse;
import com.haoming.dao.CategoryMapper;
import com.haoming.dao.ProductMapper;
import com.haoming.pojo.Category;
import com.haoming.pojo.Product;
import com.haoming.service.ICategoryService;
import com.haoming.service.IProductService;
import com.haoming.util.DateTimeUtil;
import com.haoming.util.PropertiesUtil;
import com.haoming.vo.ProductDetailVO;
import com.haoming.vo.ProductListVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

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

    public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize) {
        //startPage--start
        //Add SQL logic
        //pageHelper--final process
        PageHelper.startPage(pageNum, pageSize);
        List<Product> productList = productMapper.selectList();
        List<ProductListVO> productListVOList = Lists.newArrayList();

        for (Product item : productList) {
            productListVOList.add(assembleProductListVO(item));
        }
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVOList);
        return ServerResponse.createBySuccess(pageResult);
    }

    public ProductListVO assembleProductListVO(Product product) {
        ProductListVO productListVO = new ProductListVO();
        productListVO.setId(product.getId());
        productListVO.setName(product.getName());
        productListVO.setCategoryId(product.getCategoryId());
        productListVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://image.haoming.com/"));
        productListVO.setMainImage(product.getMainImage());
        productListVO.setPrice(product.getPrice());
        productListVO.setSubtitle(product.getSubtitle());
        productListVO.setStatus(product.getStatus());
        return productListVO;
    }

    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName)) {
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectByNameAndProductId(productName, productId);
        List<ProductListVO> productListVOList = Lists.newArrayList();

        for (Product item : productList) {
            productListVOList.add(assembleProductListVO(item));
        }
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVOList);
        return ServerResponse.createBySuccess(pageResult);
    }

    public ServerResponse<ProductDetailVO> getProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null || product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return ServerResponse.createByErrorMessage("This product has been deleted or not on sale");
        }
        // Returns a VO object.
        ProductDetailVO productDetailVO = assembleProductDetailVO(product);
        return ServerResponse.createBySuccess(productDetailVO);
    }

    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize, String orderBy) {
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = Lists.newArrayList();

        if (categoryId != null) {
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)) {
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListVO> productListVOList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVOList);
                return ServerResponse.createBySuccess(pageInfo);
            }
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(categoryId).getData();
        }
        if (StringUtils.isNotBlank(keyword)) {
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        PageHelper.startPage(pageNum, pageSize);
        // Dynamic sorting
        if (StringUtils.isNotBlank(orderBy)) {
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)) {
                String[] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);
            }
        }
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword) ? null : keyword, categoryIdList.size() == 0 ? null : categoryIdList);
        List<ProductListVO> productListVOList = Lists.newArrayList();

        for (Product item : productList) {
            productListVOList.add(assembleProductListVO(item));
        }
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVOList);
        return ServerResponse.createBySuccess(pageResult);
    }
}
