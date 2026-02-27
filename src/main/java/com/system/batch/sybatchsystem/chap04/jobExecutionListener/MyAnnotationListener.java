package com.system.batch.sybatchsystem.chap04.jobExecutionListener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MyAnnotationListener {

    @BeforeJob
    public void announceStart(JobExecution jobExecution) {
        log.info("[job 시작] id: {}", jobExecution.getJobInstanceId());
    }

    @AfterJob
    public void announceEnd(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.FAILED) {
            log.warn("[Job 실패] : 관리자에게 연락");
        } else {
            log.info("[Job 성공] : Job 성공");
        }
    }
}
