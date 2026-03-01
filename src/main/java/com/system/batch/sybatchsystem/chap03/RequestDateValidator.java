package com.system.batch.sybatchsystem.chap03;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersValidator;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class RequestDateValidator implements JobParametersValidator {

    @Override
    public void validate(@Nullable JobParameters parameters) throws InvalidJobParametersException {

        // 파라미터를 입력하지 않으면 new JobParameters() 객체가 들어온다.
        if(parameters==null){   // 이런 경우 방어코드로서만 역할을 한다. 실질적으로 파라미터 미입력시 방어가 안됨
            log.error("parameters is null");
            throw new InvalidJobParametersException("파라미터가 Null입니다");
        }

        // 그래서 원하는 타입으로 따로 받아서 validate 검사를 해야함
        String p = parameters.getString("requestDate");
        if( p == null || p.isBlank() ){
            log.error("requestDate 파라미터는 필수 입니다.");
            throw new InvalidJobParametersException("파라미터가 null/blank");
        }

    }
}
