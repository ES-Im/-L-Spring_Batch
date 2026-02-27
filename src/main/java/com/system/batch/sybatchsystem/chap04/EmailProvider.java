package com.system.batch.sybatchsystem.chap04;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailProvider {

    public void send(String to, String subject, String message) {
        // smtp로 메일 전송
        log.info("[메일 발송 성공] 받는사람 : {}", to);
        log.info("[메일 발송 성공] 제목 : {}", subject);
        log.info("[메일 발송 성공] 내용 : {}", message);
    }
}
