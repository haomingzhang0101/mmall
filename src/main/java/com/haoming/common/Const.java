package com.haoming.common;

import com.google.common.collect.Sets;

import java.util.Set;

public class Const {

    public static final String CURRENT_USER = "currentUser";

    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    public interface ProductListOrderBy {
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc", "price_asc"); //O(1) for contains method
    }

    public interface Role{
        int ROLE_CUSTOMER = 0; //Normal user
        int ROLE_ADMIN = 1; //Admin
    }

    public enum ProductStatusEnum{
        ON_SALE(1, "On Sale");

        private String value;
        private int code;
        ProductStatusEnum(int code, String value) {
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }
}
