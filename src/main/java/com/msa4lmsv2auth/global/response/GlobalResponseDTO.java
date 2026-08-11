package com.msa4lmsv2auth.global.response;

import com.msa4lmsv2auth.global.response.constant.CustomResponseCode;

public record GlobalResponseDTO<T> (
        String code
        , String message
        , T data
){
    public static <T> GlobalResponseDTO<T> from(CustomResponseCode customResponseCode, T data){
        return new GlobalResponseDTO<T>(customResponseCode.getCode(), customResponseCode.name(), data);
    }

    // data가 null인 경우
    public static GlobalResponseDTO<Void> from(CustomResponseCode customResponseCode){
        return new GlobalResponseDTO<Void>(customResponseCode.getCode(), customResponseCode.name(), null);
    }

    // SUCCESS
    public static <T> GlobalResponseDTO<T> success(T data){
        return GlobalResponseDTO.<T>from(CustomResponseCode.SUCCESS, data);
        // return new GlobalResponseDTO<T>(CustomResponseCode.SUCCESS.getCode(), CustomResponseCode.SUCCESS.name(), data);
    }

    // data가 없는 success 패턴
    public static GlobalResponseDTO<Void> success(){
        return GlobalResponseDTO.<Void>from(CustomResponseCode.SUCCESS);
        // return new GlobalResponseDTO<Void>(CustomResponseCode.SUCCESS.getCode(), CustomResponseCode.SUCCESS.name(), null);
    }
}
