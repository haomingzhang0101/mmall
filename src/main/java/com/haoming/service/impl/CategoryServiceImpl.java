package com.haoming.service.impl;

import com.haoming.common.ServerResponse;
import com.haoming.dao.CategoryMapper;
import com.haoming.pojo.Category;
import com.haoming.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse<String> addCategory(String categoryName, Integer parentId) {
        if (parentId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("Wrong parameter");
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true); // Usable

        int count = categoryMapper.insert(category);
        if (count > 0) {
            return ServerResponse.createBySuccess("Success!");
        }
        return ServerResponse.createByErrorMessage("Something wrong happened...");
    }

    public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
        if (categoryId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("Wrong parameter");
        }

        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int count = categoryMapper.updateByPrimaryKeySelective(category);
        if (count > 0) {
            return ServerResponse.createBySuccess("Success!");
        }
        return ServerResponse.createByErrorMessage("Something wrong happened...");
    }


}
