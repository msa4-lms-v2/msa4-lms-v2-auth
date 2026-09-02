package com.msa4lmsv2auth.global.error.custom.business;

import com.msa4lmsv2auth.global.error.custom.BusinessException;
import com.msa4lmsv2auth.global.response.constant.CustomResponseCode;

public class NotFoundResourceException extends BusinessException {
    public NotFoundResourceException(String message) {
        super(CustomResponseCode.DATA_NOT_FOUND_ERROR, message);
    }
}
