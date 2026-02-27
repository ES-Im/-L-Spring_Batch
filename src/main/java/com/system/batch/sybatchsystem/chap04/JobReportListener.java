package com.system.batch.sybatchsystem.chap04;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobReportListener {

    private final EmailProvider emailProvider;

    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        log.info("[배치 시작] id : {}", jobExecution.getJobInstanceId());
    }
    
    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.FAILED) {
            emailProvider.send(
                    "admin@naver.com",
                    "batch 실패 알림",
                    "job id : " + jobExecution.getJobInstanceId() + "번 실패"
            );
        } else {
            log.info("배치 성공");
        }
    }
}
