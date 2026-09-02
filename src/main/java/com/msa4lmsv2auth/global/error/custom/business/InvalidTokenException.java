package com.msa4lmsv2auth.global.error.custom.business;

import com.msa4lmsv2auth.global.error.custom.BusinessException;
import com.msa4lmsv2auth.global.response.constant.CustomResponseCode;

public class InvalidTokenException extends BusinessException {
    public InvalidTokenException(String message) {
        super(CustomResponseCode.INVALID_TOKEN_ERROR ,message);
    }
}
