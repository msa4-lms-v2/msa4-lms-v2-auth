package com.msa4lmsv2auth.global.error.custom.business;

import com.msa4lmsv2auth.global.error.custom.BusinessException;
import com.msa4lmsv2auth.global.response.constant.CustomResponseCode;


public class DuplicatedResourceException extends BusinessException {
    public DuplicatedResourceException(String message) {
        super(CustomResponseCode.DUPLICATE_ERROR, message);
    }
}
