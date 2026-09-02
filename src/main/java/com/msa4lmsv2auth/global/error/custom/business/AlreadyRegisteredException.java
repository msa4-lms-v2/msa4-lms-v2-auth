package com.msa4lmsv2auth.global.error.custom.business;

import com.msa4lmsv2auth.global.error.custom.BusinessException;
import com.msa4lmsv2auth.global.response.constant.CustomResponseCode;


public class AlreadyRegisteredException extends BusinessException {
    public AlreadyRegisteredException(String message) {
        super(CustomResponseCode.DUPLICATE_ERROR, message);
    }
}
